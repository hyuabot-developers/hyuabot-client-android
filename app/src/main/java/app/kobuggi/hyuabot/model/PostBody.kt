package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName

data class CampusRequest(
    @SerializedName("campus") val campus: String
)
