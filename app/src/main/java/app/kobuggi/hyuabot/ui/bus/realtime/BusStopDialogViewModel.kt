package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusStopDialogViewModel @Inject constructor(private val userPreferencesRepository: UserPreferencesRepository): ViewModel() {
    init { getBusStop() }
    val selectedStopID = MutableLiveData<Int?>(null)

    fun setBusStop(busStopID: Int) {
        viewModelScope.launch { userPreferencesRepository.setBusStop(busStopID) }
    }

    private fun getBusStop() {
        viewModelScope.launch {
            userPreferencesRepository.getBusStop().collect {
                selectedStopID.value = it
            }
        }
    }
}
