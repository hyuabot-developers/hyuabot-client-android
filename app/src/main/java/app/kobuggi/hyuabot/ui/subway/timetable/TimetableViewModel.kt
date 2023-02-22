package app.kobuggi.hyuabot.ui.subway.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.model.subway.SubwayTimetableItemResponse
import app.kobuggi.hyuabot.service.rest.APIService
import app.kobuggi.hyuabot.util.TimetableUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    private val _stationID = MutableLiveData("")
    private val _heading = MutableLiveData("")
    private val _weekdaysTimetable = MutableLiveData<List<SubwayTimetableItemResponse>>()
    private val _weekendsTimetable = MutableLiveData<List<SubwayTimetableItemResponse>>()
    private val _isLoading = MutableLiveData(false)
    private val _errorMessage = MutableLiveData(false)


    val weekdaysTimetable get() = _weekdaysTimetable
    val weekendsTimetable get() = _weekendsTimetable
    val isLoading get() = _isLoading
    val errorMessage get() = _errorMessage


    fun setTimetableData(stationID: String, heading: String) {
        _stationID.value = stationID
        _heading.value = heading
    }

    fun fetchTimetable() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val timetable = service.subwayStationTimetable(_stationID.value!!).body()
                _weekdaysTimetable.value = if (_heading.value == "up") {
                    timetable?.weekdays?.up?.map { SubwayTimetableItemResponse(it.startStation, it.terminalStation, TimetableUtil.add24Hour(it.departureTime))}
                } else {
                    timetable?.weekdays?.down?.map { SubwayTimetableItemResponse(it.startStation, it.terminalStation, TimetableUtil.add24Hour(it.departureTime))}
                }
                _weekendsTimetable.value = if (_heading.value == "up") {
                    timetable?.weekdays?.up?.map { SubwayTimetableItemResponse(it.startStation, it.terminalStation, TimetableUtil.add24Hour(it.departureTime))}
                } else {
                    timetable?.weekdays?.down?.map { SubwayTimetableItemResponse(it.startStation, it.terminalStation, TimetableUtil.add24Hour(it.departureTime))}
                }
                _errorMessage.value = false
            } catch (e: Exception) {
                _errorMessage.value = true
            }
        }
        _isLoading.value = false
    }
}