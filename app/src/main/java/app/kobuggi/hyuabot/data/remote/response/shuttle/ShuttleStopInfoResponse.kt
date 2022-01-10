package app.kobuggi.hyuabot.data.remote.response.shuttle

import app.kobuggi.hyuabot.model.ShuttleByStop
import com.google.gson.annotations.SerializedName

data class ShuttleStopInfoResponse(
    @SerializedName("roadViewLink") val roadViewLink: String,
    @SerializedName("firstBusForStation") val firstBusForStation: String,
    @SerializedName("lastBusForStation") val lastBusForStation: String,
    @SerializedName("firstBusForTerminal") val firstBusForTerminal: String,
    @SerializedName("lastBusForTerminal") val lastBusForTerminal: String,
    @SerializedName("weekdays") val weekdays: ShuttleByStop,
    @SerializedName("weekends") val weekends: ShuttleByStop
)