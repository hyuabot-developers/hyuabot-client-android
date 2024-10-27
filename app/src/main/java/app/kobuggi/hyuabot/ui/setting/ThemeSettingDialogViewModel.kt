package app.kobuggi.hyuabot.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSettingDialogViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    fun setDarkMode(theme: Boolean) {
        if (theme) {
            viewModelScope.launch { userPreferencesRepository.setTheme("dark") }
        } else {
            viewModelScope.launch { userPreferencesRepository.setTheme("light") }
        }
    }

    fun setDarkModeSystem() {
        viewModelScope.launch { userPreferencesRepository.setTheme(null) }
    }
}
