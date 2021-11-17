package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName

data class SubwayCardItem(
    val lineName : String,
    val lineIconResID : Int,
    val heading : String,
    val realtime : List<SubwayItemByLineWithRealtime>?,
    val timetable : List<SubwayItemByLineWithTimetable>?,
)

data class SubwayItemByLineWithRealtime(
    @SerializedName("endStn") val terminalStn: String,
    @SerializedName("pos") val pos: String,
    @SerializedName("time") val time: Float,
    @SerializedName("status") val status: String
)

data class SubwayByLineWithRealtime(
    @SerializedName("up") val upLine: List<SubwayItemByLineWithRealtime>,
    @SerializedName("down") val downLine: List<SubwayItemByLineWithRealtime>
)

data class SubwayItemByLineWithTimetable(
    @SerializedName("endStn") val terminalStn: String,
    @SerializedName("time") val time: String,
)

data class SubwayByLineWithTimetable(
    @SerializedName("up") val upLine: List<SubwayItemByLineWithTimetable>,
    @SerializedName("down") val downLine: List<SubwayItemByLineWithTimetable>
)

data class SubwayByLine(
    @SerializedName("realtime") val realtime: SubwayByLineWithRealtime,
    @SerializedName("timetable") val timetable: SubwayByLineWithTimetable
)

data class SubwayERICA(
    @SerializedName("main") val line4: SubwayByLine,
    @SerializedName("sub") val lineSuin: SubwayByLine
)
