package app.kobuggi.hyuabot.ui.bus.timetable

import java.time.LocalTime

data class BusTimetableItem (
    val routeName: String,
    val weekdays: String,
    val time: LocalTime,
)
