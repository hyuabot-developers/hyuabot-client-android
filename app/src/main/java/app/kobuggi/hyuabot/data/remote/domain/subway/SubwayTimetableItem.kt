package app.kobuggi.hyuabot.data.remote.domain.subway

import com.google.gson.annotations.SerializedName

data class SubwayTimetableItem(
    @SerializedName("endStn") val terminalStn: String,
    @SerializedName("time") val time: String,
)