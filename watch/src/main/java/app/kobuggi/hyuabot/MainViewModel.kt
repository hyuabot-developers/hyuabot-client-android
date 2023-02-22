package app.kobuggi.hyuabot

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.data.APIService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _dormitoryArrival: MutableLiveData<List<Int>> = MutableLiveData(listOf(-1, -1, -1))
    private val _shuttlecockOutArrival: MutableLiveData<List<Int>> = MutableLiveData(listOf(-1, -1, -1))
    private val _stationArrival: MutableLiveData<List<Int>> = MutableLiveData(listOf(-1, -1, -1))
    private val _terminalArrival: MutableLiveData<List<Int>> = MutableLiveData(listOf(-1))
    private val _jungangArrival: MutableLiveData<List<Int>> = MutableLiveData(listOf(-1))
    private val _shuttlecockInArrival: MutableLiveData<List<Int>> = MutableLiveData(listOf(-1))
    private val _disposable = CompositeDisposable()
    val dormitoryArrival: MutableLiveData<List<Int>> get() = _dormitoryArrival
    val shuttlecockOutArrival: MutableLiveData<List<Int>> get() = _shuttlecockOutArrival
    val stationArrival: MutableLiveData<List<Int>> get() = _stationArrival
    val terminalArrival: MutableLiveData<List<Int>> get() = _terminalArrival
    val jungangArrival: MutableLiveData<List<Int>> get() = _jungangArrival
    val shuttlecockInArrival: MutableLiveData<List<Int>> get() = _shuttlecockInArrival

    val errorMessage: MutableLiveData<String> = MutableLiveData("")
    fun getArrivalList() {
        viewModelScope.launch {
            val apiService = APIService.getInstance()
            try {
                val response = apiService.getArrivalList()
                val dormitory = response.stop.find { it.name == "dormitory_o" }
                val dormitoryArrival = arrayListOf<Int>()
                val dormitoryToStation = dormitory?.route?.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                val dormitoryToStationArrivalList = arrayListOf<Int>()
                dormitoryToStation?.forEach {
                    dormitoryToStationArrivalList.addAll(it.arrival)
                }
                dormitoryArrival.add(dormitoryToStationArrivalList.minOrNull() ?: -1)
                val dormitoryToTerminal = dormitory?.route?.filter { it.tag == "DY"  || it.tag == "C" }
                val dormitoryToTerminalArrivalList = arrayListOf<Int>()
                dormitoryToTerminal?.forEach {
                    dormitoryToTerminalArrivalList.addAll(it.arrival)
                }
                dormitoryArrival.add(dormitoryToTerminalArrivalList.minOrNull() ?: -1)
                val dormitoryToJungang = dormitory?.route?.filter { it.tag == "DJ" }
                val dormitoryToJungangArrivalList = arrayListOf<Int>()
                dormitoryToJungang?.forEach {
                    dormitoryToJungangArrivalList.addAll(it.arrival)
                }
                dormitoryArrival.add(dormitoryToJungangArrivalList.minOrNull() ?: -1)
                _dormitoryArrival.value = dormitoryArrival

                val shuttlecockOut = response.stop.find { it.name == "shuttlecock_o" }
                val shuttlecockOutArrival = arrayListOf<Int>()
                val shuttlecockOutToStation = shuttlecockOut?.route?.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
                val shuttlecockOutToStationArrivalList = arrayListOf<Int>()
                shuttlecockOutToStation?.forEach {
                    shuttlecockOutToStationArrivalList.addAll(it.arrival)
                }
                shuttlecockOutArrival.add(shuttlecockOutToStationArrivalList.minOrNull() ?: -1)
                val shuttlecockOutToTerminal = shuttlecockOut?.route?.filter { it.tag == "DY" || it.tag == "C" }
                val shuttlecockOutToTerminalArrivalList = arrayListOf<Int>()
                shuttlecockOutToTerminal?.forEach {
                    shuttlecockOutToTerminalArrivalList.addAll(it.arrival)
                }
                shuttlecockOutArrival.add(shuttlecockOutToTerminalArrivalList.minOrNull() ?: -1)
                val shuttlecockOutToJungang = shuttlecockOut?.route?.filter { it.tag == "DJ" }
                val shuttlecockOutToJungangArrivalList = arrayListOf<Int>()
                shuttlecockOutToJungang?.forEach {
                    shuttlecockOutToJungangArrivalList.addAll(it.arrival)
                }
                shuttlecockOutArrival.add(shuttlecockOutToJungangArrivalList.minOrNull() ?: -1)
                _shuttlecockOutArrival.value = shuttlecockOutArrival

                val station = response.stop.find { it.name == "station" }
                val stationArrival = arrayListOf<Int>()
                val stationToDormitoryArrivalList = arrayListOf<Int>()
                station?.route?.forEach {
                    stationToDormitoryArrivalList.addAll(it.arrival)
                }
                stationArrival.add(stationToDormitoryArrivalList.minOrNull() ?: -1)
                val stationToJungang = station?.route?.filter { it.tag == "DJ" }
                val stationToJungangArrivalList = arrayListOf<Int>()
                stationToJungang?.forEach {
                    stationToJungangArrivalList.addAll(it.arrival)
                }
                stationArrival.add(stationToJungangArrivalList.minOrNull() ?: -1)
                val stationToTerminal = station?.route?.filter { it.tag == "C" }
                val stationToTerminalArrivalList = arrayListOf<Int>()
                stationToTerminal?.forEach {
                    stationToTerminalArrivalList.addAll(it.arrival)
                }
                stationArrival.add(stationToTerminalArrivalList.minOrNull() ?: -1)
                _stationArrival.value = stationArrival

                val jungangStation = response.stop.find { it.name == "jungang_stn" }
                val jungangArrival = arrayListOf<Int>()
                val jungangToDormitory = jungangStation?.route?.filter { it.tag == "DJ" }
                val jungangToDormitoryArrivalList = arrayListOf<Int>()
                jungangToDormitory?.forEach {
                    jungangToDormitoryArrivalList.addAll(it.arrival)
                }
                jungangArrival.add(jungangToDormitoryArrivalList.minOrNull() ?: -1)

                val terminal = response.stop.find { it.name == "terminal" }
                val terminalArrival = arrayListOf<Int>()
                val terminalToDormitoryArrivalList = arrayListOf<Int>()
                terminal?.route?.forEach {
                    terminalToDormitoryArrivalList.addAll(it.arrival)
                }
                terminalArrival.add(terminalToDormitoryArrivalList.minOrNull() ?: -1)
                _terminalArrival.value = terminalArrival

                val shuttlecockIn = response.stop.find { it.name == "shuttlecock_i" }
                val shuttlecockInArrival = arrayListOf<Int>()
                val shuttlecockInToDormitoryArrivalList = arrayListOf<Int>()
                shuttlecockIn?.route?.filter { it.name.endsWith("D") }?.forEach {
                    shuttlecockInToDormitoryArrivalList.addAll(it.arrival)
                }
                shuttlecockInArrival.add(shuttlecockInToDormitoryArrivalList.minOrNull() ?: -1)
                _shuttlecockInArrival.value = shuttlecockInArrival
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            }
        }
    }

    fun start() {
        _disposable.add(
            Observable.interval(0, 1, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getArrivalList()
                }
        )
    }

    fun stop() {
        _disposable.clear()
    }
}