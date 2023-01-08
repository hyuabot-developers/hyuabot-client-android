package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class TimetableStopRouteItem(
    @SerializedName("name") val name: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("weekdays") val weekdays: List<String>,
    @SerializedName("weekends") val weekends: List<String>,
)