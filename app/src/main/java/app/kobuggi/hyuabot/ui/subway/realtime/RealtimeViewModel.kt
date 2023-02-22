package app.kobuggi.hyuabot.ui.subway.realtime

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.model.subway.SubwayStationResponse
import app.kobuggi.hyuabot.service.rest.APIService
import app.kobuggi.hyuabot.ui.subway.TimetableDataItem
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
class RealtimeViewModel @Inject constructor(val service: APIService) : ViewModel() {
    @Inject lateinit var dataStore: DataStore<Preferences>

    private val _disposable = CompositeDisposable()
    private val _bookmarkIndex : MutableLiveData<Int> = MutableLiveData(-1)
    private val _subwayBookmarkIndex = intPreferencesKey("subway")
    private val _openTimetableEvent = MutableLiveData(TimetableDataItem("", ""))
    private val _isLoading : MutableLiveData<Boolean> = MutableLiveData(false)
    private val _errorMessage = MutableLiveData(false)


    private val _k449ArrivalList = MutableLiveData<SubwayStationResponse>()
    private val _k251ArrivalList = MutableLiveData<SubwayStationResponse>()
    private val _k456ArrivalList = MutableLiveData<SubwayStationResponse>()
    private val _k258ArrivalList = MutableLiveData<SubwayStationResponse>()
    val errorMessage get() = _errorMessage


    val k449ArrivalList get() = _k449ArrivalList
    val k251ArrivalList get() = _k251ArrivalList
    val k456ArrivalList get() = _k456ArrivalList
    val k258ArrivalList get() = _k258ArrivalList
    val bookmarkIndex get() = _bookmarkIndex
    val isLoading get() = _isLoading
    val openTimetableEvent get() = _openTimetableEvent

    private suspend fun fetchK449ArrivalList(): Boolean {
        return try {
            val response = service.subwayStationItem("K449")
            if (response.isSuccessful) {
                _k449ArrivalList.value = response.body()
            }
            true
        } catch (e: Exception){
            false
        }
    }

    private suspend fun fetchK251ArrivalList(): Boolean {
        return try {
            val response = service.subwayStationItem("K251")
            if (response.isSuccessful) {
                _k251ArrivalList.value = response.body()
            }
            true
        } catch (e: Exception){
            false
        }
    }

    private suspend fun fetchK456ArrivalList(): Boolean {
       return try {
           val response = service.subwayStationItem("K456")
           if (response.isSuccessful) {
               _k456ArrivalList.value = response.body()
           }
          true
       } catch (e: Exception){
           false
       }
    }

    private suspend fun fetchK258ArrivalList(): Boolean {
        return try {
            val response = service.subwayStationItem("K258", true)
            if (response.isSuccessful) {
                _k258ArrivalList.value = response.body()
            }
            true
        } catch (e: Exception){
            false
        }
    }

    fun fetchData() {
        _errorMessage.value = false
        var fetchError: Boolean
        viewModelScope.launch {
            fetchError = fetchK449ArrivalList()
            fetchError = fetchK251ArrivalList()
            fetchError = fetchK456ArrivalList()
            fetchError = fetchK258ArrivalList()
            if (!fetchError) {
                _errorMessage.value = true
            }
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
                preferences[_subwayBookmarkIndex] ?: -1
            }.collect {
                _bookmarkIndex.value = it
            }
        }
    }

    fun setBookmark(index: Int) {
        viewModelScope.launch {
            dataStore.edit {
                if (_bookmarkIndex.value == index) {
                    it.remove(_subwayBookmarkIndex)
                    _bookmarkIndex.value = -1
                } else {
                    it[_subwayBookmarkIndex] = index
                    _bookmarkIndex.value = index
                }
            }
        }
    }

    fun openTimetable(stationID: String, heading: String) {
        _openTimetableEvent.value = TimetableDataItem(stationID, heading)
    }

    override fun onCleared() {
        super.onCleared()
        _disposable.clear()
    }
}