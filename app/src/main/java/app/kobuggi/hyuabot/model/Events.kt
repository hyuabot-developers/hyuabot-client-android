package app.kobuggi.hyuabot.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class Events(
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
