package app.kobuggi.hyuabot.ui.shuttle.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.rest.APIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    private val _stopID = MutableLiveData(-1)
    private val _destination = MutableLiveData(-1)
    private val _weekdaysTimetable = MutableLiveData<List<TimetableItem>>()
    private val _weekendsTimetable = MutableLiveData<List<TimetableItem>>()
    private val _isLoading = MutableLiveData(false)

    val stopID get() = _stopID
    val weekdaysTimetable get() = _weekdaysTimetable
    val weekendsTimetable get() = _weekendsTimetable
    val isLoading get() = _isLoading

    fun setTimetableData(stopID: Int, destinationID: Int) {
        _stopID.value = stopID
        _destination.value = destinationID
    }

    fun fetchTimetable() {
        _isLoading.value = true
        viewModelScope.launch {
            val routes = when (_stopID.value) {
                R.string.dormitory_o -> {
                    when (_destination.value) {
                        R.string.shuttle_bound_for_station -> service.entireShuttleTimetable()
                            .body()?.stop?.find { it.name == "dormitory_o" }?.route?.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                        R.string.shuttle_bound_for_terminal -> service.entireShuttleTimetable()
                            .body()?.stop?.find { it.name == "dormitory_o" }?.route?.filter { it.tag == "DY" || it.tag == "C" }
                        R.string.shuttle_bound_for_jungang_stn -> service.entireShuttleTimetable()
                            .body()?.stop?.find { it.name == "dormitory_o" }?.route?.filter { it.tag == "DJ" }
                        else -> listOf()
                    }
                }
                R.string.shuttlecock_o -> {
                    when (_destination.value) {
                        R.string.shuttle_bound_for_station -> service.entireShuttleTimetable().body()?.
                            stop?.find { it.name == "shuttlecock_o" }?.
                            route?.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                        R.string.shuttle_bound_for_terminal -> service.entireShuttleTimetable().body()?.
                            stop?.find { it.name == "shuttlecock_o" }?.
                            route?.filter { it.tag == "DY" || it.tag == "C" }
                        R.string.shuttle_bound_for_jungang_stn -> service.entireShuttleTimetable().body()?.
                            stop?.find { it.name == "shuttlecock_o" }?.
                            route?.filter { it.tag == "DJ" }
                        else -> null
                    }
                }
                R.string.station -> {
                    when (_destination.value) {
                        R.string.shuttle_bound_for_campus -> service.entireShuttleTimetable().body()?.
                            stop?.find { it.name == "station" }?.route
                        R.string.shuttle_bound_for_terminal -> service.entireShuttleTimetable().body()?.
                            stop?.find { it.name == "station" }?.
                            route?.filter { it.tag == "C" }
                        R.string.shuttle_bound_for_jungang_stn -> service.entireShuttleTimetable().body()?.
                            stop?.find { it.name == "station" }?.
                            route?.filter { it.tag == "DJ" }
                        else -> null
                    }
                }
                R.string.terminal -> service.entireShuttleTimetable().body()?.stop?.find { it.name == "terminal" }?.route
                R.string.jungang_stn -> service.entireShuttleTimetable().body()?.stop?.find { it.name == "jungang_stn" }?.route
                R.string.shuttlecock_i -> service.entireShuttleTimetable().body()?.stop?.
                    find { it.name == "shuttlecock_i" }?.route?.filter { it.name.endsWith("D") }
                else -> null
            }
            val weekdaysTimetable = arrayListOf<TimetableItem>()
            val weekendsTimetable = arrayListOf<TimetableItem>()
            if (routes != null) {
                for (route in routes) {
                    for (timetableItem in route.weekdays) {
                        weekdaysTimetable.add(TimetableItem(route.tag, route.name, timetableItem))
                    }
                    for (timetableItem in route.weekends) {
                        weekendsTimetable.add(TimetableItem(route.tag, route.name, timetableItem))
                    }
                }
            }
            _weekdaysTimetable.value = weekdaysTimetable.sortedBy { it.departureTime }
            _weekendsTimetable.value = weekendsTimetable.sortedBy { it.departureTime }
        }
        _isLoading.value = false
    }
}