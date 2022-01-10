package app.kobuggi.hyuabot.data.remote.response.bus

import com.google.gson.annotations.SerializedName

data class BusDepartureResponse(
    @SerializedName("10-1_station") val greenBusForStation: BusDepartureByRouteResponse,
    @SerializedName("10-1_campus") val greenBusForCampus: BusDepartureByRouteResponse,
    @SerializedName("707-1") val blueBus: BusDepartureByRouteResponse,
    @SerializedName("3102") val redBus: BusDepartureByRouteResponse,
)
