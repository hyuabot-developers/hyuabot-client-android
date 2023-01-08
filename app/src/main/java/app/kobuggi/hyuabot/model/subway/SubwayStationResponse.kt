package app.kobuggi.hyuabot.model.subway

import com.google.gson.annotations.SerializedName

data class SubwayStationResponse(
    @SerializedName("stationID") val stationID: String,
    @SerializedName("stationName") val stationName: String,
    @SerializedName("routeID") val routeID: Int,
    @SerializedName("realtime") val realtime: SubwayRealtimeListResponse,
    @SerializedName("timetable") val timetable: SubwayTimetableListResponse,
)
