package app.kobuggi.hyuabot.service.time

import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object CampusTime {
    val ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    fun now(clock: Clock = Clock.system(ZONE)): ZonedDateTime = ZonedDateTime.now(clock).withZoneSameInstant(ZONE)

    fun today(clock: Clock = Clock.system(ZONE)): LocalDate = now(clock).toLocalDate()

    fun minutesUntil(from: LocalTime, to: LocalTime): Long {
        val diff = Duration.between(from, to).toMinutes()
        return if (diff >= 0) diff else diff + 24 * 60L
    }
}
