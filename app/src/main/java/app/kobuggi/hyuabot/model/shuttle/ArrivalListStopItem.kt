package app.kobuggi.hyuabot.model.shuttle

import app.kobuggi.hyuabot.component.card.shuttle.SubCardItem
import com.google.gson.annotations.SerializedName

data class ArrivalListStopItem(
    @SerializedName("name") val stopName: String,
    @SerializedName("route") val routeList: List<ArrivalListRouteStopItem>
) : SubCardItem()