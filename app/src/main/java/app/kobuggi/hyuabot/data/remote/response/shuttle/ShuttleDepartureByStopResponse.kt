package app.kobuggi.hyuabot.data.remote.response.shuttle

import app.kobuggi.hyuabot.model.ShuttleItem
import com.google.gson.annotations.SerializedName

data class ShuttleDepartureByStopResponse(
    @SerializedName("busForStation") val forStation: List<ShuttleItem>,
    @SerializedName("busForTerminal") val forTerminal: List<ShuttleItem>
)