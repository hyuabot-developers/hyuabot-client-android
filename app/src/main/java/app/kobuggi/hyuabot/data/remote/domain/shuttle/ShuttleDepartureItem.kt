package app.kobuggi.hyuabot.data.remote.domain.shuttle

import com.google.gson.annotations.SerializedName

data class ShuttleDepartureItem(
    @SerializedName("time") val time: String,
    @SerializedName("type") val type: String
)
