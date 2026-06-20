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
import app.kobuggi.hyuabot.util.QueryError
import app.kobuggi.hyuabot.util.currentShuttleWeekday
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
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ShuttleRealtimeViewModel @Inject constructor(
    val userPreferencesRepository: UserPreferencesRepository,
    private val apolloClient: ApolloClient
): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _showDepartureTime = MutableLiveData(false)
    private val _showByDestination = MutableLiveData(false)
    private val _showRemainingTime = MutableLiveData(true)
    private val _result = MutableLiveData<List<ShuttleRealtimePageQuery.Stop>>()
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

    val result get() = _result
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

    fun setForceShowBusAlternative(show: Boolean) {
        _forceShowBusAlternative.value = show
    }
    val latestShuttleResult = combine(result.asFlow(), showByDestination.asFlow()) { result, showByDestination ->
        ShuttleTabData(result, showByDestination)
    }.onStart { emit(ShuttleTabData(listOf(), false)) }.asLiveData()


    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
        val language = if (locale == null) {
            "KOREAN"
        } else {
            when (locale.language) {
                "ko" -> "KOREAN"
                "en", "ja", "zh" -> "ENGLISH"
                else -> "KOREAN"
            }
        }
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleRealtimePageQuery(
                language,
                Optional.present(LocalTime.now()),
                currentShuttleWeekday()
            )).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.shuttle?.stops != null) {
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

    fun setShowDepartureTime(isVisible: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowShuttleDepartureTime(isVisible) }
    }

    fun setShowByDestination(isVisible: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowShuttleByDestination(isVisible) }
    }

    fun stop() { _disposable.clear() }

    override fun onCleared() {
        stop()
    }
}
