package app.kobuggi.hyuabot.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransitFreshnessCheckerTest {
    private val now = Instant.parse("2026-09-29T13:00:00Z")

    private fun timestamp(secondsAgo: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(now.minusSeconds(secondsAgo), ZoneId.of("Asia/Seoul"))

    @Test
    fun busBoundaryAndFutureTimestamp() {
        assertFalse(TransitFreshnessChecker.isBusStale(listOf(timestamp(119)), now))
        assertTrue(TransitFreshnessChecker.isBusStale(listOf(timestamp(120)), now))
        assertFalse(TransitFreshnessChecker.isBusStale(listOf(timestamp(-50)), now))
    }

    @Test
    fun subwayUsesTheNewestTrainAtEachStation() {
        assertFalse(TransitFreshnessChecker.isSubwayStale(listOf(timestamp(240), timestamp(179)), now))
        assertTrue(TransitFreshnessChecker.isSubwayStale(listOf(timestamp(240), timestamp(180)), now))
    }

    @Test
    fun anEmptyRealtimeListIsScheduledRatherThanStale() {
        assertFalse(TransitFreshnessChecker.isBusStale(emptyList(), now))
        assertFalse(TransitFreshnessChecker.isSubwayStale(listOf(null), now))
        assertNull(TransitFreshnessChecker.latestUpdate(emptyList()))
    }

    @Test
    fun ageUsesServerTimeAndClampsFutureValues() {
        assertEquals(2L, TransitFreshnessChecker.ageMinutes(timestamp(120).toInstant(), now))
        assertEquals(0L, TransitFreshnessChecker.ageMinutes(timestamp(-50).toInstant(), now))
    }

    @Test
    fun staleUpdatesUseTheThresholdForEachTransitMode() {
        val stations = listOf(listOf(timestamp(120)), listOf(timestamp(179)), emptyList())
        assertEquals(
            listOf(timestamp(120).toInstant(), timestamp(179).toInstant()),
            TransitFreshnessChecker.staleBusUpdates(stations, now),
        )
        assertEquals(emptyList<Instant>(), TransitFreshnessChecker.staleSubwayUpdates(stations, now))
    }
}
