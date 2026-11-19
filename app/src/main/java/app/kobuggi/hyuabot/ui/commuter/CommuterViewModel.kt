package app.kobuggi.hyuabot.ui.commuter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.CommuterBusPageQuery
import app.kobuggi.hyuabot.CommuterBusPeriodQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

enum class CommuterLoad { Idle, First, Refresh }

data class CommuterUiState(
    val load: CommuterLoad = CommuterLoad.First,
    val failed: Boolean = false,
    val routes: List<CommuterRoute> = emptyList(),
    val context: CommuterServiceContext? = null,
    val selection: CommuterSelection = CommuterSelection(),
    /** 디버그 픽스처가 열어 둔 지도 시트 정류장. */
    val sheetStop: CommuterStop? = null,
) {
    val model: CommuterScreenModel? =
        if (context != null && routes.isNotEmpty()) CommuterLogic.makeModel(routes, context, selection) else null
    val hasContext: Boolean get() = context != null
}

@HiltViewModel
class CommuterViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val clock: Clock = Clock.system(CommuterLogic.ZONE)
    private val _state = MutableStateFlow(CommuterUiState())
    val state: StateFlow<CommuterUiState> = _state.asStateFlow()
    private var loadJob: Job? = null
    private var fixture: CommuterFixture? = null

    /** 픽스처가 있으면 네트워크·위치·시계를 쓰지 않는다 (디버그 빌드 전용). */
    fun start(fixtureQuery: String?, isDebug: Boolean) {
        if (_state.value.hasContext || loadJob?.isActive == true || fixture != null) return
        if (isDebug && fixtureQuery != null) {
            val f = CommuterFixture.parse(fixtureQuery).also { fixture = it }
            val sheetStop = f.sheetIndex?.let { i ->
                val dir = f.selection.direction ?: CommuterLogic.autoDirection(f.nowMinutes)
                val model = CommuterLogic.makeModel(f.routes, f.context, f.selection)
                f.routes.firstOrNull { it.name == model.selectedRouteName && it.direction == dir }?.stops?.getOrNull(i)
            }
            _state.value = when {
                f.error -> CommuterUiState(load = CommuterLoad.Idle, failed = true)
                f.loading == "first" -> CommuterUiState(load = CommuterLoad.First)
                else -> CommuterUiState(
                    load = if (f.loading == "refresh") CommuterLoad.Refresh else CommuterLoad.Idle,
                    routes = f.routes, context = f.context, selection = f.selection, sheetStop = sheetStop,
                )
            }
            return
        }
        load()
    }

    fun load(refresh: Boolean = false) {
        fixture = null
        if (loadJob?.isActive == true) return
        _state.update {
            it.copy(load = if (refresh && it.hasContext) CommuterLoad.Refresh else CommuterLoad.First, failed = false)
        }
        loadJob = viewModelScope.launch {
            val now = ZonedDateTime.now(clock)
            val today = now.toLocalDate()
            val location = async { currentLocation() }
            val page = try {
                apolloClient.query(CommuterBusPageQuery(today)).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            val data = page?.data
            if (data == null || page.exception != null) {
                location.cancel()
                _state.update { it.copy(load = CommuterLoad.Idle, failed = true) }
                return@launch
            }
            val korean = java.util.Locale.getDefault().language == "ko"
            val routes = CommuterLogic.routes(
                data.commute.map { r ->
                    CommuterLogic.CommuteInput(
                        r.name, r.description.korean, r.description.english,
                        r.timetable.map { t ->
                            CommuterLogic.EntryInput(
                                t.name, t.order, t.time.toString(), t.stop?.name, t.stop?.description, t.stop?.latitude, t.stop?.longitude,
                            )
                        },
                    )
                },
                korean,
            )
            val period = data.shuttle.period
            val periodEnd = period?.end?.let(CommuterLogic::periodBoundaryDay)
            val nextRun = if (period?.type == "semester") null else resolveNextSemesterDay(periodEnd)
            val context = CommuterServiceContext(
                date = today,
                nowMinutes = now.hour * 60 + now.minute,
                periodType = period?.type,
                periodEndDay = periodEnd,
                holidayType = data.shuttle.holiday?.type,
                nextRunDay = nextRun,
                userLocation = location.await(),
            )
            _state.update { it.copy(load = CommuterLoad.Idle, failed = false, routes = routes, context = context) }
        }
    }

    /** 방학·계절학기면 기간 끝 다음 날을 최대 4번 따라가 다음 학기 시작일(주말이면 다음 평일)을 찾는다. */
    private suspend fun resolveNextSemesterDay(periodEnd: LocalDate?): LocalDate? {
        var boundary = periodEnd
        repeat(4) {
            val next = boundary?.plusDays(1) ?: return null
            val response = try {
                apolloClient.query(CommuterBusPeriodQuery(next)).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return null
            }
            val period = response.data?.shuttle?.period ?: return null
            if (period.type == "semester") {
                return CommuterLogic.firstWeekday(CommuterLogic.periodBoundaryDay(period.start))
            }
            boundary = CommuterLogic.periodBoundaryDay(period.end)
        }
        return null
    }

    /** 화면이 보이는 동안 60초마다, 그리고 다시 앞으로 올 때 부른다. 날짜가 바뀌면 다시 불러온다. */
    fun tick() {
        if (fixture != null) return
        val context = _state.value.context ?: return
        val now = ZonedDateTime.now(clock)
        if (now.toLocalDate() != context.date) {
            load()
        } else {
            val minutes = now.hour * 60 + now.minute
            if (minutes != context.nowMinutes) _state.update { it.copy(context = context.copy(nowMinutes = minutes)) }
        }
    }

    /** 위치 권한을 막 허용받았을 때: 위치만 다시 받아 강조를 갱신한다. */
    fun refreshLocation() {
        if (fixture != null) return
        viewModelScope.launch {
            val point = currentLocation() ?: return@launch
            loadJob?.join() // 진행 중인 불러오기가 끝난 뒤에 적용해야 덮어쓰이지 않는다
            _state.update { s -> s.context?.let { s.copy(context = it.copy(userLocation = point)) } ?: s }
        }
    }

    fun selectDirection(direction: CommuterDirection) {
        val s = _state.value
        val model = s.model ?: return
        val route = if (direction == model.autoDirection) model.autoRouteName
        else s.routes.firstOrNull { it.direction == direction }?.name
        _state.update { it.copy(selection = CommuterSelection(direction, route)) }
    }

    fun selectRoute(name: String) {
        val model = _state.value.model ?: return
        _state.update { it.copy(selection = CommuterSelection(model.selectedDirection, name)) }
    }

    fun resetToNearest() = _state.update { it.copy(selection = CommuterSelection()) }

    fun expand() = _state.update { it.copy(selection = it.selection.copy(expanded = true)) }

    fun closeSheet() = _state.update { it.copy(sheetStop = null) }

    fun openSheet(stop: CommuterStop) = _state.update { it.copy(sheetStop = stop) }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(): GeoPoint? {
        val granted = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).any {
            ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!granted) return null
        val client = LocationServices.getFusedLocationProviderClient(appContext)
        return withTimeoutOrNull(4_000) {
            suspendCancellableCoroutine { continuation ->
                val source = CancellationTokenSource()
                continuation.invokeOnCancellation { source.cancel() }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, source.token)
                    .addOnSuccessListener { continuation.resume(it?.let { l -> GeoPoint(l.latitude, l.longitude) }) }
                    .addOnFailureListener { continuation.resume(null) }
            }
        }
    }
}
