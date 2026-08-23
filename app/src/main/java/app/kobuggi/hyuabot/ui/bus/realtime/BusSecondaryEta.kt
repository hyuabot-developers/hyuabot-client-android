package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.BusSecondaryEtaLogQuery
import java.time.LocalTime

/** Estimates a bus's clock arrival time from either live GPS data or a historical-log-derived duration. */
object BusSecondaryEta {
    fun estimatedArrivalTime(arrival: BusRealtimePageQuery.Arrival): LocalTime? {
        arrival.arrivalTime?.let { return it }
        if (!arrival.isRealtime) return null
        val minutes = arrival.minutes ?: return null
        return LocalTime.now().plusMinutes(minutes.toLong())
    }

    fun secondaryArrivalTime(
        arrival: BusRealtimePageQuery.Arrival,
        primaryLogs: List<BusSecondaryEtaLogQuery.Log>,
        secondaryLogs: List<BusSecondaryEtaLogQuery.Log>?,
        destinationStopID: Int? = null,
    ): LocalTime? {
        if (destinationStopID == null) return null
        val travelMinutes = arrival.destinationTravelMinutes
            .firstOrNull { it.destinationStopId == destinationStopID }
            ?.minutes ?: return null
        val sourceArrivalTime = arrival.arrivalTime
            ?: arrival.minutes?.let { LocalTime.now().plusMinutes(it.toLong()) }
            ?: return null
        return sourceArrivalTime.plusMinutes(travelMinutes.toLong())
    }
}
