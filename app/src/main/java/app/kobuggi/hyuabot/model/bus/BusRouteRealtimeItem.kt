package app.kobuggi.hyuabot.model.bus

import com.google.gson.annotations.SerializedName

data class BusRouteRealtimeItem(
    @SerializedName("stop") val remainedStop: Int,
    @SerializedName("time") val remainedTime: Int,
    @SerializedName("seat") val remainedSeat: Int,
    @SerializedName("lowPlate") val lowPlate: Boolean,
)
