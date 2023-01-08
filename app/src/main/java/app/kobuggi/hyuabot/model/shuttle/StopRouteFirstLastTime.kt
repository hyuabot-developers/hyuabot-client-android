package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class StopRouteFirstLastTime(
    @SerializedName("first") val firstTime: String,
    @SerializedName("last") val lastTime: String,
)
