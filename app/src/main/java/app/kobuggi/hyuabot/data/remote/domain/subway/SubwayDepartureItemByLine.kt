package app.kobuggi.hyuabot.data.remote.domain.subway

import com.google.gson.annotations.SerializedName

data class SubwayDepartureItemByLine(
    @SerializedName("realtime") val realtime: SubwayRealtimeList,
    @SerializedName("timetable") val timetable: SubwayTimetableList
)
