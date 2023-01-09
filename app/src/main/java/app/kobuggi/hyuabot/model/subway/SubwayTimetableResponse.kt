package app.kobuggi.hyuabot.model.subway

import com.google.gson.annotations.SerializedName

data class SubwayTimetableResponse (
    @SerializedName("weekdays") val weekdays: SubwayTimetableListResponse,
    @SerializedName("weekends") val weekends: SubwayTimetableListResponse,
)
