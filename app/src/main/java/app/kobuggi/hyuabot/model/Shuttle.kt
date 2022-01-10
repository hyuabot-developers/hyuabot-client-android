package app.kobuggi.hyuabot.model

import app.kobuggi.hyuabot.data.remote.domain.shuttle.ShuttleDepartureItem

data class ShuttleDataItem(
    val cardTitle: Int,
    val arrivalList: List<ShuttleDepartureItem>?
)



