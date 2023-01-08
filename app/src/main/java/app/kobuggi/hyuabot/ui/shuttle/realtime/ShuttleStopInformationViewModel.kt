package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.service.rest.APIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShuttleStopInformationViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    private val _shuttleToStationWeekdays = MutableLiveData<List<String>>()
    private val _shuttleToStationWeekends = MutableLiveData<List<String>>()
    private val _shuttleToTerminalWeekdays = MutableLiveData<List<String>>()
    private val _shuttleToTerminalWeekends = MutableLiveData<List<String>>()
    private val _shuttleToJungangStnWeekdays = MutableLiveData<List<String>>()
    private val _shuttleToJungangStnWeekends = MutableLiveData<List<String>>()
    private val _shuttleToCampusWeekdays = MutableLiveData<List<String>>()
    private val _shuttleToCampusWeekends = MutableLiveData<List<String>>()
    private val _shuttleToDormitoryWeekdays = MutableLiveData<List<String>>()
    private val _shuttleToDormitoryWeekends = MutableLiveData<List<String>>()

    val shuttleToStationWeekdays get() = _shuttleToStationWeekdays
    val shuttleToStationWeekends get() = _shuttleToStationWeekends
    val shuttleToTerminalWeekdays get() = _shuttleToTerminalWeekdays
    val shuttleToTerminalWeekends get() = _shuttleToTerminalWeekends
    val shuttleToJungangStnWeekdays get() = _shuttleToJungangStnWeekdays
    val shuttleToJungangStnWeekends get() = _shuttleToJungangStnWeekends
    val shuttleToCampusWeekdays get() = _shuttleToCampusWeekdays
    val shuttleToCampusWeekends get() = _shuttleToCampusWeekends
    val shuttleToDormitoryWeekdays get() = _shuttleToDormitoryWeekdays
    val shuttleToDormitoryWeekends get() = _shuttleToDormitoryWeekends

    fun fetchTimetable(stopID: String) {
        val shuttleToStationWeekdays = arrayListOf<String>()
        val shuttleToStationWeekends = arrayListOf<String>()
        val shuttleToTerminalWeekdays = arrayListOf<String>()
        val shuttleToTerminalWeekends = arrayListOf<String>()
        val shuttleToJungangStnWeekdays = arrayListOf<String>()
        val shuttleToJungangStnWeekends = arrayListOf<String>()
        val shuttleToCampusWeekdays = arrayListOf<String>()
        val shuttleToCampusWeekends = arrayListOf<String>()
        val shuttleToDormitoryWeekdays = arrayListOf<String>()
        val shuttleToDormitoryWeekends = arrayListOf<String>()

        viewModelScope.launch {
            val response = service.shuttleStopItem(stopID)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    when(stopID) {
                        "dormitory_o", "shuttlecock_o" -> {
                            for (route in body.routeList) {
                                when (route.tag) {
                                    "DH" -> {
                                        shuttleToStationWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                        shuttleToStationWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                    }
                                    "DY" -> {
                                        shuttleToTerminalWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                        shuttleToTerminalWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                    }
                                    "DJ" -> {
                                        shuttleToStationWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                        shuttleToStationWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                        shuttleToJungangStnWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                        shuttleToJungangStnWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                    }
                                    "C" -> {
                                        shuttleToStationWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                        shuttleToStationWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                        shuttleToTerminalWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                        shuttleToTerminalWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                    }
                                }
                            }
                        }
                        "station" -> {
                            for (route in body.routeList) {
                                if (route.tag == "C") {
                                    shuttleToTerminalWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                    shuttleToTerminalWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                } else if (route.tag == "DJ") {
                                    shuttleToJungangStnWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                    shuttleToJungangStnWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                }
                                shuttleToCampusWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                shuttleToCampusWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                            }
                        }
                        "terminal", "jungang_stn" -> {
                            for (route in body.routeList) {
                                shuttleToCampusWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                shuttleToCampusWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                            }
                        }
                        "shuttlecock_i" -> {
                            for (route in body.routeList) {
                                if (route.name.endsWith("D")) {
                                    shuttleToDormitoryWeekdays.addAll(listOf(route.runningTime.weekdays.firstTime, route.runningTime.weekdays.lastTime))
                                    shuttleToDormitoryWeekends.addAll(listOf(route.runningTime.weekends.firstTime, route.runningTime.weekends.lastTime))
                                }
                            }
                        }
                    }
                }
                _shuttleToStationWeekdays.value = shuttleToStationWeekdays.filter { it != "" }.sorted()
                _shuttleToStationWeekends.value = shuttleToStationWeekends.filter { it != "" }.sorted()
                _shuttleToTerminalWeekdays.value = shuttleToTerminalWeekdays.filter { it != "" }.sorted()
                _shuttleToTerminalWeekends.value = shuttleToTerminalWeekends.filter { it != "" }.sorted()
                _shuttleToJungangStnWeekdays.value = shuttleToJungangStnWeekdays.filter { it != "" }.sorted()
                _shuttleToJungangStnWeekends.value = shuttleToJungangStnWeekends.filter { it != "" }.sorted()
                _shuttleToCampusWeekdays.value = shuttleToCampusWeekdays.filter { it != "" }.sorted()
                _shuttleToCampusWeekends.value = shuttleToCampusWeekends.filter { it != "" }.sorted()
                _shuttleToDormitoryWeekdays.value = shuttleToDormitoryWeekdays.filter { it != "" }.sorted()
                _shuttleToDormitoryWeekends.value = shuttleToDormitoryWeekends.filter { it != "" }.sorted()
            }
        }
    }
}