package app.kobuggi.hyuabot.data.remote.domain.subway

import com.google.gson.annotations.SerializedName

data class SubwayTimetableList(
    @SerializedName("up") val upLine: List<SubwayTimetableItem>,
    @SerializedName("down") val downLine: List<SubwayTimetableItem>
)