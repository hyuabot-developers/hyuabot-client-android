package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.BusRealtimePageQuery
import java.time.DayOfWeek
import java.time.LocalDate

object BusDispatchInterval {
    fun forToday(intervals: List<BusRealtimePageQuery.MinimumDispatchInterval>, date: LocalDate = LocalDate.now()): Int? {
        val weekday = when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> "saturday"
            DayOfWeek.SUNDAY -> "sunday"
            else -> "weekdays"
        }
        return intervals.firstOrNull { it.weekday == weekday }?.minutes
    }
}
