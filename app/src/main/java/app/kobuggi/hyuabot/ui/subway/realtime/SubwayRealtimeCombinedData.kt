package app.kobuggi.hyuabot.ui.subway.realtime

import app.kobuggi.hyuabot.SubwayRealtimePageQuery

data class SubwayRealtimeCombinedData(
    val campusYellow: SubwayRealtimePageQuery.Subway?,
    val campusBlue: SubwayRealtimePageQuery.Subway?,
    val oidoYellow: SubwayRealtimePageQuery.Subway?,
    val oidoBlue: SubwayRealtimePageQuery.Subway?
)
