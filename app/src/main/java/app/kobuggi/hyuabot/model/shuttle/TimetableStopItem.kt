package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class TimetableStopItem(
    @SerializedName("name") val name: String,
    @SerializedName("route") val route: List<TimetableStopRouteItem>,
)
