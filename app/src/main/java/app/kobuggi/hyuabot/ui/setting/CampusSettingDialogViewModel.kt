package app.kobuggi.hyuabot.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CampusSettingDialogViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val campusID = userPreferencesRepository.campusID.asLiveData()

    suspend fun setCampusID(campusID: Int) {
        userPreferencesRepository.setCampusID(campusID)
    }
}
