package app.kobuggi.hyuabot.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsScreenDispatcherTest {
    @Test
    fun `cold start screen is logged once after resume`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)

        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(emptyList<AnalyticsScreen>(), screens)

        dispatcher.onResumed()
        dispatcher.onResumed()
        assertEquals(listOf(AnalyticsScreen.HOME), screens)
    }

    @Test
    fun `latest paused destination is logged after resume`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)

        dispatcher.onResumed()
        dispatcher.onDestinationChanged(AnalyticsScreen.CAMPUS)
        dispatcher.onPaused()
        dispatcher.onDestinationChanged(AnalyticsScreen.MAP)
        dispatcher.onDestinationChanged(AnalyticsScreen.CONTACT)
        dispatcher.onResumed()

        assertEquals(listOf(AnalyticsScreen.CAMPUS, AnalyticsScreen.CONTACT), screens)
    }
}
