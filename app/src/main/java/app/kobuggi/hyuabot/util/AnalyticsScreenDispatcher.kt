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

    fun onDestinationChanged(screen: AnalyticsScreen) {
        if (isResumed) {
            logScreen(screen)
        } else {
            pendingScreen = screen
        }
    }

    fun onResumed() {
        isResumed = true
        pendingScreen?.let(logScreen)
        pendingScreen = null
    }

    fun onPaused() {
        isResumed = false
    }
}
