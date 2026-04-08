package app.kobuggi.hyuabot.ui.subway.timetable

import app.kobuggi.hyuabot.SubwayTimetablePageQuery.Terminal

data class SubwayTimetableItem(
    val weekday: String,
    val direction: String,
    val time: String,
    val terminal: Terminal,
)
