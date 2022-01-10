package app.kobuggi.hyuabot.data.remote.response.events

import app.kobuggi.hyuabot.data.remote.domain.events.EventItem
import com.google.gson.annotations.SerializedName

data class EventsResponse(
    @SerializedName("events") val events : List<EventItem>
)
