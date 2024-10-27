package app.kobuggi.hyuabot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val userPreferencesRepository: UserPreferencesRepository): ViewModel() {
    val theme = userPreferencesRepository.theme.asLiveData()
}
