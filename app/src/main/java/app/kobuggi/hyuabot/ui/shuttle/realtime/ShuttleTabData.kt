package app.kobuggi.hyuabot.ui.shuttle.realtime

import app.kobuggi.hyuabot.ShuttleRealtimePageQuery

data class ShuttleTabData (
    val result: List<ShuttleRealtimePageQuery.Stop>,
    val showByDestination: Boolean
)
