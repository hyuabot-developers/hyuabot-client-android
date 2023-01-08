package app.kobuggi.hyuabot.model.bus

import com.google.gson.annotations.SerializedName

data class BusTimetableResponse(
    @SerializedName("weekdays") val weekdays: List<String>,
    @SerializedName("saturday") val saturday: List<String>,
    @SerializedName("sunday") val sunday: List<String>,
)
