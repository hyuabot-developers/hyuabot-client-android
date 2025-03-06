package app.kobuggi.hyuabot.ui.bus.realtime

data class BusRealtimeItem (
    val routeName: String,
    val sequence: Int,
    val stop: Int,
    val time: Double,
    val seat: Int,
    val lowFloor: Boolean,
    val updatedAt: Any,
)
