package app.kobuggi.hyuabot.ui.shuttle.realtime

import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShuttleRequestSelectionTest {
    private val outbound = ShuttleRequestSelection("dormitory_o", true, true, true,
        HomeSubwayTransferDestination.SEOUL, ShuttleAlternativeDisplayMode.AUTOMATIC)

    @Test
    fun `only visible connections request transfer data`() {
        assertEquals(listOf("K449" to "up", "K450" to "up"), outbound.subwayPairs())
        assertTrue(outbound.needsBus)
        listOf("station", "terminal", "jungang_stn", "shuttlecock_i").forEach { stop ->
            val selection = outbound.copy(stop = stop)
            assertTrue(selection.subwayPairs().isEmpty())
            assertFalse(selection.needsBus)
        }
        assertTrue(outbound.copy(showSubway = false).subwayPairs().isEmpty())
        assertTrue(outbound.copy(byDestination = false).subwayPairs().isEmpty())
        assertFalse(outbound.copy(byDestination = false).needsBus)
        assertFalse(outbound.copy(showBus = false).needsBus)
    }

    @Test
    fun `hidden alternatives and other stop alternatives are absent`() {
        assertTrue(outbound.copy(alternatives = ShuttleAlternativeDisplayMode.HIDDEN).alternativePairs().isEmpty())
        assertEquals(setOf(216000068 to 216000138), outbound.copy(stop = "station").alternativePairs())
        assertEquals(setOf(216000068 to 216000379, 216000016 to 216000152),
            outbound.copy(stop = "shuttlecock_o").alternativePairs())
        assertTrue(outbound.copy(stop = "shuttlecock_i").alternativePairs().isEmpty())
    }
}
