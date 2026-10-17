package app.kobuggi.hyuabot.util

import app.kobuggi.hyuabot.util.ListStatus.Ended
import app.kobuggi.hyuabot.util.ListStatus.Error
import app.kobuggi.hyuabot.util.ListStatus.Halt
import app.kobuggi.hyuabot.util.ListStatus.Normal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class OperationStatusJudgeTest {
    @Test
    fun earlyMorningDepartureOfPreviousServiceDayIsRemaining() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 1, 30)
            val remaining = ServiceClock.remainingMinutes(LocalTime.of(1, 50), now)
            assertEquals(20, remaining)
            assertEquals(Normal, judgeList(TransportMode.SHUTTLE, now, false, null, 1))
            // 01:30 is outside the [02:00, 05:00) cutoff, so BUS is not ended.
            assertEquals(Normal, judgeList(TransportMode.BUS, now, false, null, 1))
        }
    }

    @Test
    fun busAndSubwayEndedInsideCutoff() {
        inBothDefaultZones {
            for ((h, m) in listOf(2 to 0, 3 to 59, 4 to 59)) {
                val now = kst(2026, 10, 17, h, m)
                assertEquals(Ended, judgeList(TransportMode.BUS, now, false, null, 5))
                assertEquals(Ended, judgeList(TransportMode.SUBWAY, now, false, null, 5))
            }
            val fiveAm = kst(2026, 10, 17, 5, 0)
            assertEquals(Normal, judgeList(TransportMode.BUS, fiveAm, false, null, 5))
            assertEquals(Normal, judgeList(TransportMode.SUBWAY, fiveAm, false, null, 5))
        }
    }

    @Test
    fun shuttleNotAffectedByCutoff() {
        inBothDefaultZones {
            assertEquals(Normal, judgeList(TransportMode.SHUTTLE, kst(2026, 10, 17, 3, 0), false, null, 5))
        }
    }

    @Test
    fun zeroRemainingIsEndedForEveryMode() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 14, 0)
            for (mode in TransportMode.entries) {
                assertEquals(Ended, judgeList(mode, now, false, null, 0))
            }
        }
    }

    @Test
    fun imminentIsZeroToTwoMinutes() {
        assertTrue(isImminent(0))
        assertTrue(isImminent(1))
        assertTrue(isImminent(2))
        assertFalse(isImminent(3))
        assertFalse(isImminent(-1))
    }

    @Test
    fun itemPriority() {
        assertEquals(ItemStatus.STALE, judgeItem(1, isRealtime = true, isLastRun = true, isStale = true))
        assertEquals(ItemStatus.IMMINENT_LAST_RUN, judgeItem(1, isRealtime = true, isLastRun = true))
        assertEquals(ItemStatus.IMMINENT, judgeItem(1, isRealtime = true, isLastRun = false))
        assertEquals(ItemStatus.LAST_RUN, judgeItem(10, isRealtime = false, isLastRun = true))
        assertEquals(ItemStatus.REALTIME, judgeItem(10, isRealtime = true, isLastRun = false))
        assertEquals(ItemStatus.SCHEDULED, judgeItem(10, isRealtime = false, isLastRun = false))
    }

    @Test
    fun listPriority() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 10, 0)
            val today = LocalDate.of(2026, 10, 17)
            assertEquals(Error, judgeList(TransportMode.SHUTTLE, now, true, today, 0))
            assertEquals(Halt(today), judgeList(TransportMode.SHUTTLE, now, false, today, 0))
            assertEquals(Ended, judgeList(TransportMode.SHUTTLE, now, false, null, 0))
        }
    }

    @Test
    fun haltOnlyAppliesToTodayAndShuttle() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 10, 0)
            val tomorrow = LocalDate.of(2026, 10, 18)
            assertEquals(Ended, judgeList(TransportMode.SHUTTLE, now, false, tomorrow, 0))
            assertEquals(Ended, judgeList(TransportMode.BUS, now, false, LocalDate.of(2026, 10, 17), 0))
        }
    }
}
