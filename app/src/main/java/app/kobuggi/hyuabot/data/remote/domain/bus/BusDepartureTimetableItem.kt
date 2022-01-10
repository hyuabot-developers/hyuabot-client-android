package app.kobuggi.hyuabot.data.remote.domain.bus

import com.google.gson.annotations.SerializedName

data class BusDepartureTimetableItem(
    @SerializedName("time") val time: String
)
