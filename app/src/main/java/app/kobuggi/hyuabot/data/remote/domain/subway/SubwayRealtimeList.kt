package app.kobuggi.hyuabot.data.remote.domain.subway

import com.google.gson.annotations.SerializedName

data class SubwayRealtimeList(
    @SerializedName("up") val upLine: List<SubwayRealtimeItem>,
    @SerializedName("down") val downLine: List<SubwayRealtimeItem>
)
