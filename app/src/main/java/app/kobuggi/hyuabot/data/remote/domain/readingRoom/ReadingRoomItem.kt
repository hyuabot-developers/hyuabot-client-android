package app.kobuggi.hyuabot.data.remote.domain.readingRoom

import com.google.gson.annotations.SerializedName

data class ReadingRoomItem(
    @SerializedName("name") val name : String,
    @SerializedName("isActive") val isActive : Boolean,
    @SerializedName("isReservable") val isReservable : Boolean,
    @SerializedName("total") val total : Int,
    @SerializedName("activeTotal") val activeTotal : Int,
    @SerializedName("occupied") val occupied : Int,
    @SerializedName("available") val available : Int
)
