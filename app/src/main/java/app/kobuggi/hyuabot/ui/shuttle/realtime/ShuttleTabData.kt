package app.kobuggi.hyuabot.ui.shuttle.realtime

import app.kobuggi.hyuabot.ShuttleRealtimePageQuery

data class ShuttleTabData (
    val result: List<ShuttleRealtimePageQuery.Timetable>,
    val showByDestination: Boolean
)
