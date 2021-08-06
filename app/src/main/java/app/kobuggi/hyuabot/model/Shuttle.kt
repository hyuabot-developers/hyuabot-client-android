package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName

data class ShuttleItem(
    @SerializedName("time") val time: String,
    @SerializedName("type") val type: String
)

data class ShuttleByStop(
    @SerializedName("busForStation") val forStation: List<ShuttleItem>,
    @SerializedName("busForTerminal") val forTerminal: List<ShuttleItem>
)

data class Shuttle(
    @SerializedName("Residence") val Residence: ShuttleByStop,
    @SerializedName("Shuttlecock_O") val Shuttlecock_O: ShuttleByStop,
    @SerializedName("Subway") val Subway: ShuttleByStop,
    @SerializedName("Terminal") val Terminal: ShuttleByStop,
    @SerializedName("Shuttlecock_I") val Shuttlecock_I: ShuttleByStop
)