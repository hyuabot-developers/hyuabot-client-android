package app.kobuggi.hyuabot.ui.subway.realtime

import app.kobuggi.hyuabot.SubwayRealtimePageQuery

data class SubwayTransferItem(
    val take: SubwayRealtimePageQuery.Entry,
    val transfer: SubwayRealtimePageQuery.Entry?
)
