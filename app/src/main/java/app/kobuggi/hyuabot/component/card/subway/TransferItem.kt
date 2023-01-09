package app.kobuggi.hyuabot.component.card.subway

import app.kobuggi.hyuabot.model.subway.SubwayRealtimeItemResponse
import app.kobuggi.hyuabot.model.subway.SubwayTimetableItemResponse

data class TransferItem(
    val from: SubwayRealtimeItemResponse,
    val fromID: Int,
    val to: SubwayTimetableItemResponse? = null,
    val toID: Int? = null
)
