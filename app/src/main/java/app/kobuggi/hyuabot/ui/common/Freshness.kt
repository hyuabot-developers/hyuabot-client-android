package app.kobuggi.hyuabot.ui.common

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Localized wording for freshness labels. Pure Kotlin; the app supplies resource-backed strings. */
interface FreshnessText {
    fun scheduleBasis(): String
    fun hourly(): String
    fun observed(time: String): String
    fun delayed(minutes: Long): String
    fun readingRoomUpdated(minutes: Long): String
    fun secondsUpdated(seconds: Long): String
    fun minutesUpdated(minutes: Long): String
    fun spokenDelayed(minutes: Long): String
    fun spokenUnderMinute(): String
    fun spokenMinutes(minutes: Long): String
    fun minuteUnit(minutes: Long): String
    fun informationDelayed(): String
}

object Freshness {
    enum class Category { SCHEDULE, REALTIME, READING_ROOM, HOURLY, OBSERVED }

    data class Display(val text: String, val spoken: String, val stale: Boolean)

    fun elapsed(now: Instant, updatedAt: Instant): Duration =
        Duration.between(updatedAt, now).coerceAtLeast(Duration.ZERO)

    fun display(
        category: Category,
        updatedAt: Instant?,
        now: Instant,
        timeZone: ZoneId,
        words: FreshnessText,
        staleAfterOverride: Duration? = null,
        forcedDelayed: Boolean = false,
    ): Display {
        if (category == Category.SCHEDULE) {
            return Display(words.scheduleBasis(), words.scheduleBasis(), false)
        }
        if (forcedDelayed) {
            if (updatedAt == null) {
                val delayed = words.informationDelayed()
                return Display(delayed, delayed, true)
            }
            val minutes = elapsed(now, updatedAt).toMinutes()
            return Display(words.delayed(minutes), words.spokenDelayed(minutes), true)
        }
        if (category == Category.REALTIME && updatedAt == null) {
            return Display("", "", false)
        }
        if (category == Category.HOURLY) return Display(words.hourly(), words.hourly(), false)
        if (category == Category.OBSERVED && updatedAt != null) {
            val observed = words.observed(updatedAt.atZone(timeZone).format(CLOCK))
            return Display(observed, observed, false)
        }
        if (updatedAt == null) {
            if (!forcedDelayed) return Display("", "", false)
            val delayed = words.informationDelayed()
            return Display(delayed, delayed, true)
        }

        val age = elapsed(now, updatedAt)
        val staleAfter = staleAfterOverride
            ?: if (category == Category.READING_ROOM) Duration.ofMinutes(5) else Duration.ofMinutes(3)
        val minutes = age.toMinutes()
        val stale = age > staleAfter
        val text = when {
            stale -> words.delayed(minutes)
            category == Category.READING_ROOM -> words.readingRoomUpdated(minutes)
            age.seconds < 60 -> words.secondsUpdated(age.seconds)
            else -> words.minutesUpdated(minutes)
        }
        val spoken = when {
            stale -> words.spokenDelayed(minutes)
            minutes == 0L -> words.spokenUnderMinute()
            else -> words.spokenMinutes(minutes)
        }
        return Display(text, spoken, stale)
    }

    fun timeText(minutes: Long, words: FreshnessText): String = when {
        minutes < 0 -> "\u2014"
        minutes >= 60 -> String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)
        else -> words.minuteUnit(minutes)
    }

    private val CLOCK = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
}
