package app.kobuggi.hyuabot.ui.common.coachmark

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupOverlayCoordinatorTest {
    @Test
    fun `coachmark wins over all startup candidates`() {
        assertEquals(StartupOverlay.COACHMARK, chooseStartupOverlay(true, true, true, true))
    }

    @Test
    fun `language wins over birthday and review`() {
        assertEquals(StartupOverlay.LANGUAGE, chooseStartupOverlay(false, true, true, true))
    }

    @Test
    fun `birthday precedes review`() {
        assertEquals(StartupOverlay.BIRTHDAY, chooseStartupOverlay(false, false, true, true))
    }

    @Test
    fun `review is selected only when it is eligible`() {
        assertEquals(StartupOverlay.REVIEW, chooseStartupOverlay(false, false, false, true))
        assertEquals(StartupOverlay.NONE, chooseStartupOverlay(false, false, false, false))
    }
}
