package app.kobuggi.hyuabot.data.remote.response.bus

import app.kobuggi.hyuabot.model.BusRealtimeItem
import app.kobuggi.hyuabot.model.BusTimetableByDay
import com.google.gson.annotations.SerializedName

data class BusDepartureByRouteResponse(
    @SerializedName("realtime") val realtime: List<BusRealtimeItem>,
    @SerializedName("timetable") val timetable: BusTimetableByDay
)
