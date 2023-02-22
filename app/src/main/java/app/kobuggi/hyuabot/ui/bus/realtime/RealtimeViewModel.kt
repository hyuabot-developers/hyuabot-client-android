package app.kobuggi.hyuabot.ui.bus.realtime

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.model.bus.BusStopItem
import app.kobuggi.hyuabot.service.rest.APIService
import app.kobuggi.hyuabot.ui.bus.TimetableDataItem
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class RealtimeViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    @Inject lateinit var dataStore: DataStore<Preferences>
    private val _conventionCenterBusArrivalList : MutableLiveData<BusStopItem> = MutableLiveData()
    private val _mainGateBusArrivalList : MutableLiveData<BusStopItem> = MutableLiveData()
    private val _sangnoksuBusArrivalList : MutableLiveData<BusStopItem> = MutableLiveData()
    private val _seonganBusHighSchoolArrivalList : MutableLiveData<BusStopItem> = MutableLiveData()
    private val _isLoading : MutableLiveData<Boolean> = MutableLiveData(false)
    private val _disposable = CompositeDisposable()
    private val _bookmarkIndex : MutableLiveData<Int> = MutableLiveData(-1)
    private val _busBookmarkIndex = intPreferencesKey("bus")
    private val _openTimetableEvent = MutableLiveData(TimetableDataItem(-1, "", -1))
    private val _errorMessage = MutableLiveData(false)

    val conventionCenterBusArrivalList get() = _conventionCenterBusArrivalList
    val mainGateBusArrivalList get() = _mainGateBusArrivalList
    val sangnoksuArrivalList get() = _sangnoksuBusArrivalList
    val seonganHighSchoolArrivalList get() = _seonganBusHighSchoolArrivalList
    val isLoading get() = _isLoading
    val bookmarkIndex get() = _bookmarkIndex
    val timetableEvent get() = _openTimetableEvent
    val errorMessage get() = _errorMessage


    fun fetchData() {
        _isLoading.value = true
        _errorMessage.value = false
        var fetchError: Boolean
        viewModelScope.launch {
            fetchError = fetchConventionCenterArrivalList()
            fetchError = fetchMainGateArrivalList()
            fetchError = fetchSangnoksuArrivalList()
            fetchError = fetchSeonganHighSchoolArrivalList()
            if (!fetchError) {
                _errorMessage.value = true
            }
        }
        _isLoading.value = false
    }

    private suspend fun fetchConventionCenterArrivalList(): Boolean {
        return try {
            val response = service.busStopItem(216000379)
            if (response.isSuccessful) {
                _conventionCenterBusArrivalList.value = response.body()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchMainGateArrivalList(): Boolean {
        return try {
            val response = service.busStopItem(216000719)
            if (response.isSuccessful) {
                _mainGateBusArrivalList.value = response.body()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchSangnoksuArrivalList(): Boolean {
        return try {
            val response = service.busStopItem(216000138)
            if (response.isSuccessful) {
                _sangnoksuBusArrivalList.value = response.body()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchSeonganHighSchoolArrivalList(): Boolean {
        return try {
            val response = service.busStopItem(216000070)
            if (response.isSuccessful) {
                _seonganBusHighSchoolArrivalList.value = response.body()
            }
            true
        } catch (e: Exception) {
            false
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
                preferences[_busBookmarkIndex] ?: -1
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
                    it.remove(_busBookmarkIndex)
                    _bookmarkIndex.value = -1
                } else {
                    it[_busBookmarkIndex] = index
                    _bookmarkIndex.value = index
                }
            }
        }
    }

    fun openTimetable(routeID: Int, routeName: String, startStopID: Int) {
        _openTimetableEvent.value = TimetableDataItem(routeID, routeName, startStopID)
    }

    override fun onCleared() {
        super.onCleared()
        _disposable.clear()
    }
}