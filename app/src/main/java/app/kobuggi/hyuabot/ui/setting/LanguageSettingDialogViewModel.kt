package app.kobuggi.hyuabot.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.service.database.AppDatabase
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSettingDialogViewModel @Inject constructor(
    private val database: AppDatabase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _localeCode = MutableLiveData<String>()
    val localeCode get() = _localeCode

    fun setLocaleCode(code: String) {
        viewModelScope.launch {
            database.contactDao().deleteAll()
            database.calendarDao().deleteAll()
            userPreferencesRepository.clearContactVersion()
            userPreferencesRepository.clearCalendarVersion()
            _localeCode.value = code
        }
    }
}
