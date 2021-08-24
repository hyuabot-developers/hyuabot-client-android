package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class Events(
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class EventsJson(
    @SerializedName("events") val events : List<EventsJsonItem>
)

data class EventsJsonItem(
    @SerializedName("title") val title: String,
    @SerializedName("start") val startDate: String,
    @SerializedName("end") val endDate: String
)