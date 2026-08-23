package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.BusRealtimePageQuery
import java.time.LocalTime

data class BusArrivalItem(
    val route: String,
    val item: BusRealtimePageQuery.Arrival,
    val secondaryArrivalTime: LocalTime? = null,
    val destinationStopID: Int? = null,
    val minimumDispatchMinutes: Int? = null,
) {
    val destinationArrivalTime: LocalTime?
        get() {
            val travelMinutes = destinationStopID?.let { destinationID ->
                item.destinationTravelMinutes.firstOrNull { it.destinationStopId == destinationID }?.minutes
            } ?: return null
            val sourceArrival = item.arrivalTime
                ?: item.minutes?.let { LocalTime.now().plusMinutes(it.toLong()) }
                ?: return null
            return sourceArrival.plusMinutes(travelMinutes.toLong())
        }

    /**
     * Minutes from now until arrival, computed the same way regardless of whether the estimate
     * came from live GPS (`minutes`) or a timetable/log clock time (`arrivalTime`) — sorting by
     * this instead of bucketing realtime-first keeps merged multi-route lists (e.g. Suwon's
     * 7070/9090) in true chronological order.
     */
    val remainingMinutes: Double?
        get() {
            if (item.isRealtime) {
                return item.minutes?.toDouble()
            }
            val arrival = item.arrivalTime ?: return null
            val now = LocalTime.now()
            fun serviceSeconds(time: LocalTime): Int {
                val seconds = time.toSecondOfDay()
                return if (seconds < 4 * 3600) seconds + 86400 else seconds
            }
            return (serviceSeconds(arrival) - serviceSeconds(now)) / 60.0
        }
}
