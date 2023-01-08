package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class StopRouteRunningTime(
    @SerializedName("weekdays") val weekdays: StopRouteFirstLastTime,
    @SerializedName("weekends") val weekends: StopRouteFirstLastTime,
)