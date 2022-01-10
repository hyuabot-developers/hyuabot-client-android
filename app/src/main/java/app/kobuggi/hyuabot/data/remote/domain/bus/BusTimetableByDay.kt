package app.kobuggi.hyuabot.data.remote.domain.bus

import com.google.gson.annotations.SerializedName

data class BusTimetableByDay(
    @SerializedName("weekdays") val weekdays: List<BusDepartureTimetableItem>,
    @SerializedName("sat") val sat: List<BusDepartureTimetableItem>,
    @SerializedName("sun") val sun: List<BusDepartureTimetableItem>,
)