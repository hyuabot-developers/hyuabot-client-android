package app.kobuggi.hyuabot.ui.shuttle.realtime

import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShuttleRequestSelectionTest {
    private val selection = ShuttleRequestSelection(
        byDestination = true,
        showBus = true,
        showSubway = true,
        subwayDestination = HomeSubwayTransferDestination.SEOUL,
        alternatives = ShuttleAlternativeDisplayMode.AUTOMATIC,
    )

    @Test
    fun `transfer request is independent of the selected stop`() {
        assertEquals(listOf("K449" to "up", "K450" to "up"), selection.subwayPairs())
        assertTrue(selection.needsBus)
        assertTrue(selection.copy(showSubway = false).subwayPairs().isEmpty())
        assertTrue(selection.copy(byDestination = false).subwayPairs().isEmpty())
        assertFalse(selection.copy(byDestination = false).needsBus)
        assertFalse(selection.copy(showBus = false).needsBus)
    }

    @Test
    fun `all shuttle stop alternatives are requested together`() {
        assertTrue(selection.copy(alternatives = ShuttleAlternativeDisplayMode.HIDDEN).alternativePairs().isEmpty())
        assertEquals(12, selection.alternativePairs().size)
        assertTrue(selection.alternativePairs().contains(216000068 to 216000138))
        assertTrue(selection.alternativePairs().contains(216000068 to 216000379))
    }
}
