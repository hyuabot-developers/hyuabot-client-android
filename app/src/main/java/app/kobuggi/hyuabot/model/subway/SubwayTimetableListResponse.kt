package app.kobuggi.hyuabot.model.subway

import com.google.gson.annotations.SerializedName

data class SubwayTimetableListResponse(
    @SerializedName("up") val up: List<SubwayTimetableItemResponse>,
    @SerializedName("down") val down: List<SubwayTimetableItemResponse>,
)
