package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.BusStopCoordinatesQuery
import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.type.BusRouteStopInput
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.doNotStore
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.apollo.api.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BusRealtimeViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<BusRealtimePageQuery.Bus>>()
    private val _notices = MutableLiveData<List<BusRealtimePageQuery.Notice1>>()

    private val _disposable = CompositeDisposable()
    private val _selectedStopID = MutableLiveData<Int?>(null)
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _showSecondaryEta = MutableLiveData(true)
    private val _seoulTarget = MutableLiveData(BusSeoulTargetStop.GANGNAM)
    private val _seoulFirstStopID = MutableLiveData<Int?>(null)
    private val _seoulSecondStopID = MutableLiveData<Int?>(null)
    private val _suwonStopID = MutableLiveData<Int?>(null)
    private val _stopCoordinates = MutableLiveData<List<BusStopCoordinatesQuery.Bus>>()
    private var coordinatesLoading = false
    private var latestRequestGeneration = 0L
    private var lastAppliedGeneration = 0L
    val stopCoordinates get() = _stopCoordinates

    val result get() = _result
    val notices get() = _notices
    val isLoading get() = _isLoading
    val selectedStopID get() = _selectedStopID
    val queryError get() = _queryError
    val showSecondaryEta get() = _showSecondaryEta
    val seoulTarget get() = _seoulTarget
    val seoulFirstStopID get() = _seoulFirstStopID
    val seoulSecondStopID get() = _seoulSecondStopID
    val suwonStopID get() = _suwonStopID

    fun initSelectedStopID() {
        viewModelScope.launch {
            userPreferencesRepository.getBusStop().collect {
                updateRequestSetting(_selectedStopID, it)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getShowBusSecondaryEta().collect {
                updateRequestSetting(_showSecondaryEta, it)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getBusSeoulTargetStop().collect {
                updateRequestSetting(_seoulTarget, BusSeoulTargetStop.from(it))
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getBusSeoulFirstStop().collect {
                updateRequestSetting(_seoulFirstStopID, it)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getBusSeoulSecondStop().collect {
                updateRequestSetting(_seoulSecondStopID, it)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getBusSuwonStop().collect {
                updateRequestSetting(_suwonStopID, it)
            }
        }
    }

    /** Saved selections usually arrive after the first poll; refetch so the shown tab is not left without data until the next tick. */
    private fun <T> updateRequestSetting(target: MutableLiveData<T>, value: T) {
        if (target.value == value) return
        target.value = value
        if (latestRequestGeneration > 0) fetchData()
    }

    fun setSelectedStopID(stopID: Int) {
        _selectedStopID.value = stopID
        viewModelScope.launch { userPreferencesRepository.setBusStop(stopID) }
    }

    fun setSeoulFirstStopID(stopRes: Int) {
        _seoulFirstStopID.value = stopRes
        viewModelScope.launch { userPreferencesRepository.setBusSeoulFirstStop(stopRes) }
    }

    fun setSeoulSecondStopID(stopRes: Int) {
        _seoulSecondStopID.value = stopRes
        viewModelScope.launch { userPreferencesRepository.setBusSeoulSecondStop(stopRes) }
    }

    fun setSuwonStopID(stopRes: Int) {
        _suwonStopID.value = stopRes
        viewModelScope.launch { userPreferencesRepository.setBusSuwonStop(stopRes) }
    }

    fun setShowSecondaryEta(show: Boolean) {
        _showSecondaryEta.value = show
        viewModelScope.launch { userPreferencesRepository.setShowBusSecondaryEta(show) }
    }

    fun setSeoulTarget(target: BusSeoulTargetStop) {
        _seoulTarget.value = target
        viewModelScope.launch { userPreferencesRepository.setBusSeoulTargetStop(target.value) }
    }

    fun fetchData() {
        fetchStopCoordinates()
        val requestGeneration = ++latestRequestGeneration
        if (_result.value == null) _isLoading.value = true
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
        val appLanguage = locale?.language ?: Locale.getDefault().language
        val language = if (appLanguage == Locale.KOREAN.language) "KOREAN" else "ENGLISH"
        val dates = BusRecentDates.sameWeekdayType(count = 4)
        val busInput = selectedBusInput(dates)
        viewModelScope.launch {
            // Notices are only shown from the first successful response, so later polls skip them.
            val response = apolloClient.query(BusRealtimePageQuery(language, busInput, includeNotices = _notices.value == null))
                .fetchPolicy(FetchPolicy.NetworkOnly)
                .doNotStore(true)
                .execute()
            // Timer ticks issue newer requests with identical inputs; keep applying a slow response unless the
            // selected stops changed or a newer response was already rendered.
            if (requestGeneration <= lastAppliedGeneration || busInput != selectedBusInput(dates)) return@launch
            lastAppliedGeneration = requestGeneration
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.bus != null) {
                _result.value = response.data?.bus
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            if (_notices.value == null) {
                // Left null on failure so the next poll requests notices again.
                response.data?.notices?.let { notices -> _notices.value = notices.flatMap { it.notices } }
            }
            _isLoading.value = false
        }
    }

    fun start() {
        if (_disposable.size() > 0) return
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

    fun stop() { _disposable.clear() }

    override fun onCleared() {
        stop()
    }

    private fun fetchStopCoordinates() {
        if (coordinatesLoading || _stopCoordinates.value != null) return
        coordinatesLoading = true
        viewModelScope.launch {
            try {
                val inputs = busLocationInputs()
                val response = apolloClient.query(BusStopCoordinatesQuery(inputs))
                    .fetchPolicy(FetchPolicy.NetworkOnly)
                    .doNotStore(true)
                    .execute()
                response.data?.bus?.takeIf { it.isNotEmpty() }?.let { _stopCoordinates.value = it }
            } finally {
                coordinatesLoading = false
            }
        }
    }

    private fun selectedBusInput(dates: List<java.time.LocalDate>): List<BusRouteStopInput> {
        val seoulRemoteStops = setOf(121000060, 121000929, 121000974, 121000970, 121000220)
        val cityStop = busStopSequence(_selectedStopID.value) ?: 216000379
        val seoulFirstStop = busStopSequence(_seoulFirstStopID.value) ?: 216000379
        val seoulSecondStop = busStopSequence(_seoulSecondStopID.value) ?: 216000719
        val suwonStop = busStopSequence(_suwonStopID.value) ?: 216000070
        val showSecondary = _showSecondaryEta.value ?: true

        fun input(route: Int, stop: Int, destinations: List<Int> = emptyList()) = BusRouteStopInput(
            route = route,
            stop = stop,
            destinationStops = destinations.takeIf { showSecondary && it.isNotEmpty() }?.let { Optional.present(it) } ?: Optional.Absent,
            limit = Optional.present(3),
            dates = Optional.present(dates),
        )

        val inputs = mutableListOf(
            input(216000068, cityStop, listOf(216000138)),
            input(216000068, 216000138, listOf(216000378)),
            input(
                216000061,
                seoulFirstStop,
                listOf(if (seoulFirstStop in seoulRemoteStops) 216000378 else (_seoulTarget.value ?: BusSeoulTargetStop.GANGNAM).stopID),
            ),
            input(
                216000104,
                suwonStop,
                listOf(if (suwonStop == 202000106) 216000141 else 202000208),
            ),
            input(
                200000015,
                suwonStop,
                listOf(if (suwonStop == 202000106) 216000141 else 202000208),
            ),
            input(216000075, 216000759, listOf(213000487)),
            input(216000075, 213000487, listOf(216000117)),
        )
        val secondSeoulDestination = if (seoulSecondStop in seoulRemoteStops) {
            216000048
        } else {
            (_seoulTarget.value ?: BusSeoulTargetStop.GANGNAM).stopID
        }
        inputs += listOf(216000043, 216000026, 216000096).map {
            input(it, seoulSecondStop, listOf(secondSeoulDestination))
        }
        return inputs
    }
}

internal fun busStopSequence(stopResource: Int?): Int? = when (stopResource) {
    R.string.bus_stop_convention -> 216000379
    R.string.bus_stop_cluster -> 216000381
    R.string.bus_stop_dormitory -> 216000383
    R.string.bus_stop_main_gate -> 216000719
    R.string.bus_stop_seocho -> 121000060
    R.string.bus_stop_gyodae -> 121000929
    R.string.bus_stop_gangnam -> 121000974
    R.string.bus_stop_yangjae -> 121000970
    R.string.bus_stop_yangjae_forest -> 121000220
    R.string.bus_stop_entrance -> 216000070
    R.string.bus_stop_suwon_station -> 202000106
    else -> null
}
