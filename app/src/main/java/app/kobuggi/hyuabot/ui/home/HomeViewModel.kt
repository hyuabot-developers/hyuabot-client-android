package app.kobuggi.hyuabot.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatDelegate
import app.kobuggi.hyuabot.BusDepartureLogDialogQuery
import app.kobuggi.hyuabot.BusStopCoordinatesQuery
import app.kobuggi.hyuabot.ui.bus.realtime.busLocationInputs
import app.kobuggi.hyuabot.HomePageQuery
import app.kobuggi.hyuabot.service.ShuttlePresenceService
import app.kobuggi.hyuabot.service.alarm.ShuttleServiceNoticeScheduler
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator
import app.kobuggi.hyuabot.ui.shuttle.initialstop.ShuttleGeoCoordinate
import app.kobuggi.hyuabot.ui.shuttle.initialstop.ShuttleInitialStopRuleCandidate
import app.kobuggi.hyuabot.ui.bus.realtime.BusRecentDates
import app.kobuggi.hyuabot.type.ShuttleStopInput
import app.kobuggi.hyuabot.type.ShuttleLimitInput
import app.kobuggi.hyuabot.type.SubwayStationInput
import app.kobuggi.hyuabot.type.BusRouteStopInput
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.doNotStore
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
    private val _initialStopRules = MutableLiveData<List<ShuttleInitialStopRuleCandidate>?>(null)
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _showBus50Transfer = MutableLiveData(true)
    private val _showSubwayTransfer = MutableLiveData(true)
    private val _subwayTransferDestination = MutableLiveData(HomeSubwayTransferDestination.SEOUL)
    private val _bus50TerminalLogTimes = MutableLiveData<List<LocalTime>>(emptyList())
    private val _showPresenceStatus = MutableLiveData(true)
    private val _presenceViewerCount = MutableLiveData<Int?>(null)
    private val _presenceAvailableSeats = MutableLiveData<Int?>(null)
    private var isFetching = false
    private var pendingRefresh = false
    private var requestSelection = HomeRequestSelection()
    private var loadedSubwayLanguage: String? = null
    private var presenceJob: Job? = null
    private var selectedPresenceStop = "dormitory_o"
    private var latestPresenceViewerCounts: Map<String, Int>? = null
    private var selectedPresenceDestination: String? = null
    private var presencePreviewCount: Int? = null
    private var presencePreferenceLoaded = false
    private var presenceUpdatesStarted = false
    private var selectedHomeBusGroup: HomeBusGroup? = null
    private val _stopCoordinates = MutableLiveData<List<BusStopCoordinatesQuery.Bus>>()
    val stopCoordinates: LiveData<List<BusStopCoordinatesQuery.Bus>> get() = _stopCoordinates
    private var coordinatesLoading = false
    private var selectedHomeBusDestination = BusHomeDestination.GANGNAM

    val isLoading: LiveData<Boolean> get() = _isLoading
    val data: LiveData<HomePageQuery.Data?> get() = _data
    val initialStopRules: LiveData<List<ShuttleInitialStopRuleCandidate>?> get() = _initialStopRules
    val queryError: LiveData<QueryError?> get() = _queryError
    val showBus50Transfer: LiveData<Boolean> get() = _showBus50Transfer
    val showSubwayTransfer: LiveData<Boolean> get() = _showSubwayTransfer
    val subwayTransferDestination: LiveData<HomeSubwayTransferDestination> get() = _subwayTransferDestination
    val bus50TerminalLogTimes: LiveData<List<LocalTime>> get() = _bus50TerminalLogTimes
    val showPresenceStatus: LiveData<Boolean> get() = _showPresenceStatus
    val presenceViewerCount: LiveData<Int?> get() = _presenceViewerCount
    val presenceAvailableSeats: LiveData<Int?> get() = _presenceAvailableSeats

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
        fetchStopCoordinates()
        viewModelScope.launch {
            if (isFetching) {
                pendingRefresh = true
                return@launch
            }
            isFetching = true
            pendingRefresh = false
            val requestedSelection = requestSelection
            val requestedBusGroup = selectedHomeBusGroup
            val requestedBusDestination = selectedHomeBusDestination
            val subwayLanguage = DynamicTextTranslator.currentAppLanguageTag()
            if (loadedSubwayLanguage != subwayLanguage) {
                _data.value = null
                loadedSubwayLanguage = subwayLanguage
            }
            if (_data.value == null) _isLoading.value = true
            try {
                val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                val mealDate = if (now.hour >= 20) now.toLocalDate().plusDays(1) else now.toLocalDate()
                val response = apolloClient.query(
                    HomePageQuery(
                        language = currentNoticeLanguage(),
                        subwayLanguage = subwayLanguage,
                        after = Optional.present(LocalTime.now(ZoneId.of("Asia/Seoul"))),
                        shuttleStops = listOf(ShuttleStopInput(
                            name = requestedSelection.stop,
                            destinations = Optional.present(listOf(requestedSelection.destination)),
                            limit = ShuttleLimitInput(destination = Optional.present(100)),
                        )),
                        transferBusInput = if (requestedSelection.needsBus50) listOf(
                            BusRouteStopInput(route = 216000075, stop = 216000759, limit = Optional.present(2)),
                        ) else emptyList(),
                        subwayKeys = requestedSelection.subwayPairs().filter { it.first != "S26" }.map { (station, direction) ->
                            SubwayStationInput(
                                stationID = station,
                                direction = listOf(direction),
                                weekdays = listOf(currentSubwayWeekday(now)),
                                limit = if (station == "S26") Optional.Absent else Optional.present(12),
                            )
                        },
                        subwayTimetableKeys = requestedSelection.subwayPairs().filter { it.first == "S26" }.map { (station, direction) ->
                            SubwayStationInput(station, listOf(direction), listOf(currentSubwayWeekday(now)))
                        },
                        date = mealDate,
                        campusID = userPreferencesRepository.campusID.first(),
                        busInput = homeBusInput(),
                    )
                ).fetchPolicy(FetchPolicy.NetworkOnly).doNotStore(true).execute()

                if (requestedSelection != requestSelection || requestedBusGroup != selectedHomeBusGroup || requestedBusDestination != selectedHomeBusDestination) {
                    pendingRefresh = true
                    return@launch
                }
                if (response.data == null || response.exception != null) {
                    _initialStopRules.value = emptyList()
                    _queryError.value = QueryError.SERVER_ERROR
                } else {
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
                    _data.value = response.data
                    _bus50TerminalLogTimes.value = if (requestedSelection.needsBus50) fetchBus50TerminalLogTimes(now.toLocalDate()) else emptyList()
                    viewModelScope.launch { shuttleServiceNoticeScheduler.syncIfStale() }
                    _queryError.value = null
                }
            } catch (_: Exception) {
                _initialStopRules.value = emptyList()
                _queryError.value = QueryError.SERVER_ERROR
            } finally {
                _isLoading.value = false
                isFetching = false
                if (pendingRefresh) fetchData()
            }
        }
    }

    internal fun setRequestSelection(selection: HomeRequestSelection) {
        if (requestSelection == selection) return
        requestSelection = selection
        fetchData()
    }

    fun setHomeBusSelection(group: HomeBusGroup?, destination: BusHomeDestination) {
        selectedHomeBusGroup = group
        selectedHomeBusDestination = destination
    }

    fun invalidateInitialStopRules() {
        _initialStopRules.value = null
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

    fun setPresenceStop(stopId: String, destination: String) {
        if (selectedPresenceStop == stopId && selectedPresenceDestination == destination) return
        selectedPresenceStop = stopId
        selectedPresenceDestination = destination
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
        _presenceAvailableSeats.value = null
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
                val viewerCounts = shuttlePresenceService.viewerCounts()
                viewerCounts?.let { latestPresenceViewerCounts = it }
                _presenceAvailableSeats.value = estimatedAvailableSeats(viewerCounts ?: latestPresenceViewerCounts)
                _presenceViewerCount.value = shuttlePresenceService.heartbeat(selectedPresenceStop)
                delay(PRESENCE_REFRESH_INTERVAL_MILLIS)
            }
        }
    }

    private fun estimatedAvailableSeats(viewerCounts: Map<String, Int>?): Int? {
        val stop = _data.value?.shuttle?.stops?.firstOrNull { it.name == selectedPresenceStop } ?: return null
        val routeStops = stop.timetable.destination.firstOrNull { it.destination == selectedPresenceDestination }
            ?.entries?.firstOrNull()?.stops?.map { it.stop } ?: return null
        val stopIndex = routeStops.indexOf(selectedPresenceStop)
        if (viewerCounts == null || stopIndex < 0) return null
        val onboard = routeStops.take(stopIndex).fold(0) { count, stopId ->
            (if (stopId == "station") 0 else count) + viewerCounts.getOrDefault(stopId, 0)
        }
        return (45 - onboard).coerceAtLeast(0)
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

    private fun homeBusInput(): List<BusRouteStopInput> {
        val dates = BusRecentDates.sameWeekdayType(count = 4)
        return (homeBusPairs(selectedHomeBusGroup, selectedHomeBusDestination) + requestSelection.alternativeBusPairs()).map { (route, stop) ->
            BusRouteStopInput(
                route = route,
                stop = stop,
                destinationStops = homeBusDestinationStopIDs(route, stop),
                limit = Optional.present(3),
                dates = Optional.present(dates),
            )
        }
    }

    private fun homeBusDestinationStopIDs(route: Int, stop: Int): Optional<List<Int>> {
        // Destination travel minutes feed the destination ETA (hidden when the setting is off) and, for Seoul
        // origin groups, the row ordering; skip them when neither applies.
        if (!requestSelection.showSeoulBusStop && stop !in HOME_SEOUL_ORIGIN_STOPS) return Optional.Absent
        val destinations = when {
            route == 216000061 && stop in setOf(216000383, 216000381, 216000379) ->
                if (requestSelection.showSeoulBusStop) listOf(requestSelection.seoulBusStop) else emptyList()
            route == 216000096 && stop == 216000719 ->
                if (selectedHomeBusDestination == BusHomeDestination.UIWANG) listOf(226000042)
                else if (requestSelection.showSeoulBusStop) listOf(requestSelection.seoulBusStop) else emptyList()
            else -> homeBusDestinationStops(route, stop)
        }
        return if (destinations.isEmpty()) Optional.Absent else Optional.present(destinations)
    }

    companion object {
        private val HOME_SEOUL_ORIGIN_STOPS = setOf(121000060, 121000929, 121000974, 121000970, 121000220)

        internal fun homeBusPairsForTest(
            group: HomeBusGroup?,
            destination: BusHomeDestination,
        ): Set<Pair<Int, Int>> = homeBusPairs(group, destination)

        internal fun homeBusDestinationStopsForTest(route: Int, stop: Int): List<Int> =
            homeBusDestinationStops(route, stop)

        private fun homeBusDestinationStops(route: Int, stop: Int): List<Int> {
            val seoulStops = listOf(121000060, 121000929, 121000974, 121000970, 121000220)
            return when {
                route == 216000068 && stop in setOf(216000383, 216000381, 216000379) -> listOf(216000138)
                route == 216000061 && stop in setOf(216000383, 216000381, 216000379) -> seoulStops
                route == 216000096 && stop == 216000719 -> seoulStops + 226000042
                route == 216000026 && stop == 216000719 -> listOf(226000042)
                route == 216000043 && stop == 216000719 -> listOf(225000116)
                route in setOf(216000104, 200000015) && stop == 216000070 -> listOf(202000208)
                route in setOf(216000104, 200000015) && stop == 202000106 -> listOf(216000141)
                stop in seoulStops -> listOf(if (route == 216000061) 216000378 else 216000048)
                else -> emptyList()
            }
        }

        private fun homeBusPairs(
            group: HomeBusGroup?,
            destination: BusHomeDestination,
        ): Set<Pair<Int, Int>> {
            val pairs = linkedSetOf<Pair<Int, Int>>()
            if (group == null) return pairs
            pairs += when (group) {
                HomeBusGroup.CAMPUS -> when (destination) {
                    BusHomeDestination.SANGNOKSU -> setOf(216000068 to 216000379)
                    BusHomeDestination.GANGNAM -> setOf(216000061 to 216000379, 216000096 to 216000719)
                    BusHomeDestination.SUWON -> setOf(216000104 to 216000070, 200000015 to 216000070)
                    BusHomeDestination.UIWANG -> setOf(216000026 to 216000719, 216000096 to 216000719)
                    BusHomeDestination.GUNPO -> setOf(216000043 to 216000719)
                }
                HomeBusGroup.KITECH -> setOf(216000068 to 216000381, 216000061 to 216000381)
                HomeBusGroup.DORMITORY -> setOf(216000068 to 216000383, 216000061 to 216000383)
                HomeBusGroup.SUWON -> setOf(216000104 to 202000106, 200000015 to 202000106)
                else -> group.stopSeq?.let { stop ->
                    setOf(216000061 to stop, 216000026 to stop, 216000043 to stop, 216000096 to stop)
                }.orEmpty()
            }
            return pairs
        }

        const val PRESENCE_REFRESH_INTERVAL_MILLIS = 30_000L
    }

    private fun fetchStopCoordinates() {
        if (coordinatesLoading || _stopCoordinates.value != null) return
        coordinatesLoading = true
        viewModelScope.launch {
            try {
                val response = apolloClient.query(BusStopCoordinatesQuery(busLocationInputs()))
                    .fetchPolicy(FetchPolicy.NetworkOnly).doNotStore(true).execute()
                response.data?.bus?.takeIf { it.isNotEmpty() }?.let { _stopCoordinates.value = it }
            } finally {
                coordinatesLoading = false
            }
        }
    }

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
}
