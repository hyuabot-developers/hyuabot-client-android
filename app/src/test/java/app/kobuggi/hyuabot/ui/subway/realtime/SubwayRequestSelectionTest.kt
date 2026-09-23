package app.kobuggi.hyuabot.ui.subway.realtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubwayRequestSelectionTest {
    @Test
    fun `line tabs request only the displayed station in both directions`() {
        for ((tab, id) in listOf(0 to "K449", 1 to "K251")) {
            val key = subwayRequestKeys(tab, "weekdays").single()
            assertEquals(id, key.stationID)
            assertEquals(listOf("up", "down"), key.direction)
            assertEquals(listOf("weekdays"), key.weekdays)
            assertEquals(4, key.limit.getOrNull())
        }
    }

    @Test
    fun `transfer tab keeps both legs without hidden directions or unused station`() {
        val keys = subwayRequestKeys(2, "weekends")
        assertEquals(listOf("K449", "K251", "K258", "S26"), keys.map { it.stationID })
        assertEquals(listOf(listOf("down"), listOf("down"), listOf("down"), listOf("up")), keys.map { it.direction })
        keys.forEach { assertEquals(listOf("weekends"), it.weekdays) }
        assertEquals(4, keys[0].limit.getOrNull())
        assertEquals(4, keys[1].limit.getOrNull())
        assertNull(keys[2].limit.getOrNull())
        assertNull(keys[3].limit.getOrNull())
    }
}
