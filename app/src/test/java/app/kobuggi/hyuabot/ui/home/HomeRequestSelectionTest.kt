package app.kobuggi.hyuabot.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRequestSelectionTest {
    @Test
    fun `hidden and inapplicable transfers request no data`() {
        assertTrue(HomeRequestSelection(showSubway = false).subwayPairs().isEmpty())
        assertTrue(HomeRequestSelection(stop = "station", destination = "CAMPUS").subwayPairs().isEmpty())
        assertFalse(HomeRequestSelection().needsBus50)
        assertTrue(HomeRequestSelection(destination = "TERMINAL").needsBus50)
        assertFalse(HomeRequestSelection(destination = "TERMINAL", showBus50 = false).needsBus50)
        assertFalse(HomeRequestSelection(stop = "station", destination = "TERMINAL").needsBus50)
    }

    @Test
    fun `transfer destinations request only necessary stations and directions`() {
        val expected = mapOf(
            HomeSubwayTransferDestination.SEOUL to listOf("K449" to "up"),
            HomeSubwayTransferDestination.SUWON_YONGIN to listOf("K251" to "up"),
            HomeSubwayTransferDestination.OIDO to listOf("K449" to "down", "K251" to "down"),
            HomeSubwayTransferDestination.INCHEON to listOf("K449" to "down", "K251" to "down", "K258" to "down"),
            HomeSubwayTransferDestination.SOSA to listOf("K449" to "down", "K251" to "down", "S26" to "up"),
        )
        expected.forEach { (destination, pairs) ->
            assertEquals(pairs, HomeRequestSelection(subwayDestination = destination).subwayPairs())
        }
    }

    @Test
    fun `alternatives follow the selected shuttle path`() {
        assertEquals(setOf(216000068 to 216000383), HomeRequestSelection().alternativeBusPairs())
        assertEquals(setOf(216000016 to 216000152), HomeRequestSelection("shuttlecock_o", "JUNGANG").alternativeBusPairs())
        assertEquals(setOf(216000082 to 217000140, 216000102 to 217000140, 216000016 to 217000264),
            HomeRequestSelection("jungang_stn", "CAMPUS").alternativeBusPairs())
        assertTrue(HomeRequestSelection("shuttlecock_o", "STATION").alternativeBusPairs().isEmpty())
    }
}
