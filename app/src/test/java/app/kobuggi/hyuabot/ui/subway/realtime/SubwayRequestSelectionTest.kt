package app.kobuggi.hyuabot.ui.subway.realtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubwayRequestSelectionTest {
    @Test
    fun `request includes the data needed by every tab`() {
        val keys = subwayRequestKeys("weekends")
        assertEquals(listOf("K449", "K251", "K258", "S26"), keys.map { it.stationID })
        assertEquals(
            listOf(listOf("up", "down"), listOf("up", "down"), listOf("down"), listOf("up")),
            keys.map { it.direction },
        )
        keys.forEach { assertEquals(listOf("weekends"), it.weekdays) }
        assertEquals(4, keys[0].limit.getOrNull())
        assertEquals(4, keys[1].limit.getOrNull())
        assertNull(keys[2].limit.getOrNull())
        assertNull(keys[3].limit.getOrNull())
    }
}
