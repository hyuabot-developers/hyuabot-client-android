package app.kobuggi.hyuabot.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.service.database.AppDatabase
import app.kobuggi.hyuabot.service.preferences.StartScreen
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val database: AppDatabase,
) : ViewModel() {
    val campusID: StateFlow<Int> = repository.campusID.stateIn(viewModelScope, SharingStarted.Eagerly, 2)
    val theme: StateFlow<String?> = repository.theme.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val startScreen: StateFlow<String> = repository.startScreen
        .stateIn(viewModelScope, SharingStarted.Eagerly, StartScreen.SHUTTLE)
    val analyticsConsent: StateFlow<Boolean> = repository.analyticsConsent.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setCampusID(id: Int) = viewModelScope.launch { repository.setCampusID(id) }
    fun setTheme(theme: String?) = viewModelScope.launch { repository.setTheme(theme) }
    fun setStartScreen(screen: String) = viewModelScope.launch { repository.setStartScreen(screen) }
    fun setAnalyticsConsent(enabled: Boolean) = viewModelScope.launch { repository.setAnalyticsConsent(enabled) }

    /** 언어를 바꾸면 언어별로 받아 둔 연락처·학사일정 캐시를 비운다. */
    fun clearLocalizedCaches(onDone: () -> Unit) {
        viewModelScope.launch {
            database.contactDao().deleteAll()
            database.calendarDao().deleteAll()
            repository.clearContactVersion()
            repository.clearCalendarVersion()
            onDone()
        }
    }
}
