package app.kobuggi.hyuabot.model.library

import com.google.gson.annotations.SerializedName

data class ReadingRoomItemResponse(
    @SerializedName("room_id") val roomID: Int,
    @SerializedName("name") val roomName: String,
    @SerializedName("total") val totalSeat: Int,
    @SerializedName("active") val activeSeat: Int,
    @SerializedName("occupied") val occupiedSeat: Int,
    @SerializedName("available") val availableSeat: Int,
)
