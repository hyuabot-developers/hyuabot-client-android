package app.kobuggi.hyuabot.util

/**
 * Delays manual screen events until the Activity is resumed. Firebase rejects
 * screen views emitted from NavController callbacks during Activity.onCreate.
 */
class AnalyticsScreenDispatcher(
    private val logScreen: (AnalyticsScreen) -> Unit,
) {
    private var isResumed = false
    private var pendingScreen: AnalyticsScreen? = null
    var lastLoggedScreen: AnalyticsScreen? = null
        private set

    fun restoreLastLoggedScreen(screen: AnalyticsScreen?) {
        lastLoggedScreen = screen
    }

    fun onDestinationChanged(screen: AnalyticsScreen) {
        if (isResumed) {
            dispatch(screen)
        } else {
            pendingScreen = screen
        }
    }

    fun onResumed() {
        isResumed = true
        pendingScreen?.let(::dispatch)
        pendingScreen = null
    }

    fun onPaused() {
        isResumed = false
    }

    private fun dispatch(screen: AnalyticsScreen) {
        if (lastLoggedScreen == screen) return
        lastLoggedScreen = screen
        logScreen(screen)
    }
}
