package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class ArrivalListResponse(
    @SerializedName("stop") val stopList: List<ArrivalListStopItem>
)
