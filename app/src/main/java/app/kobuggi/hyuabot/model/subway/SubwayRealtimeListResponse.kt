package app.kobuggi.hyuabot.model.subway

import app.kobuggi.hyuabot.component.card.shuttle.SubCardItem
import com.google.gson.annotations.SerializedName

data class SubwayRealtimeListResponse(
    @SerializedName("up") val up: List<SubwayRealtimeItemResponse>,
    @SerializedName("down") val down: List<SubwayRealtimeItemResponse>,
) : SubCardItem()
