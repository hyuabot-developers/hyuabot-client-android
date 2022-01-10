package app.kobuggi.hyuabot.data.remote.response.subway

import app.kobuggi.hyuabot.model.SubwayByLine
import com.google.gson.annotations.SerializedName

data class SubwayERICAResponse(
    @SerializedName("main") val line4: SubwayByLine,
    @SerializedName("sub") val lineSuin: SubwayByLine
)
