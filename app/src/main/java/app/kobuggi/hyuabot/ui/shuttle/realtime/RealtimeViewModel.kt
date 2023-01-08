package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.model.bus.BusStopRouteItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListStopItem
import app.kobuggi.hyuabot.model.subway.SubwayStationResponse
import app.kobuggi.hyuabot.service.preference.DataStoreModule
import app.kobuggi.hyuabot.service.rest.APIService
import app.kobuggi.hyuabot.ui.shuttle.TimetableDataItem
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class RealtimeViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    @Inject lateinit var dataStore: DataStore<Preferences>
    private val _shuttleArrivalList : MutableLiveData<List<ArrivalListStopItem>> =
        MutableLiveData(arrayListOf())
    private val _k251ArrivalList : MutableLiveData<SubwayStationResponse> = MutableLiveData()
    private val _suwonArrivalList : MutableLiveData<BusStopRouteItem> = MutableLiveData()
    private val _toGwangmyeongArrivalList : MutableLiveData<BusStopRouteItem> = MutableLiveData()
    private val _fromGwangmyeongArrivalList : MutableLiveData<BusStopRouteItem> =
        MutableLiveData()
    private val _sangnoksuArrivalList : MutableLiveData<BusStopRouteItem> = MutableLiveData()
    private val _isLoading : MutableLiveData<Boolean> = MutableLiveData(false)
    private val _disposable = CompositeDisposable()
    private val _bookmarkIndex : MutableLiveData<Int> = MutableLiveData(-1)
    private val _shuttleBookmarkIndex = intPreferencesKey("shuttle")
    private val _shuttleStopInformationEvent = MutableLiveData(-1)
    private val _openTimetableEvent = MutableLiveData(TimetableDataItem(-1, -1))

    val shuttleArrivalList get() = _shuttleArrivalList
    val k251ArrivalList get() = _k251ArrivalList
    val suwonArrivalList get() = _suwonArrivalList
    val toGwangmyeongArrivalList get() = _toGwangmyeongArrivalList
    val fromGwangmyeongArrivalList get() = _fromGwangmyeongArrivalList
    val sangnoksuArrivalList get() = _sangnoksuArrivalList
    val isLoading get() = _isLoading
    val bookmarkIndex get() = _bookmarkIndex
    val shuttleStopInformationEvent get() = _shuttleStopInformationEvent
    val openTimetableEvent get() = _openTimetableEvent

    fun fetchData() {
        _isLoading.value = true
        viewModelScope.launch {
            fetchShuttleArrivalList()
            fetchK251ArrivalList()
            fetchSuwonArrivalList()
            fetchToGwangmyeongArrivalList()
            fetchFromGwangmyeongArrivalList()
            fetchSangnoksuArrivalList()
        }
        _isLoading.value = false
    }

    private suspend fun fetchShuttleArrivalList() {
        val response = service.entireShuttleArrivalList()
        if (response.isSuccessful) {
            _shuttleArrivalList.value = response.body()?.stopList
        }
    }

    private suspend fun fetchK251ArrivalList() {
        val response = service.subwayStationItem("K251")
        if (response.isSuccessful) {
            _k251ArrivalList.value = response.body()
        }
    }

    private suspend fun fetchSuwonArrivalList() {
        val response = service.busStopItem(216000719)
        if (response.isSuccessful) {
            _suwonArrivalList.value = response.body()?.routes?.find { it.routeName == "707-1" }
        }
    }

    private suspend fun fetchToGwangmyeongArrivalList() {
        val response = service.busStopItem(216000759)
        if (response.isSuccessful) {
            _toGwangmyeongArrivalList.value = response.body()?.routes?.find { it.routeName == "50" }
        }
    }

    private suspend fun fetchFromGwangmyeongArrivalList() {
        val response = service.busStopItem(216000117)
        if (response.isSuccessful) {
            _fromGwangmyeongArrivalList.value = response.body()?.routes?.find { it.routeName == "50" }
        }
    }

    private suspend fun fetchSangnoksuArrivalList() {
        val response = service.busStopItem(216000138)
        if (response.isSuccessful) {
            _sangnoksuArrivalList.value = response.body()?.routes?.find { it.routeName == "10-1" }
        }
    }

    fun start() {
        _disposable.add(
            Observable.interval(0, 1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    try {
                        fetchData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        )
    }

    fun stop() {
        _disposable.clear()
    }

    fun getBookmark() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[_shuttleBookmarkIndex] ?: -1
            }.collect {
                _bookmarkIndex.value = it
            }
        }
    }

    fun setBookmark(index: Int) {
        Log.d("RealtimeViewModel", "setBookmark: $index")
        viewModelScope.launch {
            dataStore.edit {
                if (_bookmarkIndex.value == index) {
                    it.remove(_shuttleBookmarkIndex)
                    _bookmarkIndex.value = -1
                } else {
                    it[_shuttleBookmarkIndex] = index
                    _bookmarkIndex.value = index
                }
            }
        }
    }

    fun openShuttleStopInformation(stopID: Int) {
        _shuttleStopInformationEvent.value = stopID
    }

    fun openTimetable(stopID: Int, destination: Int) {
        _openTimetableEvent.value = TimetableDataItem(stopID, destination)
    }

    override fun onCleared() {
        super.onCleared()
        _disposable.clear()
    }
}