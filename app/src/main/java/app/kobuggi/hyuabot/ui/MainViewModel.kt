package app.kobuggi.hyuabot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.service.database.AppDatabase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: AppDatabase,
): ViewModel() {
    /** 시스템 설정(Android 13+)에서 언어가 바뀐 경우에도 언어별 연락처·학사일정 캐시를 비운다. */
    fun clearLocalizedCaches(onDone: () -> Unit) {
        viewModelScope.launch {
            database.contactDao().deleteAll()
            database.calendarDao().deleteAll()
            userPreferencesRepository.clearContactVersion()
            userPreferencesRepository.clearCalendarVersion()
            onDone()
        }
    }

    suspend fun startScreen(): String = userPreferencesRepository.startScreen.first()
    val theme = userPreferencesRepository.theme.asLiveData()
}
