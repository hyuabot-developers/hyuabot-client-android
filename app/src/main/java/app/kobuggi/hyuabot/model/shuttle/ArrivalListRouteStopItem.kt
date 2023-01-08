package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class ArrivalListRouteStopItem(
    @SerializedName("name") val routeName: String,
    @SerializedName("tag") val routeTag: String,
    @SerializedName("arrival") val arrivalList: List<Int>
)
