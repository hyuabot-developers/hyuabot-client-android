package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.ShuttlePresenceService
import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination
import app.kobuggi.hyuabot.ui.shuttle.initialstop.ShuttleGeoCoordinate
import app.kobuggi.hyuabot.ui.shuttle.initialstop.ShuttleInitialStopRuleCandidate
import app.kobuggi.hyuabot.util.QueryError
import app.kobuggi.hyuabot.util.currentShuttleWeekday
import app.kobuggi.hyuabot.util.shuttleBusLogReferenceDates
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ShuttleRealtimeViewModel @Inject constructor(
    val userPreferencesRepository: UserPreferencesRepository,
    private val apolloClient: ApolloClient,
    private val shuttlePresenceService: ShuttlePresenceService,
): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _showDepartureTime = MutableLiveData(false)
    private val _showByDestination = MutableLiveData(false)
    private val _showRemainingTime = MutableLiveData(true)
    private val _result = MutableLiveData<List<ShuttleRealtimePageQuery.Stop>>()
    private val _initialStopRules = MutableLiveData<List<ShuttleInitialStopRuleCandidate>>(emptyList())
    private val _notices = MutableLiveData<List<ShuttleRealtimePageQuery.Notice1>>()
    private val _transfer = MutableLiveData<ShuttleRealtimePageQuery.Data?>(null)
    private val _disposable = CompositeDisposable()
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _busAlternativeShuttlecock = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeDormitory = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeStation = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeDormitory80 = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeShuttlecock62 = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeTerminal80 = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeTerminal62 = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeJungang80 = MutableLiveData<BusAlternativeData?>(null)
    private val _busAlternativeJungang62 = MutableLiveData<BusAlternativeData?>(null)
    private val _forceShowBusAlternative = MutableLiveData<Boolean>(false)
    private val _showPresenceStatus = MutableLiveData(true)
    private val _showBusTransfer = MutableLiveData(true)
    private val _showSubwayTransfer = MutableLiveData(true)
    private val _subwayTransferDestination = MutableLiveData(HomeSubwayTransferDestination.SEOUL)
    private val _alternativeDisplayMode = MutableLiveData(ShuttleAlternativeDisplayMode.AUTOMATIC)
    private val _presenceViewerCount = MutableLiveData<Int?>(null)
    private val _presenceAvailableSeats = MutableLiveData<Int?>(null)
    private var presenceJob: Job? = null
    private var selectedPresenceStop = PRESENCE_STOP_IDS.first()
    private var presencePreviewCount: Int? = null
    private var presencePreferenceLoaded = false
    private var isStarted = false

    val result get() = _result
    val initialStopRules get() = _initialStopRules
    val notices get() = _notices
    val transfer get() = _transfer
    val isLoading get() = _isLoading
    val queryError get() = _queryError
    val showDepartureTime get() = _showDepartureTime
    val showByDestination get() = _showByDestination
    val busAlternativeShuttlecock get() = _busAlternativeShuttlecock
    val busAlternativeDormitory get() = _busAlternativeDormitory
    val busAlternativeStation get() = _busAlternativeStation
    val busAlternativeDormitory80 get() = _busAlternativeDormitory80
    val busAlternativeShuttlecock62 get() = _busAlternativeShuttlecock62
    val busAlternativeTerminal80 get() = _busAlternativeTerminal80
    val busAlternativeTerminal62 get() = _busAlternativeTerminal62
    val busAlternativeJungang80 get() = _busAlternativeJungang80
    val busAlternativeJungang62 get() = _busAlternativeJungang62
    val forceShowBusAlternative get() = _forceShowBusAlternative
    val showPresenceStatus get() = _showPresenceStatus
    val showBusTransfer get() = _showBusTransfer
    val showSubwayTransfer get() = _showSubwayTransfer
    val subwayTransferDestination get() = _subwayTransferDestination
    val alternativeDisplayMode get() = _alternativeDisplayMode
    val presenceViewerCount get() = _presenceViewerCount
    val presenceAvailableSeats get() = _presenceAvailableSeats

    init {
        viewModelScope.launch {
            userPreferencesRepository.getShowHomeBus50Transfer().collect {
                _showBusTransfer.value = it
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
            userPreferencesRepository.getShuttleAlternativeDisplayMode().collect {
                _alternativeDisplayMode.value = ShuttleAlternativeDisplayMode.from(it)
            }
        }
    }

    fun setForceShowBusAlternative(show: Boolean) {
        _forceShowBusAlternative.value = show
    }
    val latestShuttleResult = combine(result.asFlow(), showByDestination.asFlow()) { result, showByDestination ->
        ShuttleTabData(result, showByDestination)
    }.onStart { emit(ShuttleTabData(listOf(), false)) }.asLiveData()


    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
        val appLanguage = locale?.language ?: Locale.getDefault().language
        val language = if (appLanguage == Locale.KOREAN.language) "KOREAN" else "ENGLISH"
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleRealtimePageQuery(
                language,
                Optional.present(LocalTime.now()),
                currentShuttleWeekday(),
                Optional.present(shuttleBusLogReferenceDates()),
            )).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.shuttle?.stops != null) {
                _initialStopRules.value =
                    response.data?.shuttle?.initialStopRules.orEmpty().map { rule ->
                        ShuttleInitialStopRuleCandidate(
                            sequence = rule.seq,
                            stopName = rule.stopName,
                            priority = rule.priority,
                            polygon =
                                rule.polygon.map { point ->
                                    ShuttleGeoCoordinate(
                                        latitude = point.latitude,
                                        longitude = point.longitude,
                                    )
                                },
                        )
                    }
                _result.value = response.data?.shuttle?.stops
                _transfer.value = response.data
                updateBusAlternatives(response.data?.busAlternative.orEmpty())
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            if (_notices.value == null) {
                _notices.value = response.data?.notices?.flatMap { it.notices } ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    private fun updateBusAlternatives(busList: List<ShuttleRealtimePageQuery.BusAlternative>) {
        _busAlternativeShuttlecock.value = busList.firstOrNull { it.route.seq == 216000068 && it.stop.seq == 216000379 }
            .toBusAlternativeData(R.string.shuttle_bus_alternative_route_campus)
        _busAlternativeDormitory.value = busList.firstOrNull { it.route.seq == 216000068 && it.stop.seq == 216000383 }
            .toBusAlternativeData(R.string.shuttle_bus_alternative_route_campus)
        _busAlternativeStation.value = busList.firstOrNull { it.route.seq == 216000068 && it.stop.seq == 216000138 }
            .toBusAlternativeData(R.string.shuttle_bus_alternative_route)

        val item80A = busList.firstOrNull { it.route.seq == 216000081 && it.stop.seq == 216000028 }
        val itemN80A = busList.firstOrNull { it.route.seq == 216000101 && it.stop.seq == 216000028 }
        val stop28 = item80A?.stop ?: itemN80A?.stop
        _busAlternativeDormitory80.value = selectBestRoute(
            BusRouteOption(item80A?.arrival?.firstOrNull()?.minutes, R.string.shuttle_bus_alternative_route_80a, stop28?.name ?: "", stop28?.latitude ?: 0.0, stop28?.longitude ?: 0.0),
            BusRouteOption(itemN80A?.arrival?.firstOrNull()?.minutes, R.string.shuttle_bus_alternative_route_n80a, stop28?.name ?: "", stop28?.latitude ?: 0.0, stop28?.longitude ?: 0.0)
        )

        _busAlternativeShuttlecock62.value = busList.firstOrNull { it.route.seq == 216000016 && it.stop.seq == 216000152 }
            .toBusAlternativeData(R.string.shuttle_bus_alternative_route_62_terminal)

        val item80B_t = busList.firstOrNull { it.route.seq == 216000082 && it.stop.seq == 216000077 }
        val itemN80B_t = busList.firstOrNull { it.route.seq == 216000102 && it.stop.seq == 216000077 }
        val stop77 = item80B_t?.stop ?: itemN80B_t?.stop
        _busAlternativeTerminal80.value = selectBestRoute(
            BusRouteOption(item80B_t?.arrival?.firstOrNull()?.minutes, R.string.shuttle_bus_alternative_route_80b, stop77?.name ?: "", stop77?.latitude ?: 0.0, stop77?.longitude ?: 0.0),
            BusRouteOption(itemN80B_t?.arrival?.firstOrNull()?.minutes, R.string.shuttle_bus_alternative_route_n80b, stop77?.name ?: "", stop77?.latitude ?: 0.0, stop77?.longitude ?: 0.0)
        )

        _busAlternativeTerminal62.value = busList.firstOrNull { it.route.seq == 216000016 && it.stop.seq == 216000074 }
            .toBusAlternativeData(R.string.shuttle_bus_alternative_route_62_dormitory)

        val item80B_j = busList.firstOrNull { it.route.seq == 216000082 && it.stop.seq == 217000140 }
        val itemN80B_j = busList.firstOrNull { it.route.seq == 216000102 && it.stop.seq == 217000140 }
        val stop140 = item80B_j?.stop ?: itemN80B_j?.stop
        _busAlternativeJungang80.value = selectBestRoute(
            BusRouteOption(item80B_j?.arrival?.firstOrNull()?.minutes, R.string.shuttle_bus_alternative_route_80b, stop140?.name ?: "", stop140?.latitude ?: 0.0, stop140?.longitude ?: 0.0),
            BusRouteOption(itemN80B_j?.arrival?.firstOrNull()?.minutes, R.string.shuttle_bus_alternative_route_n80b, stop140?.name ?: "", stop140?.latitude ?: 0.0, stop140?.longitude ?: 0.0)
        )

        _busAlternativeJungang62.value = busList.firstOrNull { it.route.seq == 216000016 && it.stop.seq == 217000264 }
            .toBusAlternativeData(R.string.shuttle_bus_alternative_route_62_dormitory)
    }

    private data class BusRouteOption(val minutes: Int?, val routeName: Int, val stopName: String, val stopLat: Double, val stopLng: Double)

    private fun selectBestRoute(vararg options: BusRouteOption): BusAlternativeData? {
        val best = options.filter { it.stopLat != 0.0 }
            .minWithOrNull(compareBy<BusRouteOption> { it.minutes == null }.thenBy { it.minutes ?: Int.MAX_VALUE })
        return best?.let { BusAlternativeData(it.routeName, it.minutes, it.stopName, it.stopLat, it.stopLng) }
    }

    private fun ShuttleRealtimePageQuery.BusAlternative?.toBusAlternativeData(routeName: Int): BusAlternativeData? {
        return this?.let {
            BusAlternativeData(
                routeName,
                it.arrival.firstOrNull()?.minutes,
                it.stop.name,
                it.stop.latitude,
                it.stop.longitude
            )
        }
    }

    fun setRemainingTimeVisibility(isVisible: Boolean) {
        _showRemainingTime.value = isVisible
    }

    fun start() {
        isStarted = true
        if (_disposable.size() == 0) {
            _disposable.add(
                Observable.interval(0, 15, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        try {
                            fetchData()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            )
        }
        restartPresenceUpdates()
    }

    fun setShowDepartureTime(isVisible: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowShuttleDepartureTime(isVisible) }
    }

    fun setShowByDestination(isVisible: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowShuttleByDestination(isVisible) }
    }

    fun applyShowPresenceStatus(show: Boolean) {
        val changed = _showPresenceStatus.value != show
        _showPresenceStatus.value = show
        presencePreferenceLoaded = true
        if (changed || presenceJob == null) restartPresenceUpdates()
    }

    fun setShowPresenceStatus(show: Boolean) {
        applyShowPresenceStatus(show)
        viewModelScope.launch { userPreferencesRepository.setShowShuttlePresence(show) }
    }

    fun setShowBusTransfer(show: Boolean) {
        _showBusTransfer.value = show
        viewModelScope.launch { userPreferencesRepository.setShowHomeBus50Transfer(show) }
    }

    fun setShowSubwayTransfer(show: Boolean) {
        _showSubwayTransfer.value = show
        viewModelScope.launch { userPreferencesRepository.setShowHomeSubwayTransfer(show) }
    }

    fun setSubwayTransferDestination(destination: HomeSubwayTransferDestination) {
        _subwayTransferDestination.value = destination
        viewModelScope.launch {
            userPreferencesRepository.setHomeSubwayTransferDestination(destination.value)
        }
    }

    fun setAlternativeDisplayMode(mode: ShuttleAlternativeDisplayMode) {
        _alternativeDisplayMode.value = mode
        viewModelScope.launch { userPreferencesRepository.setShuttleAlternativeDisplayMode(mode.value) }
    }

    fun shouldShowBusAlternative(
        data: BusAlternativeData?,
        nextShuttleTime: LocalTime?,
        forceShow: Boolean = false,
    ): Boolean {
        return when (_alternativeDisplayMode.value ?: ShuttleAlternativeDisplayMode.AUTOMATIC) {
            ShuttleAlternativeDisplayMode.HIDDEN -> false
            ShuttleAlternativeDisplayMode.ALWAYS -> data?.minutes != null || forceShow
            ShuttleAlternativeDisplayMode.AUTOMATIC -> {
                forceShow ||
                    data?.minutes != null &&
                    (nextShuttleTime == null || minutesUntil(nextShuttleTime) >= ALTERNATIVE_THRESHOLD_MINUTES)
            }
        }
    }

    private fun minutesUntil(time: LocalTime): Long {
        val difference = java.time.Duration.between(LocalTime.now(), time).toMinutes()
        return if (difference >= 0) difference else difference + MINUTES_PER_DAY
    }

    fun setPresenceStop(position: Int) {
        val stopId = PRESENCE_STOP_IDS.getOrNull(position) ?: return
        if (selectedPresenceStop == stopId) return
        selectedPresenceStop = stopId
        restartPresenceUpdates()
    }

    fun setPresencePreviewCount(count: Int?) {
        presencePreviewCount = count
        restartPresenceUpdates()
    }

    private fun restartPresenceUpdates() {
        presenceJob?.cancel()
        presenceJob = null
        _presenceViewerCount.value = null
        _presenceAvailableSeats.value = null
        if (!isStarted || !presencePreferenceLoaded || _showPresenceStatus.value != true) return
        presenceJob = viewModelScope.launch {
            presencePreviewCount?.let {
                _presenceViewerCount.value = it
                return@launch
            }
            while (isActive) {
                val viewerCounts = shuttlePresenceService.viewerCounts()
                _presenceAvailableSeats.value = estimatedAvailableSeats(viewerCounts)
                _presenceViewerCount.value = shuttlePresenceService.heartbeat(selectedPresenceStop)
                delay(30_000)
            }
        }
    }

    fun stop() {
        isStarted = false
        presenceJob?.cancel()
        presenceJob = null
        _presenceViewerCount.value = null
        _presenceAvailableSeats.value = null
        _disposable.clear()
    }

    private fun estimatedAvailableSeats(viewerCounts: Map<String, Int>?): Int? {
        val stop = _result.value?.firstOrNull { it.name == selectedPresenceStop } ?: return null
        val routeStops = stop.timetable.order.firstOrNull()?.stops?.map { it.stop } ?: return null
        val stopIndex = routeStops.indexOf(selectedPresenceStop)
        if (viewerCounts == null || stopIndex < 0) return null
        val onboard = routeStops.take(stopIndex).fold(0) { count, stopId ->
            (if (stopId == "station") 0 else count) + viewerCounts.getOrDefault(stopId, 0)
        }
        return (45 - onboard).coerceAtLeast(0)
    }

    override fun onCleared() {
        stop()
    }

    private companion object {
        const val ALTERNATIVE_THRESHOLD_MINUTES = 20
        const val MINUTES_PER_DAY = 24 * 60L
        val PRESENCE_STOP_IDS = listOf(
            "dormitory_o",
            "shuttlecock_o",
            "station",
            "terminal",
            "jungang_stn",
            "shuttlecock_i",
        )
    }
}
