package app.kobuggi.hyuabot.util

import java.time.LocalTime
import java.time.ZonedDateTime

data class BusDeparture(
    val key: Any,
    val route: String,
    val stop: String,
    val isRealtime: Boolean,
    val minutes: Int?,
    val arrivalTime: LocalTime?,
)

enum class BusContext { HOME, DETAIL }

object LastRunJudge {
    /** Shuttle last-run: the last item of the list the caller passes (same as the previous inline expression). */
    fun <T> shuttleLastRunSeqs(remaining: List<T>, seq: (T) -> Int): Set<Int> =
        setOfNotNull(remaining.lastOrNull()?.let(seq))

    /**
     * Sorts departures on the service clock. Realtime items use now + minutes (when minutes is usable),
     * otherwise arrivalTime. Items with neither are dropped. Ties put realtime items first.
     */
    fun busSorted(list: List<BusDeparture>, now: ZonedDateTime): List<BusDeparture> {
        val kstNow = now.withZoneSameInstant(ServiceClock.ZONE)
        return list
            .mapNotNull { departure -> serviceSecondsOf(departure, kstNow)?.let { departure to it } }
            .sortedWith(
                compareBy<Pair<BusDeparture, Int>> { it.second }
                    .thenBy { if (it.first.isRealtime) 0 else 1 },
            )
            .map { it.first }
    }

    /**
     * Keys of bus departures that carry a last-run badge. Grouped per (route, stop).
     * The group's last item qualifies only if its index is below [topN]. HOME never produces last-run.
     */
    fun busLastRunKeys(
        list: List<BusDeparture>,
        now: ZonedDateTime,
        context: BusContext,
        topN: Int = 3,
    ): Set<Any> {
        if (context == BusContext.HOME) return emptySet()
        return list
            .groupBy { it.route to it.stop }
            .values
            .mapNotNull { group ->
                val sorted = busSorted(group, now)
                if (sorted.isEmpty() || sorted.lastIndex >= topN) null else sorted.last().key
            }
            .toSet()
    }

    fun subwayIsLastRun(isLast: Boolean?): Boolean = isLast == true

    private fun serviceSecondsOf(departure: BusDeparture, kstNow: ZonedDateTime): Int? {
        val minutes = departure.minutes
        if (departure.isRealtime && minutes != null) {
            val nowServiceSeconds = ServiceClock.serviceSecondOfDay(kstNow.toLocalTime())
            return nowServiceSeconds + minutes * 60
        }
        return departure.arrivalTime?.let { ServiceClock.serviceSecondOfDay(it) }
    }
}
