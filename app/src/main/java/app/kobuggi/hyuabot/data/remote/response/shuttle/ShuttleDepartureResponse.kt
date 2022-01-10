package app.kobuggi.hyuabot.data.remote.response.shuttle

import app.kobuggi.hyuabot.model.ShuttleByStop
import com.google.gson.annotations.SerializedName

data class ShuttleDepartureResponse(
    @SerializedName("Residence") val Residence: ShuttleByStop,
    @SerializedName("Shuttlecock_O") val Shuttlecock_O: ShuttleByStop,
    @SerializedName("Subway") val Subway: ShuttleByStop,
    @SerializedName("Terminal") val Terminal: ShuttleByStop,
    @SerializedName("Shuttlecock_I") val Shuttlecock_I: ShuttleByStop
)