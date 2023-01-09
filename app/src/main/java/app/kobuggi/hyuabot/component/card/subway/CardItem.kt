package app.kobuggi.hyuabot.component.card.subway

import app.kobuggi.hyuabot.model.subway.SubwayRealtimeListResponse
import app.kobuggi.hyuabot.model.subway.SubwayTimetableListResponse

data class CardItem (
    val title: Int,
    val stationID: String,
    val headingList: List<Int>,
    var realtimeList: SubwayRealtimeListResponse = SubwayRealtimeListResponse(listOf(), listOf()),
    var timetableList: SubwayTimetableListResponse = SubwayTimetableListResponse(listOf(), listOf()),
    var transferRealtimeList: SubwayRealtimeListResponse = SubwayRealtimeListResponse(listOf(), listOf()),
    var transferTimetableList: SubwayTimetableListResponse = SubwayTimetableListResponse(listOf(), listOf()),
)