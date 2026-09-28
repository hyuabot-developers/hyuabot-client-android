package app.kobuggi.hyuabot.ui.shuttle.realtime

import app.kobuggi.hyuabot.ui.home.HomeRequestSelection
import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination

internal data class ShuttleRequestSelection(
    val byDestination: Boolean,
    val showBus: Boolean,
    val showSubway: Boolean,
    val subwayDestination: HomeSubwayTransferDestination,
    val alternatives: ShuttleAlternativeDisplayMode,
) {
    fun subwayPairs(): List<Pair<String, String>> {
        if (!byDestination || !showSubway) return emptyList()
        val station = HomeRequestSelection(subwayDestination = subwayDestination).subwayPairs()
        val jungang = when (subwayDestination) {
            HomeSubwayTransferDestination.SEOUL -> listOf("K450" to "up")
            HomeSubwayTransferDestination.OIDO, HomeSubwayTransferDestination.SOSA -> listOf("K450" to "down")
            else -> emptyList()
        }
        return station + jungang
    }
    val needsBus get() = byDestination && showBus
    fun alternativePairs(): Set<Pair<Int, Int>> {
        if (alternatives == ShuttleAlternativeDisplayMode.HIDDEN) return emptySet()
        return setOf(
            216000068 to 216000383,
            216000081 to 216000028,
            216000101 to 216000028,
            216000068 to 216000379,
            216000016 to 216000152,
            216000068 to 216000138,
            216000082 to 216000077,
            216000102 to 216000077,
            216000016 to 216000074,
            216000082 to 217000140,
            216000102 to 217000140,
            216000016 to 217000264,
        )
    }
}
