package app.kobuggi.hyuabot.ui.subway.realtime

import app.kobuggi.hyuabot.type.SubwayStationInput
import com.apollographql.apollo.api.Optional

internal fun subwayRequestKeys(tab: Int, weekday: String): List<SubwayStationInput> {
    fun station(id: String, directions: List<String>, limit: Int?) = SubwayStationInput(
        stationID = id,
        direction = directions,
        weekdays = listOf(weekday),
        limit = Optional.present(limit),
    )
    return when (tab) {
        1 -> listOf(station("K251", listOf("up", "down"), 4))
        2 -> listOf(
            station("K449", listOf("down"), 4),
            station("K251", listOf("down"), 4),
            station("K258", listOf("down"), null),
            station("S26", listOf("up"), null),
        )
        else -> listOf(station("K449", listOf("up", "down"), 4))
    }
}
