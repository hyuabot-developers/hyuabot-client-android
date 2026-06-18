package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.annotation.StringRes

data class BusAlternativeData(
    @StringRes val routeDisplayName: Int,
    val minutes: Int?,
    val stopName: String = "",
    val stopLat: Double = 0.0,
    val stopLng: Double = 0.0
)
