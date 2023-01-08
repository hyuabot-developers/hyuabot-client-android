package app.kobuggi.hyuabot.ui.bus.realtime

data class RealtimeItem(
    val routeName: String,
    val remainedStop: Int,
    val remainedTime: Int,
    val remainedSeat: Int,
    val lowPlate: Boolean,
)
