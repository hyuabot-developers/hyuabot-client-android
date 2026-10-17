package app.kobuggi.hyuabot.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

internal val KST: ZoneId = ZoneId.of("Asia/Seoul")

internal fun kst(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime =
    ZonedDateTime.of(year, month, day, hour, minute, 0, 0, KST)

/** Runs [block] once with the JVM default time zone set to Seoul and once set to New York. */
internal fun inBothDefaultZones(block: () -> Unit) {
    val original = TimeZone.getDefault()
    try {
        for (zone in listOf("Asia/Seoul", "America/New_York")) {
            TimeZone.setDefault(TimeZone.getTimeZone(zone))
            block()
        }
    } finally {
        TimeZone.setDefault(original)
    }
}
