package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class StopLocationItem(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
)
