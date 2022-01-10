package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName

data class CampusRequest(
    @SerializedName("campus") val campus: String
)

data class ShuttleStopRequest(
    @SerializedName("busStop") val shuttleStop: String
)
