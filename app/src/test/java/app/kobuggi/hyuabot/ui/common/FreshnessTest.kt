package app.kobuggi.hyuabot.ui.common

import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreshnessTest {
    private val now = Instant.parse("2026-10-18T03:00:00Z")
    private val utc = ZoneOffset.UTC

    private fun realtime(secondsAgo: Long) = Freshness.display(
        Freshness.Category.REALTIME,
        now.minusSeconds(secondsAgo),
        now,
        utc,
        Ko,
    )

    @Test fun formats59And60Seconds() {
        assertEquals("59초 전 갱신", realtime(59).text)
        assertEquals("1분 전 갱신", realtime(60).text)
    }

    @Test fun realtimeStalesOnlyAfterThreeMinutes() {
        assertEquals("3분 전 갱신", realtime(180).text)
        assertFalse(realtime(180).stale)
        assertEquals("정보 지연 · 3분 전", realtime(181).text)
        assertTrue(realtime(181).stale)
    }

    @Test fun negativeElapsedIsClampedToZero() {
        val result = Freshness.display(Freshness.Category.REALTIME, now.plusSeconds(15), now, utc, Ko)
        assertEquals("0초 전 갱신", result.text)
        assertFalse(result.stale)
    }

    @Test fun readingRoomStalesOnlyAfterFiveMinutes() {
        fun label(secondsAgo: Long) = Freshness.display(
            Freshness.Category.READING_ROOM, now.minusSeconds(secondsAgo), now, utc, Ko,
        )
        assertFalse(label(300).stale)
        assertEquals("정보 지연 · 5분 전", label(301).text)
    }

    @Test fun timeTextSwitchesAtSixtyMinutes() {
        assertEquals("59분", Freshness.timeText(59, Ko))
        assertEquals("01:00", Freshness.timeText(60, Ko))
    }
}

/** Korean wording matching values/strings.xml, so the assertions keep checking the shipped text. */
private object Ko : FreshnessText {
    override fun scheduleBasis() = "시간표 기준"
    override fun hourly() = "매시 갱신"
    override fun observed(time: String) = "$time 관측"
    override fun delayed(minutes: Long) = "정보 지연 · ${minutes}분 전"
    override fun readingRoomUpdated(minutes: Long) = "${minutes}분 전 갱신"
    override fun secondsUpdated(seconds: Long) = "${seconds}초 전 갱신"
    override fun minutesUpdated(minutes: Long) = "${minutes}분 전 갱신"
    override fun spokenDelayed(minutes: Long) = "정보 지연, ${minutes}분 전"
    override fun spokenUnderMinute() = "1분 미만"
    override fun spokenMinutes(minutes: Long) = "${minutes}분 전"
    override fun minuteUnit(minutes: Long) = "${minutes}분"
}
