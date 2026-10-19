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
    private var restoredScreen: AnalyticsScreen? = null

    /** Suppresses only the first dispatch when it matches the recreated screen. */
    fun restoreLastLoggedScreen(screen: AnalyticsScreen?) {
        restoredScreen = screen
    }

    fun onDestinationChanged(screen: AnalyticsScreen?) {
        if (screen == null) {
            restoredScreen = null
            pendingScreen = null
            return
        }
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
        val restored = restoredScreen
        restoredScreen = null
        if (restored == screen) return
        logScreen(screen)
    }
}
