package app.kobuggi.hyuabot.model.bus

import com.google.gson.annotations.SerializedName

data class BusRouteStartStopItem(
    @SerializedName("id") val stopID: Int,
    @SerializedName("name") val stopName: String,
    @SerializedName("timetable") val timetable: List<String>,
)