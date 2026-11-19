package app.kobuggi.hyuabot.ui.readingRoom

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import app.kobuggi.hyuabot.service.alarm.AlarmFunction
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReadingRoomLoad { Idle, First, Refresh }

/** 권한 안내 시트 종류. 알림 권한 없음 · 영구 거부 · 정확한 알람 없음. */
enum class ReadingRoomSheet { Perm, Denied, Exact }

/** 알림 권한·정확한 알람 안내 뒤에 이어서 실행할 동작. */
sealed interface ReadingRoomJob {
    data class Bell(val seq: Int) : ReadingRoomJob
    data class Alarm(val hours: Int) : ReadingRoomJob
}

/** 스낵바로 알리는 결과. 문구는 화면에서 문자열 리소스로 만든다. */
sealed interface ReadingRoomMessage {
    data class Subscribed(val seq: Int, val on: Boolean) : ReadingRoomMessage
    data class SubscribeFailed(val on: Boolean) : ReadingRoomMessage
    data class AlarmScheduled(val at: LocalDateTime) : ReadingRoomMessage
    data object AlarmCancelled : ReadingRoomMessage
    data object PermissionDenied : ReadingRoomMessage
}

data class ReadingRoomItem(
    val seq: Int,
    /** 서버가 준 이름. 앱 문자열에 있는 열람실은 그 문자열을 쓴다. */
    val name: String,
    val active: Int,
    val available: Int,
)

/** [hours]는 저장된 값이 없으면 0. */
data class ReadingRoomAlarm(val at: LocalDateTime, val hours: Int)

data class ReadingRoomUiState(
    val load: ReadingRoomLoad = ReadingRoomLoad.First,
    /** 첫 로딩 실패만 true. 목록이 있으면 실패해도 기존 목록을 둔다. */
    val failed: Boolean = false,
    val campus: Int = ReadingRoomLogic.DEFAULT_CAMPUS,
    val rooms: List<ReadingRoomItem> = emptyList(),
    val updatedAt: ZonedDateTime? = null,
    val subscribed: Set<Int> = emptySet(),
    val alarm: ReadingRoomAlarm? = null,
    val sheet: ReadingRoomSheet? = null,
    val job: ReadingRoomJob? = null,
    val message: ReadingRoomMessage? = null,
    /** 디버그 픽스처의 고정 시각 (알림 시각 계산용). */
    val clockOverride: LocalDateTime? = null,
) {
    val hasData: Boolean get() = updatedAt != null
}

@HiltViewModel
class ReadingRoomViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(ReadingRoomUiState())
    val state: StateFlow<ReadingRoomUiState> = _state.asStateFlow()
    private var all: List<ReadingRoomPageQuery.ReadingRoom> = emptyList()
    private var loadJob: Job? = null
    private var started = false
    private var fixture = false
    private val alarmFunction = AlarmFunction(appContext)

    val isFixture: Boolean get() = fixture

    /** 픽스처가 있으면 네트워크·저장값을 쓰지 않는다 (디버그 빌드 전용). */
    fun start(fixtureQuery: String?, isDebug: Boolean) {
        if (started) return
        started = true
        if (isDebug && fixtureQuery != null) {
            fixture = true
            _state.value = ReadingRoomFixture.parse(fixtureQuery)
            return
        }
        viewModelScope.launch {
            combine(
                userPreferencesRepository.campusID,
                userPreferencesRepository.readingRoomNotifications,
                userPreferencesRepository.readingRoomExtendNotification,
                userPreferencesRepository.readingRoomExtendHours,
            ) { campus, topics, alarmTime, hours ->
                Stored(campus, topics.mapNotNull { it.removePrefix("reading_room_").toIntOrNull() }.toSet(), alarmTime, hours)
            }.collect { stored ->
                val alarm = ReadingRoomLogic.restoreAlarm(stored.alarmTime, LocalDateTime.now(ReadingRoomLogic.ZONE))
                if (alarm == null && stored.alarmTime.isNotEmpty()) clearAlarmStorage()
                _state.update { it.copy(campus = stored.campus, subscribed = stored.subscribed, alarm = alarm?.let { at -> ReadingRoomAlarm(at, stored.hours) }) }
                publishRooms()
            }
        }
        refresh(manual = false)
    }

    private data class Stored(val campus: Int, val subscribed: Set<Int>, val alarmTime: String, val hours: Int)

    /** 이미 불러오는 중이면 무시한다. 목록이 있으면 스켈레톤 없이 조용히 바꾸고, 수동이면 표시기를 보여 준다. */
    fun refresh(manual: Boolean) {
        if (fixture || loadJob?.isActive == true) return
        _state.update {
            when {
                // 첫 로딩에 실패해 오류 카드가 떠 있을 때의 자동 갱신은 조용히 다시 시도한다 (스켈레톤으로 깜빡이지 않는다).
                !it.hasData -> if (it.failed && !manual) it else it.copy(load = ReadingRoomLoad.First, failed = false)
                manual -> it.copy(load = ReadingRoomLoad.Refresh)
                else -> it
            }
        }
        loadJob = viewModelScope.launch {
            val data = try {
                val response = apolloClient.query(ReadingRoomPageQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
                response.data?.takeIf { response.exception == null }?.readingRoom
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (data == null) {
                // 첫 로딩 실패만 오류 카드. 그 밖에는 기존 목록을 그대로 둔다.
                _state.update { it.copy(load = ReadingRoomLoad.Idle, failed = !it.hasData) }
            } else {
                all = data
                _state.update { it.copy(load = ReadingRoomLoad.Idle, failed = false) }
                publishRooms(loaded = true)
            }
        }
    }

    /** 캠퍼스로 거른 서버 순서 그대로 보여 주고, 기준 시각은 거른 목록의 updatedAt 최댓값(없으면 지금)이다. */
    private fun publishRooms(loaded: Boolean = false) {
        if (!loaded && all.isEmpty()) return
        _state.update { current ->
            val filtered = all.filter { it.campus == current.campus }
            current.copy(
                rooms = filtered.map { ReadingRoomItem(it.seq, it.name, it.seats.active, it.seats.available) },
                updatedAt = ReadingRoomLogic.latestUpdate(filtered.map { it.updatedAt }) ?: ZonedDateTime.now(ReadingRoomLogic.ZONE),
            )
        }
    }

    fun openSheet(sheet: ReadingRoomSheet, job: ReadingRoomJob? = null) =
        _state.update { it.copy(sheet = sheet, job = job ?: it.job) }

    fun setJob(job: ReadingRoomJob?) = _state.update { it.copy(job = job) }

    /** 시트만 닫고 이어서 할 동작은 남긴다 (시스템 권한 팝업·설정 화면으로 넘어갈 때). */
    fun hideSheet() = _state.update { it.copy(sheet = null) }

    fun dismissSheet() = _state.update { it.copy(sheet = null, job = null) }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun showMessage(message: ReadingRoomMessage) = _state.update { it.copy(message = message) }

    /** 벨: 켜져 있으면 해제, 꺼져 있으면 FCM 토픽을 구독한다. 성공하면 목록에 저장하고 스낵바를 띄운다. */
    fun toggleSubscription(seq: Int) {
        val on = seq !in _state.value.subscribed
        _state.update { it.copy(job = null) }
        if (fixture) {
            _state.update { s ->
                s.copy(subscribed = if (on) s.subscribed + seq else s.subscribed - seq, message = ReadingRoomMessage.Subscribed(seq, on))
            }
            return
        }
        val topic = "reading_room_$seq"
        val task = if (on) Firebase.messaging.subscribeToTopic(topic) else Firebase.messaging.unsubscribeFromTopic(topic)
        task.addOnSuccessListener {
            viewModelScope.launch {
                if ((seq in _state.value.subscribed) != on) userPreferencesRepository.toggleReadingRoomNotification(seq)
                showMessage(ReadingRoomMessage.Subscribed(seq, on))
            }
        }.addOnFailureListener {
            showMessage(ReadingRoomMessage.SubscribeFailed(on))
        }
    }

    /** 지금부터 h × 60 − 10분 뒤 한 번 울린다. 이전 예약은 지우고 새로 건다. 시각과 시간 수를 저장해 다시 들어와도 복원한다. */
    fun scheduleAlarm(hours: Int) {
        val now = _state.value.clockOverride ?: LocalDateTime.now(ReadingRoomLogic.ZONE)
        val at = ReadingRoomLogic.alarmAt(now, hours)
        _state.update { it.copy(job = null, sheet = null, alarm = ReadingRoomAlarm(at, hours), message = ReadingRoomMessage.AlarmScheduled(at)) }
        if (fixture) return
        cancelScheduled()
        val requestCode = if (hours == 3) R.string.reading_room_alarm_3_hour else R.string.reading_room_alarm_4_hour
        alarmFunction.callAlarm(at.format(ReadingRoomLogic.STORED_TIME), requestCode, appContext.getString(R.string.reading_room_alarm_extend))
        viewModelScope.launch {
            userPreferencesRepository.setReadingRoomExtend(at.format(ReadingRoomLogic.STORED_TIME), hours)
        }
    }

    fun cancelAlarm() {
        _state.update { it.copy(alarm = null, message = ReadingRoomMessage.AlarmCancelled) }
        if (fixture) return
        cancelScheduled()
        clearAlarmStorage()
    }

    private fun cancelScheduled() {
        alarmFunction.cancelAlarm(R.string.reading_room_alarm_3_hour)
        alarmFunction.cancelAlarm(R.string.reading_room_alarm_4_hour)
    }

    private fun clearAlarmStorage() {
        viewModelScope.launch {
            userPreferencesRepository.setReadingRoomExtend(null, null)
        }
    }
}
