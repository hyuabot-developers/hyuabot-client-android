package app.kobuggi.hyuabot.util

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Service clock for shuttle/bus/subway operation judgments.
 *
 * All judgments use KST (Asia/Seoul). The service day starts at 04:00 KST,
 * so times before 04:00 belong to the previous service day.
 */
object ServiceClock {
    val ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    private const val SECONDS_PER_DAY = 86_400
    private val SERVICE_DAY_START: LocalTime = LocalTime.of(4, 0)
    private val CUTOFF_START: LocalTime = LocalTime.of(2, 0)
    private val CUTOFF_END: LocalTime = LocalTime.of(5, 0)

    fun now(clock: Clock = Clock.systemUTC()): ZonedDateTime = ZonedDateTime.now(clock.withZone(ZONE))

    fun serviceDate(now: ZonedDateTime): LocalDate {
        val kst = now.withZoneSameInstant(ZONE)
        return if (kst.toLocalTime().isBefore(SERVICE_DAY_START)) {
            kst.toLocalDate().minusDays(1)
        } else {
            kst.toLocalDate()
        }
    }

    fun serviceSecondOfDay(t: LocalTime): Int {
        val seconds = t.toSecondOfDay()
        return if (t.isBefore(SERVICE_DAY_START)) seconds + SECONDS_PER_DAY else seconds
    }

    /** True for KST 02:00:00 (inclusive) through 04:59:59 (end of the 05:00 exclusive bound). */
    fun isLateNightCutoff(now: ZonedDateTime): Boolean {
        val t = now.withZoneSameInstant(ZONE).toLocalTime()
        return !t.isBefore(CUTOFF_START) && t.isBefore(CUTOFF_END)
    }

    /** Floor of (target - now) in seconds divided by 60. Negative when the target is already past. */
    fun remainingMinutes(target: LocalTime, now: ZonedDateTime): Int {
        val nowSeconds = serviceSecondOfDay(now.withZoneSameInstant(ZONE).toLocalTime())
        val diff = serviceSecondOfDay(target) - nowSeconds
        return Math.floorDiv(diff, 60)
    }
}
