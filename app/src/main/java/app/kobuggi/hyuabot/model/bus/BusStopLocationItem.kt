package app.kobuggi.hyuabot.model.bus

import com.google.gson.annotations.SerializedName

data class BusStopLocationItem(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
)
