package app.kobuggi.hyuabot.ui.common

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Android-free freshness rules shared by the display component and its unit tests. */
object Freshness {
    enum class Category { SCHEDULE, REALTIME, READING_ROOM, HOURLY, OBSERVED }

    data class Display(val text: String, val spoken: String, val stale: Boolean)

    fun elapsed(now: Instant, updatedAt: Instant): Duration =
        Duration.between(updatedAt, now).coerceAtLeast(Duration.ZERO)

    fun display(category: Category, updatedAt: Instant?, now: Instant, timeZone: ZoneId, staleAfterOverride: Duration? = null): Display {
        if (category == Category.SCHEDULE || (category == Category.REALTIME && updatedAt == null)) {
            return Display("시간표 기준", "시간표 기준", false)
        }
        if (category == Category.HOURLY) return Display("매시 갱신", "매시 갱신", false)
        if (category == Category.OBSERVED && updatedAt != null) {
            val observed = updatedAt.atZone(timeZone).format(CLOCK) + " 관측"
            return Display(observed, observed, false)
        }
        if (updatedAt == null) return Display("", "", false)

        val age = elapsed(now, updatedAt)
        val staleAfter = staleAfterOverride
            ?: if (category == Category.READING_ROOM) Duration.ofMinutes(5) else Duration.ofMinutes(3)
        val minutes = age.toMinutes()
        val stale = age > staleAfter
        val text = when {
            stale -> "정보 지연 · ${minutes}분 전"
            category == Category.READING_ROOM -> "${minutes}분 전 갱신"
            age.seconds < 60 -> "${age.seconds}초 전 갱신"
            else -> "${minutes}분 전 갱신"
        }
        val spoken = when {
            stale -> "정보 지연, ${minutes}분 전"
            minutes == 0L -> "1분 미만"
            else -> "${minutes}분 전"
        }
        return Display(text, spoken, stale)
    }

    fun timeText(minutes: Long): String = when {
        minutes < 0 -> "—"
        minutes >= 60 -> String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)
        else -> "${minutes}분"
    }

    private val CLOCK = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
}
