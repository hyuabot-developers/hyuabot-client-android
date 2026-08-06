package app.kobuggi.hyuabot.ui.bus.realtime

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

data class LogEntry(val date: LocalDate, val time: LocalTime, val vehicle: String)

object BusTravelTimeEstimator {
    private data class Sample(val primaryMinutes: Double, val durationMinutes: Double)

    private const val MAX_PLAUSIBLE_DURATION_MINUTES = 180.0
    private val TIME_OF_DAY_WINDOWS = listOf(30.0, 60.0, 120.0)

    fun secondaryArrivalTime(
        primaryArrivalTime: LocalTime,
        primaryLogs: List<LogEntry>,
        secondaryLogs: List<LogEntry>
    ): LocalTime? {
        val samples = travelDurationSamples(primaryLogs, secondaryLogs)
        if (samples.isEmpty()) return null

        val targetMinutes = minutesSinceMidnight(primaryArrivalTime)
        for (window in TIME_OF_DAY_WINDOWS) {
            val nearby = samples.filter { abs(it.primaryMinutes - targetMinutes) <= window }
            if (nearby.isEmpty()) continue
            val averageDuration = nearby.sumOf { it.durationMinutes } / nearby.size.toDouble()
            return offsetLocalTime(primaryArrivalTime, averageDuration)
        }
        return null
    }

    private fun travelDurationSamples(
        primaryLogs: List<LogEntry>,
        secondaryLogs: List<LogEntry>
    ): List<Sample> {
        val secondaryByDate = secondaryLogs.groupBy { it.date }
        val samples = mutableListOf<Sample>()
        for (primaryLog in primaryLogs) {
            val sameDateSecondaryLogs = secondaryByDate[primaryLog.date] ?: continue
            val laterMatches = sameDateSecondaryLogs.filter { it.vehicle == primaryLog.vehicle && it.time > primaryLog.time }
            val matched = laterMatches.minByOrNull { it.time } ?: continue

            val primaryMinutes = minutesSinceMidnight(primaryLog.time)
            val duration = minutesSinceMidnight(matched.time) - primaryMinutes
            if (duration <= 0 || duration >= MAX_PLAUSIBLE_DURATION_MINUTES) continue
            samples.add(Sample(primaryMinutes, duration))
        }
        return samples
    }

    private fun minutesSinceMidnight(time: LocalTime): Double =
        time.hour * 60.0 + time.minute + time.second / 60.0

    private fun offsetLocalTime(time: LocalTime, minutes: Double): LocalTime {
        // plusSeconds wraps around midnight automatically, matching LocalTime's circular semantics
        return time.plusSeconds((minutes * 60).toLong())
    }
}
