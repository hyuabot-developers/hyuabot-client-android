package app.kobuggi.hyuabot.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import com.apollographql.apollo.ApolloClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class MainViewModel(private val apolloClient: ApolloClient, private val stopID: String): ViewModel() {
    private val _result = MutableLiveData<List<ShuttleRealtimePageQuery.Timetable>>()
    private val _firstItem = MutableLiveData<ShuttleRealtimePageQuery.Timetable?>(null)
    private val _secondItem = MutableLiveData<ShuttleRealtimePageQuery.Timetable?>(null)
    private val _thirdItem = MutableLiveData<ShuttleRealtimePageQuery.Timetable?>(null)
    private val _fourthItem = MutableLiveData<ShuttleRealtimePageQuery.Timetable?>(null)
    private val _disposable = CompositeDisposable()

    val result: LiveData<List<ShuttleRealtimePageQuery.Timetable>> get() = _result
    val firstItem: LiveData<ShuttleRealtimePageQuery.Timetable?> get() = _firstItem
    val secondItem: LiveData<ShuttleRealtimePageQuery.Timetable?> get() = _secondItem
    val thirdItem: LiveData<ShuttleRealtimePageQuery.Timetable?> get() = _thirdItem
    val fourthItem: LiveData<ShuttleRealtimePageQuery.Timetable?> get() = _fourthItem

    private fun fetchData() {
        val now = LocalDateTime.now()
        val currentTime = DateTimeFormatter.ofPattern("HH:mm").format(now)
        val currentDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(now)
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleRealtimePageQuery(getStop(), currentTime, currentDateTime)).execute()
            if (response.data?.shuttle?.timetable != null) {
                _result.value = response.data?.shuttle?.timetable?.filter { it.time > currentTime }
                if (stopID == "기숙사") {
                    _firstItem.value = _result.value!!.firstOrNull { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                    _secondItem.value = _result.value!!.firstOrNull { it.tag == "DY" || it.tag == "C" }
                    _thirdItem.value = _result.value!!.firstOrNull { it.tag == "DJ" }
                    _fourthItem.value = null
                } else if (stopID == "셔틀콕") {
                    _firstItem.value = _result.value!!.firstOrNull { it.stop == "shuttlecock_o" && (it.tag == "DH" || it.tag == "DJ" || it.tag == "C") }
                    _secondItem.value = _result.value!!.firstOrNull { it.stop == "shuttlecock_o" && (it.tag == "DY" || it.tag == "C") }
                    _thirdItem.value = _result.value!!.firstOrNull { it.stop == "shuttlecock_o" && it.tag == "DJ" }
                    _fourthItem.value = _result.value!!.firstOrNull { it.stop == "shuttlecock_i" && it.route.endsWith("D") }
                } else if (stopID == "한대앞") {
                    _firstItem.value = _result.value!!.firstOrNull()
                    _secondItem.value = _result.value!!.firstOrNull { it.tag == "C" }
                    _thirdItem.value = _result.value!!.firstOrNull { it.tag == "DJ" }
                    _fourthItem.value = null
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
