package app.kobuggi.hyuabot.ui.bus.realtime

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class RealtimeUpdatedAtTest {
    @Test fun returnsLatestTimestamp() {
        val earlier = Instant.parse("2026-10-18T02:00:00Z")
        val later = Instant.parse("2026-10-18T03:00:00Z")

        assertEquals(later, maxRealtimeUpdatedAt(listOf(earlier, later)))
    }

    @Test fun ignoresNullsAndReturnsNullWhenNoTimestampsExist() {
        val timestamp = Instant.parse("2026-10-18T03:00:00Z")

        assertEquals(timestamp, maxRealtimeUpdatedAt(listOf(null, timestamp, null)))
        assertEquals(null, maxRealtimeUpdatedAt(emptyList()))
        assertEquals(null, maxRealtimeUpdatedAt(listOf(null, null)))
    }
}
