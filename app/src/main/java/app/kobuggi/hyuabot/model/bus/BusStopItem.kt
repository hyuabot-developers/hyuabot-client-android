package app.kobuggi.hyuabot.model.bus

import com.google.gson.annotations.SerializedName

data class BusStopItem(
    @SerializedName("stopID") val stopID: Int,
    @SerializedName("stopName") val stopName: String,
    @SerializedName("mobileNumber") val mobileNumber: String,
    @SerializedName("location") val location: BusStopLocationItem,
    @SerializedName("route") val routes: List<BusStopRouteItem>,
)
