package app.kobuggi.hyuabot.data.remote.domain.shuttle

import app.kobuggi.hyuabot.data.remote.response.shuttle.ShuttleDepartureByStopResponse
import com.google.gson.annotations.SerializedName

data class ShuttleDepartureForAllStop (
    @SerializedName("Residence") val Residence: ShuttleDepartureByStopResponse,
    @SerializedName("Shuttlecock_O") val Shuttlecock_O: ShuttleDepartureByStopResponse,
    @SerializedName("Subway") val Subway: ShuttleDepartureByStopResponse,
    @SerializedName("Terminal") val Terminal: ShuttleDepartureByStopResponse,
    @SerializedName("Shuttlecock_I") val Shuttlecock_I: ShuttleDepartureByStopResponse
)