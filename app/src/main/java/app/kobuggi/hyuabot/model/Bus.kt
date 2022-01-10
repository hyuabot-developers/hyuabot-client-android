package app.kobuggi.hyuabot.model

import app.kobuggi.hyuabot.data.remote.response.bus.BusDepartureByRouteResponse

data class BusCardItem(
    val lineName: String,
    val lineColor: String,
    val busStop: String,
    val heading: String,
    val busData : BusDepartureByRouteResponse,
    val minutesFromTerminalStop : Int
)


