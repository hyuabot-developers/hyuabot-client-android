package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class StopItemResponse(
    @SerializedName("stop") val stopList: List<ArrivalListStopItem>,
    @SerializedName("location") val location: StopLocationItem,
    @SerializedName("route") val routeList: List<StopRouteItem>,
)
