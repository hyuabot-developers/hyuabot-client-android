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
        secondaryLogs: List<BusSecondaryEtaLogQuery.Log>?
    ): LocalTime? {
        if (secondaryLogs == null) return null
        val primaryArrivalTime = estimatedArrivalTime(arrival) ?: return null
        return BusTravelTimeEstimator.secondaryArrivalTime(
            primaryArrivalTime,
            primaryLogs.map { LogEntry(it.date, it.time, it.vehicle) },
            secondaryLogs.map { LogEntry(it.date, it.time, it.vehicle) }
        )
    }
}
