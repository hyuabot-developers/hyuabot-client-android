package app.kobuggi.hyuabot.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.type.ShuttleLimitInput
import app.kobuggi.hyuabot.type.ShuttleStopInput
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit


class MainViewModel(private val apolloClient: ApolloClient, private val stopID: String): ViewModel() {
    private val _firstItem = MutableLiveData<ShuttleRealtimePageQuery.Entry?>(null)
    private val _secondItem = MutableLiveData<ShuttleRealtimePageQuery.Entry?>(null)
    private val _thirdItem = MutableLiveData<ShuttleRealtimePageQuery.Entry?>(null)
    private val _fourthItem = MutableLiveData<ShuttleRealtimePageQuery.Entry?>(null)
    private val _result = MutableLiveData<List<ShuttleRealtimePageQuery.Entry>>(emptyList())
    private val _isLoading = MutableLiveData(true)
    private val _disposable = CompositeDisposable()

    val firstItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _firstItem
    val secondItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _secondItem
    val thirdItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _thirdItem
    val fourthItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _fourthItem
    val result: LiveData<List<ShuttleRealtimePageQuery.Entry>> get() = _result
    val isLoading: LiveData<Boolean> get() = _isLoading

    private fun fetchData() {
        val now = LocalDateTime.now()
        viewModelScope.launch {
            try {
                val response = apolloClient.query(ShuttleRealtimePageQuery(
                    stops = getStop().map {
                        ShuttleStopInput(
                            name = it,
                            limit = ShuttleLimitInput(
                                destination = Optional.present(1),
                            )
                        )
                    },
                    after = Optional.present(now.toLocalTime())
                )).fetchPolicy(FetchPolicy.NetworkOnly).execute()
                response.data?.shuttle?.stops.let { stops ->
                    if (stops == null) return@let
                    when (stopID) {
                    "dormitory" -> {
                        val stop = stops.first { it.name == "dormitory_o" }
                        stop.timetable.destination.let { timetableByDestination ->
                            _firstItem.value =
                                timetableByDestination.first { it.destination == "STATION" }.entries.firstOrNull()
                            _secondItem.value =
                                timetableByDestination.first { it.destination == "TERMINAL" }.entries.firstOrNull()
                            _thirdItem.value =
                                timetableByDestination.first { it.destination == "JUNGANG" }.entries.firstOrNull()
                        }
                        _fourthItem.value = null
                    }
                    "shuttlecock" -> {
                        val stop1 = stops.first { it.name == "shuttlecock_o" }
                        val stop2 = stops.first { it.name == "shuttlecock_i" }
                        stop1.timetable.destination.let { timetableByDestination ->
                            _firstItem.value =
                                timetableByDestination.first { it.destination == "STATION" }.entries.firstOrNull()
                            _secondItem.value =
                                timetableByDestination.first { it.destination == "TERMINAL" }.entries.firstOrNull()
                            _thirdItem.value =
                                timetableByDestination.first { it.destination == "JUNGANG" }.entries.firstOrNull()
                        }
                        stop2.timetable.destination.let { timetableByDestination ->
                            _fourthItem.value =
                                timetableByDestination.first { it.destination == "CAMPUS" }.entries.firstOrNull()
                        }
                    }
                    "station" -> {
                        val stop = stops.first { it.name == "station" }
                        stop.timetable.destination.let { timetableByDestination ->
                            _firstItem.value =
                                timetableByDestination.first { it.destination == "CAMPUS" }.entries.firstOrNull()
                            _secondItem.value =
                                timetableByDestination.first { it.destination == "TERMINAL" }.entries.firstOrNull()
                            _thirdItem.value =
                                timetableByDestination.first { it.destination == "JUNGANG" }.entries.firstOrNull()
                        }
                        _fourthItem.value = null
                    }
                    "terminal" -> {
                        val stop = stops.first { it.name == "terminal" }
                        stop.timetable.destination.let { timetableByDestination ->
                            _result.value = timetableByDestination.first { it.destination == "CAMPUS" }.entries
                        }
                    }
                    "jungang" -> {
                        val stop = stops.first { it.name == "jungang_stn" }
                        stop.timetable.destination.let { timetableByDestination ->
                            _result.value = timetableByDestination.first { it.destination == "CAMPUS" }.entries
                        }
                    }
                    }
                }
            } catch (error: Exception) {
                error.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun start() {
        _disposable.add(
            Observable.interval(0, 1, TimeUnit.MINUTES)
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

    private fun getStop(): List<String> {
        return when(stopID) {
            "dormitory" -> listOf("dormitory_o")
            "shuttlecock" -> listOf("shuttlecock_o", "shuttlecock_i")
            "station" -> listOf("station")
            "terminal" -> listOf("terminal")
            "jungang" -> listOf("jungang_stn")
            else -> listOf()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
