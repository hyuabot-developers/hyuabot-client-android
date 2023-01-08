package app.kobuggi.hyuabot.model.bus

import app.kobuggi.hyuabot.component.card.shuttle.SubCardItem
import com.google.gson.annotations.SerializedName

data class BusStopRouteItem(
    @SerializedName("id") val routeID: Int,
    @SerializedName("name") val routeName: String,
    @SerializedName("start") val startStop: BusRouteStartStopItem,
    @SerializedName("realtime") val realtime: List<BusRouteRealtimeItem>,
) : SubCardItem()
