package app.kobuggi.hyuabot.data.remote.domain.events

import com.google.gson.annotations.SerializedName

data class EventItem(
    @SerializedName("title") val title: String,
    @SerializedName("start") val startDate: String,
    @SerializedName("end") val endDate: String
)