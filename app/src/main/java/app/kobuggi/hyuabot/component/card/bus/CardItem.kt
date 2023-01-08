package app.kobuggi.hyuabot.component.card.bus

import app.kobuggi.hyuabot.ui.bus.realtime.RealtimeRouteItem

data class CardItem (
    val title: Int, val routeList: List<RealtimeRouteItem>,
)