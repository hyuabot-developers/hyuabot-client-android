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
        restored.restoreLastLoggedScreen(AnalyticsScreen.HOME)
        restored.onDestinationChanged(AnalyticsScreen.HOME)
        restored.onResumed()
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
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.MAP, AnalyticsScreen.HOME), screens)
    }

    @Test
    fun `return from an unmapped destination logs home again`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)
        dispatcher.onResumed()
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        dispatcher.onDestinationChanged(null)
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.HOME, AnalyticsScreen.HOME), screens)
    }

    @Test
    fun `restoration suppresses only the first dispatch`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)
        dispatcher.restoreLastLoggedScreen(AnalyticsScreen.HOME)
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        dispatcher.onResumed()
        assertEquals(emptyList<AnalyticsScreen>(), screens)

        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.HOME), screens)
        dispatcher.onDestinationChanged(null)
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.HOME, AnalyticsScreen.HOME), screens)
    }

    @Test
    fun `restoring an unmapped destination does not suppress the next home visit`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)
        dispatcher.restoreLastLoggedScreen(AnalyticsScreen.HOME)
        dispatcher.onDestinationChanged(null)
        dispatcher.onResumed()
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.HOME), screens)
    }

    @Test
    fun `unmapped destination clears a pending screen before resume`() {
        val screens = mutableListOf<AnalyticsScreen>()
        val dispatcher = AnalyticsScreenDispatcher(screens::add)
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        dispatcher.onDestinationChanged(null)
        dispatcher.onResumed()
        assertEquals(emptyList<AnalyticsScreen>(), screens)
        dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
        assertEquals(listOf(AnalyticsScreen.HOME), screens)
    }
}
