package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName

data class ReadingRoomList(
    @SerializedName("rooms") val rooms : ArrayList<ReadingRoom>
)

data class ReadingRoom(
    @SerializedName("name") val name : String,
    @SerializedName("isActive") val isActive : Boolean,
    @SerializedName("isReservable") val isReservable : Boolean,
    @SerializedName("total") val total : Int,
    @SerializedName("activeTotal") val activeTotal : Int,
    @SerializedName("occupied") val occupied : Int,
    @SerializedName("available") val available : Int
)
