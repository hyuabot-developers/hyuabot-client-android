package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.BusRealtimePageQuery

data class BusArrivalItem(
    val route: String,
    val item: BusRealtimePageQuery.Arrival
)
