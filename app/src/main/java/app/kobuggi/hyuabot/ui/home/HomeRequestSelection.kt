package app.kobuggi.hyuabot.ui.home

/** The visible shuttle path and transfer options, captured once per request. */
internal data class HomeRequestSelection(
    val stop: String = "dormitory_o",
    val destination: String = "STATION",
    val showSeoulBusStop: Boolean = true,
    val seoulBusStop: Int = 121000974,
    val showBus50: Boolean = true,
    val showSubway: Boolean = true,
    val subwayDestination: HomeSubwayTransferDestination = HomeSubwayTransferDestination.SEOUL,
) {
    private val outbound get() = stop == "dormitory_o" || stop == "shuttlecock_o"
    val needsBus50 get() = showBus50 && outbound && destination == "TERMINAL"

    fun subwayPairs(): List<Pair<String, String>> {
        if (!showSubway || !outbound || destination != "STATION") return emptyList()
        return when (subwayDestination) {
            HomeSubwayTransferDestination.SEOUL -> listOf("K449" to "up")
            HomeSubwayTransferDestination.SUWON_YONGIN -> listOf("K251" to "up")
            HomeSubwayTransferDestination.OIDO -> listOf("K449" to "down", "K251" to "down")
            HomeSubwayTransferDestination.INCHEON -> listOf("K449" to "down", "K251" to "down", "K258" to "down")
            HomeSubwayTransferDestination.SOSA -> listOf("K449" to "down", "K251" to "down", "S26" to "up")
        }
    }

    fun alternativeBusPairs(): Set<Pair<Int, Int>> = when (stop to destination) {
        "dormitory_o" to "STATION" -> setOf(216000068 to 216000383)
        "dormitory_o" to "TERMINAL", "dormitory_o" to "JUNGANG" ->
            setOf(216000081 to 216000028, 216000101 to 216000028)
        "shuttlecock_o" to "TERMINAL", "shuttlecock_o" to "JUNGANG" -> setOf(216000016 to 216000152)
        "station" to "CAMPUS" -> setOf(216000068 to 216000138)
        "terminal" to "CAMPUS" -> setOf(216000082 to 216000077, 216000102 to 216000077, 216000016 to 216000074)
        "jungang_stn" to "CAMPUS" -> setOf(216000082 to 217000140, 216000102 to 217000140, 216000016 to 217000264)
        else -> emptySet()
    }
}
