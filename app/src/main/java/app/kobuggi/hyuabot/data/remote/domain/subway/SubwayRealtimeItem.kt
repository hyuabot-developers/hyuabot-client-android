package app.kobuggi.hyuabot.data.remote.domain.subway

import com.google.gson.annotations.SerializedName

data class SubwayRealtimeItem(
    @SerializedName("UpdatedTime") val updatedTime: String,
    @SerializedName("TerminalStation") val terminalStn: String,
    @SerializedName("Position") val pos: String,
    @SerializedName("RemainedTime") val time: Float,
    @SerializedName("Status") val status: String
)
