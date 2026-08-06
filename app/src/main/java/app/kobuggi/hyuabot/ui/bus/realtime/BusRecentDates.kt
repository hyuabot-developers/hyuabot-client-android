package app.kobuggi.hyuabot.ui.bus.realtime

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

object BusRecentDates {
    fun sameWeekdayType(
        count: Int,
        zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
        from: LocalDate = LocalDate.now(zoneId)
    ): List<LocalDate> {
        if (count <= 0) return emptyList()

        val targetBucket = bucketOf(from)
        return buildList(count) {
            var current = from.minusDays(1)
            while (size < count) {
                if (bucketOf(current) == targetBucket) add(current)
                current = current.minusDays(1)
            }
        }
    }

    private enum class Bucket { WEEKDAYS, SATURDAY, SUNDAY }

    private fun bucketOf(date: LocalDate): Bucket = when (date.dayOfWeek) {
        DayOfWeek.SATURDAY -> Bucket.SATURDAY
        DayOfWeek.SUNDAY -> Bucket.SUNDAY
        else -> Bucket.WEEKDAYS
    }
}
