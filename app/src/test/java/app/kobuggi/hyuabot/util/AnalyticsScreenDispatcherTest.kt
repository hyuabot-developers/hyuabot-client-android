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

    @Test
    fun `rotation restores home without another screen view and navigation still logs`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        dispatcher.onResumed()
        dispatcher.onPaused()

        val restored = AnalyticsScreenDispatcher(screens::add)
        restored.restoreLastLoggedScreen(dispatcher.lastLoggedScreen)
        restored.onDestinationChanged(AnalyticsScreen.HOME)
        restored.onResumed()
        restored.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.HOME), screens)

        restored.onDestinationChanged(AnalyticsScreen.BUS_REALTIME)
        restored.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.HOME, AnalyticsScreen.BUS_REALTIME, AnalyticsScreen.HOME), screens)
    }

    @Test
    fun `new destination during recreation still logs after resume`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)
        dispatcher.restoreLastLoggedScreen(AnalyticsScreen.HOME)
        dispatcher.onDestinationChanged(AnalyticsScreen.MAP)
        dispatcher.onResumed()
        assertEquals(listOf(AnalyticsScreen.MAP), screens)
    }
}
