package app.kobuggi.hyuabot.model.subway

import com.google.gson.annotations.SerializedName

data class SubwayTimetableItemResponse(
    @SerializedName("start") val startStation: String,
    @SerializedName("terminal") val terminalStation: String,
    @SerializedName("departureTime") val departureTime: String,
)
