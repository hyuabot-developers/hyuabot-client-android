package app.kobuggi.hyuabot.ui.main

import app.kobuggi.hyuabot.data.remote.domain.shuttle.ShuttleDepartureItem
import app.kobuggi.hyuabot.data.remote.domain.subway.SubwayRealtimeList
import app.kobuggi.hyuabot.data.remote.domain.subway.SubwayTimetableList

data class ShuttleCardItem(
    val shuttleStopID: Int,
    val headingID: Int,
    val arrivalList: List<ShuttleDepartureItem>,
    val subwayItemsRealtime : List<SubwayRealtimeList>?,
    val subwayItemsTimetable: List<SubwayTimetableList>?
)
