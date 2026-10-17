package app.kobuggi.hyuabot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class LastRunJudgeTest {
    private fun schedule(key: String, route: String, stop: String, time: LocalTime) =
        BusDeparture(key, route, stop, isRealtime = false, minutes = null, arrivalTime = time)

    private fun realtime(key: String, route: String, stop: String, minutes: Int) =
        BusDeparture(key, route, stop, isRealtime = true, minutes = minutes, arrivalTime = null)

    @Test
    fun realtimeFirstOnTieSoScheduleItemIsLast() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 9, 50)
            val r = realtime("R", "1", "S", 10) // 10:00
            val s = schedule("S", "1", "S", LocalTime.of(10, 0))
            val sorted = LastRunJudge.busSorted(listOf(s, r), now).map { it.key }
            assertEquals(listOf("R", "S"), sorted)
            assertEquals(setOf<Any>("S"), LastRunJudge.busLastRunKeys(listOf(s, r), now, BusContext.DETAIL))
        }
    }

    @Test
    fun lastRunAtIndexThreeIsExcludedForTopThree() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 9, 0)
            val four = listOf(
                schedule("a", "1", "S", LocalTime.of(9, 10)),
                schedule("b", "1", "S", LocalTime.of(9, 20)),
                schedule("c", "1", "S", LocalTime.of(9, 30)),
                schedule("d", "1", "S", LocalTime.of(9, 40)),
            )
            assertEquals(emptySet<Any>(), LastRunJudge.busLastRunKeys(four, now, BusContext.DETAIL, topN = 3))
            val three = four.take(3)
            assertEquals(setOf<Any>("c"), LastRunJudge.busLastRunKeys(three, now, BusContext.DETAIL, topN = 3))
        }
    }

    @Test
    fun homeContextNeverProducesLastRun() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 9, 0)
            val list = listOf(schedule("a", "1", "S", LocalTime.of(9, 10)))
            assertEquals(emptySet<Any>(), LastRunJudge.busLastRunKeys(list, now, BusContext.HOME))
        }
    }

    @Test
    fun realtimeCrossingMidnightSortsAfterLateNightSchedule() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 23, 58)
            val before = schedule("before", "1", "S", LocalTime.of(23, 59))
            val crossing = realtime("crossing", "1", "S", 5) // 00:03 on the next day
            val after = schedule("after", "1", "S", LocalTime.of(0, 10))
            val sorted = LastRunJudge.busSorted(listOf(after, crossing, before), now).map { it.key }
            assertEquals(listOf("before", "crossing", "after"), sorted)
            assertEquals(setOf<Any>("after"), LastRunJudge.busLastRunKeys(listOf(after, crossing, before), now, BusContext.DETAIL))
        }
    }

    @Test
    fun itemsWithoutMinutesOrArrivalTimeAreDropped() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 9, 0)
            val empty = BusDeparture("x", "1", "S", isRealtime = true, minutes = null, arrivalTime = null)
            val kept = schedule("k", "1", "S", LocalTime.of(9, 5))
            assertEquals(listOf("k"), LastRunJudge.busSorted(listOf(empty, kept), now).map { it.key })
        }
    }

    @Test
    fun lastRunIsEvaluatedPerRouteAndStop() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 9, 0)
            val list = listOf(
                schedule("r1-a", "1", "S", LocalTime.of(9, 10)),
                schedule("r1-b", "1", "S", LocalTime.of(9, 20)),
                schedule("r2-a", "2", "S", LocalTime.of(9, 5)),
                schedule("r2-b", "2", "T", LocalTime.of(9, 15)),
            )
            assertEquals(
                setOf<Any>("r1-b", "r2-a", "r2-b"),
                LastRunJudge.busLastRunKeys(list, now, BusContext.DETAIL),
            )
        }
    }

    @Test
    fun pastRealtimeArrivalDoesNotSortAheadOfLaterScheduleAt0500() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 5, 0)
            val past = realtime("past", "1", "S", -61) // 03:59 by wall clock, already gone
            val later = schedule("later", "1", "S", LocalTime.of(5, 10))
            assertEquals(listOf("past", "later"), LastRunJudge.busSorted(listOf(later, past), now).map { it.key })
            assertEquals(setOf<Any>("later"), LastRunJudge.busLastRunKeys(listOf(later, past), now, BusContext.DETAIL))
        }
    }

    @Test
    fun realtimeCrossingFourAmSortsAfterScheduledBeforeFour() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 3, 50)
            val rt = realtime("rt", "1", "S", 20) // 04:10 service time
            val sched = schedule("sched", "1", "S", LocalTime.of(3, 55))
            assertEquals(listOf("sched", "rt"), LastRunJudge.busSorted(listOf(rt, sched), now).map { it.key })
        }
    }

    @Test
    fun realtimeTieWithScheduledAtSameSecondSortsRealtimeFirst() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 10, 0)
            val rt = realtime("rt", "1", "S", 0) // 10:00
            val sched = schedule("sched", "1", "S", LocalTime.of(10, 0))
            assertEquals(listOf("rt", "sched"), LastRunJudge.busSorted(listOf(sched, rt), now).map { it.key })
        }
    }

    @Test
    fun subwayIsLastRunOnlyWhenTrue() {
        assertTrue(LastRunJudge.subwayIsLastRun(true))
        assertEquals(false, LastRunJudge.subwayIsLastRun(false))
        assertEquals(false, LastRunJudge.subwayIsLastRun(null))
    }
}
