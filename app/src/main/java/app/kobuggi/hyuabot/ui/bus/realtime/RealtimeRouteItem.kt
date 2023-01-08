package app.kobuggi.hyuabot.ui.bus.realtime

data class RealtimeRouteItem(
    var routeID: Int = 0,
    var startStopID: Int = 0,
    var stopID: Int = 0,
    var routeName: String = "",
    var timetable: List<String> = listOf(),
    var realtime: List<RealtimeItem> = listOf(),
)
