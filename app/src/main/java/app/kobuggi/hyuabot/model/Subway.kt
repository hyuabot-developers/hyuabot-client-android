package app.kobuggi.hyuabot.model

import app.kobuggi.hyuabot.data.remote.domain.subway.SubwayRealtimeItem
import app.kobuggi.hyuabot.data.remote.domain.subway.SubwayTimetableItem

data class SubwayCardItem(
    val lineName : String,
    val lineIconResID : Int,
    val heading : String,
    val realtime : List<SubwayRealtimeItem>?,
    val timetable : List<SubwayTimetableItem>?,
)


