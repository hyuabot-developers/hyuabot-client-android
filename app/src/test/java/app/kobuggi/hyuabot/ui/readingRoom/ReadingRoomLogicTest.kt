package app.kobuggi.hyuabot.ui.readingRoom

import java.time.LocalDateTime
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReadingRoomLogicTest {
    @Test
    fun levelThresholds() {
        assertEquals(SeatLevel.Full, ReadingRoomLogic.level(0, 64))
        assertEquals(SeatLevel.Full, ReadingRoomLogic.level(-3, 64))
        assertEquals(SeatLevel.Full, ReadingRoomLogic.level(5, 0))
        assertEquals(SeatLevel.Busy, ReadingRoomLogic.level(6, 96))
        assertEquals(SeatLevel.Mid, ReadingRoomLogic.level(37, 220))
        assertEquals(SeatLevel.Mid, ReadingRoomLogic.level(29, 100))
        assertEquals(SeatLevel.Good, ReadingRoomLogic.level(30, 100))
        assertEquals(SeatLevel.Busy, ReadingRoomLogic.level(9, 100))
        assertEquals(SeatLevel.Mid, ReadingRoomLogic.level(10, 100))
    }

    @Test
    fun usageIsClamped() {
        assertEquals(0f, ReadingRoomLogic.usage(10, 0), 0f)
        assertEquals(1f, ReadingRoomLogic.usage(-5, 50), 0f)
        assertEquals(0.5f, ReadingRoomLogic.usage(50, 100), 0f)
        assertEquals(0f, ReadingRoomLogic.usage(200, 100), 0f)
        assertEquals(0, ReadingRoomLogic.shownAvailable(-4))
    }

    @Test
    fun splitsNameAndFloor() {
        assertEquals("제1열람실" to "2층", ReadingRoomLogic.splitName("제1열람실[2층]"))
        assertEquals("노상일 HOLMZ" to "4층", ReadingRoomLogic.splitName("노상일 HOLMZ[4층]"))
        assertEquals("HOLMZ 열람석" to "", ReadingRoomLogic.splitName("HOLMZ 열람석"))
    }

    @Test
    fun latestUpdateAndClock() {
        val a = ZonedDateTime.parse("2026-11-09T12:05:00Z")
        val b = ZonedDateTime.parse("2026-11-09T12:14:30Z")
        assertEquals(b, ReadingRoomLogic.latestUpdate(listOf(a, b)))
        assertNull(ReadingRoomLogic.latestUpdate(emptyList()))
        // UTC 12:14는 서울 21:14
        assertEquals("21:14", ReadingRoomLogic.clock(b))
    }

    @Test
    fun alarmTimes() {
        val now = LocalDateTime.of(2026, 11, 9, 21, 14)
        val at = ReadingRoomLogic.alarmAt(now, 3)
        assertEquals(LocalDateTime.of(2026, 11, 10, 0, 4), at)
        assertEquals("00:04", ReadingRoomLogic.clock(at))
        assertEquals(LocalDateTime.of(2026, 11, 10, 0, 14), ReadingRoomLogic.endAt(at))
    }

    @Test
    fun restoreDropsPastOrBrokenAlarms() {
        val now = LocalDateTime.of(2026, 11, 9, 21, 14)
        assertEquals(LocalDateTime.of(2026, 11, 10, 0, 4), ReadingRoomLogic.restoreAlarm("2026-11-10 00:04:00", now))
        assertNull(ReadingRoomLogic.restoreAlarm("2026-11-09 20:00:00", now))
        assertNull(ReadingRoomLogic.restoreAlarm("", now))
        assertNull(ReadingRoomLogic.restoreAlarm("nonsense", now))
        assertNull(ReadingRoomLogic.restoreAlarm(null, now))
    }
}
