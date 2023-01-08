package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class StopRouteItem(
    @SerializedName("name") val name: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("runningTime") val runningTime: StopRouteRunningTime,
    @SerializedName("timetable") val timetable: List<String>,
)
