package app.kobuggi.hyuabot.data.remote.domain.bus

import com.google.gson.annotations.SerializedName

data class BusDepartureRealtimeItem(
    @SerializedName("Location") val location: Int,
    @SerializedName("RemainedTime") val time: Int,
    @SerializedName("RemainedSeat") val seat: Int,
)
