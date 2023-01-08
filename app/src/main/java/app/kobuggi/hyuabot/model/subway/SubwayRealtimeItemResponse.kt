package app.kobuggi.hyuabot.model.subway

import com.google.gson.annotations.SerializedName

data class SubwayRealtimeItemResponse(
    @SerializedName("terminal") val terminalStation: String,
    @SerializedName("trainNumber") val trainNumber: String,
    @SerializedName("time") val time: Int,
    @SerializedName("stop") val stop: Int,
    @SerializedName("heading") val heading: Boolean,
    @SerializedName("express") val express: Boolean,
    @SerializedName("location") val location: String,
)
