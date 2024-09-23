package app.kobuggi.hyuabot.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CampusSettingDialogViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    fun setCampusID(campusID: Int) {
        viewModelScope.launch { userPreferencesRepository.setCampusID(campusID) }
    }
}
