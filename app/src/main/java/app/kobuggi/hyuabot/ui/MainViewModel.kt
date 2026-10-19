package app.kobuggi.hyuabot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.util.AnalyticsScreen
import app.kobuggi.hyuabot.util.AnalyticsScreenDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(userPreferencesRepository: UserPreferencesRepository): ViewModel() {
    val theme = userPreferencesRepository.theme.asLiveData()
    // A new process must log its initial screen, so keep this out of saved state.
    private var lastLoggedScreen: AnalyticsScreen? = null

    fun createAnalyticsScreenDispatcher(logScreen: (AnalyticsScreen) -> Unit): AnalyticsScreenDispatcher =
        AnalyticsScreenDispatcher { screen ->
            lastLoggedScreen = screen
            logScreen(screen)
        }.apply { restoreLastLoggedScreen(lastLoggedScreen) }
}
