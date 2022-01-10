package app.kobuggi.hyuabot.data.remote.response.readingRoom

import app.kobuggi.hyuabot.model.ReadingRoom
import com.google.gson.annotations.SerializedName

data class ReadingRoomResponse(
    @SerializedName("rooms") val rooms : ArrayList<ReadingRoom>
)