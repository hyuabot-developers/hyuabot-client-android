package app.kobuggi.hyuabot.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatDelegate
import app.kobuggi.hyuabot.BusDepartureLogDialogQuery
import app.kobuggi.hyuabot.HomePageQuery
import app.kobuggi.hyuabot.service.ShuttlePresenceService
import app.kobuggi.hyuabot.service.alarm.ShuttleServiceNoticeScheduler
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.type.BusRouteStopInput
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val shuttleServiceNoticeScheduler: ShuttleServiceNoticeScheduler,
    private val shuttlePresenceService: ShuttlePresenceService,
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _data = MutableLiveData<HomePageQuery.Data?>()
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _showBus50Transfer = MutableLiveData(true)
    private val _showSubwayTransfer = MutableLiveData(true)
    private val _subwayTransferDestination = MutableLiveData(HomeSubwayTransferDestination.SEOUL)
    private val _bus50TerminalLogTimes = MutableLiveData<List<LocalTime>>(emptyList())
    private val _showPresenceStatus = MutableLiveData(true)
    private val _presenceViewerCount = MutableLiveData<Int?>(null)
    private var isFetching = false
    private var presenceJob: Job? = null
    private var selectedPresenceStop = "dormitory_o"
    private var presencePreviewCount: Int? = null
    private var presencePreferenceLoaded = false
    private var presenceUpdatesStarted = false

    val isLoading: LiveData<Boolean> get() = _isLoading
    val data: LiveData<HomePageQuery.Data?> get() = _data
    val queryError: LiveData<QueryError?> get() = _queryError
    val showBus50Transfer: LiveData<Boolean> get() = _showBus50Transfer
    val showSubwayTransfer: LiveData<Boolean> get() = _showSubwayTransfer
    val subwayTransferDestination: LiveData<HomeSubwayTransferDestination> get() = _subwayTransferDestination
    val bus50TerminalLogTimes: LiveData<List<LocalTime>> get() = _bus50TerminalLogTimes
    val showPresenceStatus: LiveData<Boolean> get() = _showPresenceStatus
    val presenceViewerCount: LiveData<Int?> get() = _presenceViewerCount

    init {
        viewModelScope.launch {
            userPreferencesRepository.getShowHomeBus50Transfer().collect {
                _showBus50Transfer.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getShowHomeSubwayTransfer().collect {
                _showSubwayTransfer.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getHomeSubwayTransferDestination().collect {
                _subwayTransferDestination.value = HomeSubwayTransferDestination.from(it)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getShowShuttlePresence().collect {
                applyShowPresenceStatus(it)
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            if (isFetching) return@launch
            isFetching = true
            if (_data.value == null) _isLoading.value = true
            try {
                val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                val mealDate = if (now.hour >= 20) now.toLocalDate().plusDays(1) else now.toLocalDate()
                val response = apolloClient.query(
                    HomePageQuery(
                        language = currentNoticeLanguage(),
                        after = Optional.present(LocalTime.now(ZoneId.of("Asia/Seoul"))),
                        weekday = currentSubwayWeekday(now),
                        date = mealDate,
                        campusID = userPreferencesRepository.campusID.first(),
                        busInput = homeBusInput(),
                    )
                ).fetchPolicy(FetchPolicy.NetworkOnly).execute()

                if (response.data == null || response.exception != null) {
                    _queryError.value = QueryError.SERVER_ERROR
                } else {
                    _data.value = response.data
                    _bus50TerminalLogTimes.value = fetchBus50TerminalLogTimes(now.toLocalDate())
                    shuttleServiceNoticeScheduler.sync()
                    _queryError.value = null
                }
            } catch (_: Exception) {
                _queryError.value = QueryError.SERVER_ERROR
            } finally {
                _isLoading.value = false
                isFetching = false
            }
        }
    }

    fun setShowBus50Transfer(show: Boolean) {
        _showBus50Transfer.value = show
        viewModelScope.launch {
            userPreferencesRepository.setShowHomeBus50Transfer(show)
        }
    }

    fun setShowSubwayTransfer(show: Boolean) {
        _showSubwayTransfer.value = show
        viewModelScope.launch {
            userPreferencesRepository.setShowHomeSubwayTransfer(show)
        }
    }

    fun setSubwayTransferDestination(destination: HomeSubwayTransferDestination) {
        _subwayTransferDestination.value = destination
        viewModelScope.launch {
            userPreferencesRepository.setHomeSubwayTransferDestination(destination.value)
        }
    }

    fun setShowPresenceStatus(show: Boolean) {
        applyShowPresenceStatus(show)
        viewModelScope.launch { userPreferencesRepository.setShowShuttlePresence(show) }
    }

    private fun applyShowPresenceStatus(show: Boolean) {
        val changed = _showPresenceStatus.value != show
        _showPresenceStatus.value = show
        presencePreferenceLoaded = true
        if (changed || presenceJob == null) restartPresenceUpdates()
    }

    fun setPresenceStop(stopId: String) {
        if (selectedPresenceStop == stopId) return
        selectedPresenceStop = stopId
        restartPresenceUpdates()
    }

    fun setPresencePreviewCount(count: Int?) {
        if (presencePreviewCount == count) return
        presencePreviewCount = count
        restartPresenceUpdates()
    }

    fun startPresenceUpdates() {
        presenceUpdatesStarted = true
        restartPresenceUpdates()
    }

    fun stopPresenceUpdates() {
        presenceUpdatesStarted = false
        presenceJob?.cancel()
        presenceJob = null
        _presenceViewerCount.value = null
    }

    private fun restartPresenceUpdates() {
        presenceJob?.cancel()
        presenceJob = null
        _presenceViewerCount.value = null
        if (!presenceUpdatesStarted || !presencePreferenceLoaded || _showPresenceStatus.value != true) return
        presenceJob = viewModelScope.launch {
            presencePreviewCount?.let {
                _presenceViewerCount.value = it
                return@launch
            }
            while (isActive) {
                _presenceViewerCount.value = shuttlePresenceService.heartbeat(selectedPresenceStop)
                delay(PRESENCE_REFRESH_INTERVAL_MILLIS)
            }
        }
    }

    private fun currentSubwayWeekday(now: ZonedDateTime): String {
        return if (now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY) {
            "weekends"
        } else {
            "weekdays"
        }
    }

    private fun currentNoticeLanguage(): String {
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
        val language = locale?.language ?: Locale.getDefault().language
        return if (language == Locale.KOREAN.language) "KOREAN" else "ENGLISH"
    }

    private fun homeBusInput(): List<BusRouteStopInput> = listOf(
        BusRouteStopInput(route = 216000068, stop = 216000383, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000068, stop = 216000138, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000081, stop = 216000028, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000101, stop = 216000028, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000016, stop = 216000152, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000082, stop = 216000077, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000102, stop = 216000077, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000016, stop = 216000074, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000082, stop = 217000140, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000102, stop = 217000140, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000016, stop = 217000264, limit = Optional.present(1)),
    )

    private suspend fun fetchBus50TerminalLogTimes(today: LocalDate): List<LocalTime> {
        val dates = listOf(
            today.minusDays(7),
            today.minusDays(2),
            today.minusDays(1),
        )
        return try {
            val response = apolloClient.query(
                BusDepartureLogDialogQuery(
                    listOf(
                        BusRouteStopInput(
                            route = 216000075,
                            stop = 216000759,
                            dates = Optional.present(dates),
                        ),
                    ),
                ),
            ).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            response.data?.bus
                ?.flatMap { it.log }
                ?.map { it.time }
                ?.sorted()
                .orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun onCleared() {
        stopPresenceUpdates()
    }

    private companion object {
        const val PRESENCE_REFRESH_INTERVAL_MILLIS = 30_000L
    }
}
