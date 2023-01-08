package app.kobuggi.hyuabot.model.shuttle

import com.google.gson.annotations.SerializedName

data class TimetableResponse (
    @SerializedName("stop") val stop: List<TimetableStopItem>,
)