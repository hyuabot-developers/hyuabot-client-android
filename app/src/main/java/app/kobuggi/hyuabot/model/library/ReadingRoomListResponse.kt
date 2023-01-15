package app.kobuggi.hyuabot.model.library

import com.google.gson.annotations.SerializedName

data class ReadingRoomListResponse(
    @SerializedName("room") val roomList: List<ReadingRoomItemResponse>
)
