package app.kobuggi.hyuabot.ui.bus.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.rest.APIService
import app.kobuggi.hyuabot.ui.shuttle.timetable.TimetableItem
import app.kobuggi.hyuabot.util.TimetableUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    private val _stopID = MutableLiveData(-1)
    private val _routeID = MutableLiveData(-1)
    private val _weekdaysTimetable = MutableLiveData<List<String>>()
    private val _saturdaysTimetable = MutableLiveData<List<String>>()
    private val _sundaysTimetable = MutableLiveData<List<String>>()
    private val _isLoading = MutableLiveData(false)

    val weekdaysTimetable get() = _weekdaysTimetable
    val saturdaysTimetable get() = _saturdaysTimetable
    val sundaysTimetable get() = _sundaysTimetable
    val isLoading get() = _isLoading

    fun setTimetableData(routeID: Int, stopID: Int) {
        _routeID.value = routeID
        _stopID.value = stopID
    }

    fun fetchTimetable() {
        _isLoading.value = true
        viewModelScope.launch {
            val timetable = service.busTimetableItem(_routeID.value!!, _stopID.value!!).body()
            _weekdaysTimetable.value = timetable?.weekdays?.map { TimetableUtil.add24Hour(it) }?.sorted()
            _saturdaysTimetable.value = timetable?.saturday?.map { TimetableUtil.add24Hour(it) }?.sorted()
            _sundaysTimetable.value = timetable?.sunday?.map { TimetableUtil.add24Hour(it) }?.sorted()
        }
        _isLoading.value = false
    }
}