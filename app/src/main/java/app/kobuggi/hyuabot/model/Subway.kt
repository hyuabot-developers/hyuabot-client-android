package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName

data class SubwayItemByLineWithRealtime(
    @SerializedName("terminalStn") val terminalStn: String,
    @SerializedName("pos") val pos: String,
    @SerializedName("time") val time: Float,
    @SerializedName("status") val status: String
)

data class SubwayByLineWithRealtime(
    @SerializedName("up") val upLine: List<SubwayItemByLineWithRealtime>,
    @SerializedName("down") val downLine: List<SubwayItemByLineWithRealtime>
)

data class SubwayItemByLineWithTimetable(
    @SerializedName("terminalStn") val terminalStn: String,
    @SerializedName("time") val time: String,
)

data class SubwayByLineWithTimetable(
    @SerializedName("up") val upLine: List<SubwayItemByLineWithTimetable>,
    @SerializedName("down") val downLine: List<SubwayItemByLineWithTimetable>
)

data class SubwayERICA(
    @SerializedName("main") val line4: SubwayByLineWithRealtime,
    @SerializedName("sub") val lineSuin: SubwayByLineWithTimetable
)
