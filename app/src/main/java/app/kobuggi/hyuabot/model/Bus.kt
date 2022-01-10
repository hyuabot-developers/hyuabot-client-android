package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName

data class BusCardItem(
    val lineName: String,
    val lineColor: String,
    val busStop: String,
    val heading: String,
    val busData : BusByRoute,
    val minutesFromTerminalStop : Int
)

data class BusRealtimeItem(
    @SerializedName("Location") val location: Int,
    @SerializedName("RemainedTime") val time: Int,
    @SerializedName("RemainedSeat") val seat: Int,
)

data class BusTimeTableItem (
    @SerializedName("time") val time: String
)

data class BusTimetableByDay(
    @SerializedName("weekdays") val weekdays: List<BusTimeTableItem>,
    @SerializedName("sat") val sat: List<BusTimeTableItem>,
    @SerializedName("sun") val sun: List<BusTimeTableItem>,
)

data class BusByRoute(
    @SerializedName("realtime") val realtime: List<BusRealtimeItem>,
    @SerializedName("timetable") val timetable: BusTimetableByDay
)

data class Bus(
    @SerializedName("10-1_station") val greenBusForStation: BusByRoute,
    @SerializedName("10-1_campus") val greenBusForCampus: BusByRoute,
    @SerializedName("707-1") val blueBus: BusByRoute,
    @SerializedName("3102") val redBus: BusByRoute,
)

data class BusByRouteRequest(
    @SerializedName("routeID") val routeID: String
)