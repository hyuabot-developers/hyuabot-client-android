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
    private val _disposable = CompositeDisposable()

    val firstItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _firstItem
    val secondItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _secondItem
    val thirdItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _thirdItem
    val fourthItem: LiveData<ShuttleRealtimePageQuery.Entry?> get() = _fourthItem
    val result: LiveData<List<ShuttleRealtimePageQuery.Entry>> get() = _result

    private fun fetchData() {
        val now = LocalDateTime.now()
        viewModelScope.launch {
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
            )).execute()
            response.data?.shuttle?.stops.let { stops ->
                if (stops == null) return@let
                if (stopID == "기숙사") {
                    val stop = stops.first { it.name == "dormitory_o" }
                    stop.timetable.destination.let { timetableByDestination ->
                        _firstItem.value = timetableByDestination.first { it.destination == "STATION" }.entries.firstOrNull()
                        _secondItem.value = timetableByDestination.first { it.destination == "TERMINAL" }.entries.firstOrNull()
                        _thirdItem.value = timetableByDestination.first { it.destination == "JUNGANG" }.entries.firstOrNull()
                    }
                    _fourthItem.value = null
                } else if (stopID == "셔틀콕") {
                    val stop1 = stops.first { it.name == "shuttlecock_o" }
                    val stop2 = stops.first { it.name == "shuttlecock_i" }
                    stop1.timetable.destination.let { timetableByDestination ->
                        _firstItem.value = timetableByDestination.first { it.destination == "STATION" }.entries.firstOrNull()
                        _secondItem.value = timetableByDestination.first { it.destination == "TERMINAL" }.entries.firstOrNull()
                        _thirdItem.value = timetableByDestination.first { it.destination == "JUNGANG" }.entries.firstOrNull()
                    }
                    stop2.timetable.destination.let { timetableByDestination ->
                        _fourthItem.value = timetableByDestination.first { it.destination == "CAMPUS" }.entries.firstOrNull()
                    }
                } else if (stopID == "한대앞") {
                    val stop = stops.first { it.name == "station" }
                    stop.timetable.destination.let { timetableByDestination ->
                        _firstItem.value = timetableByDestination.first { it.destination == "CAMPUS" }.entries.firstOrNull()
                        _secondItem.value = timetableByDestination.first { it.destination == "TERMINAL" }.entries.firstOrNull()
                        _thirdItem.value = timetableByDestination.first { it.destination == "JUNGANG" }.entries.firstOrNull()
                    }
                    _fourthItem.value = null
                } else if (stopID == "예술인") {
                    val stop = stops.first { it.name == "terminal" }
                    stop.timetable.destination.let { timetableByDestination ->
                        _result.value = timetableByDestination.first { it.destination == "CAMPUS" }.entries
                    }
                } else if (stopID == "중앙역") {
                    val stop = stops.first { it.name == "jungang_stn" }
                    stop.timetable.destination.let { timetableByDestination ->
                        _result.value = timetableByDestination.first { it.destination == "CAMPUS" }.entries
                    }
                }
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
            "기숙사" -> listOf("dormitory_o")
            "셔틀콕" -> listOf("shuttlecock_o", "shuttlecock_i")
            "한대앞" -> listOf("station")
            "예술인" -> listOf("terminal")
            "중앙역" -> listOf("jungang_stn")
            else -> listOf()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
