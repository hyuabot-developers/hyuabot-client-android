package app.kobuggi.hyuabot.util

import java.time.LocalDate

/** App-side view of a service notice. Callers map from the Apollo-generated type. */
data class ServiceNoticeInput(
    val kind: String,
    val date: LocalDate,
    val holidayType: String?,
)

/** Returns [today] when a holiday notice of type "halt" exists for today, otherwise null. */
fun haltDateFor(notices: List<ServiceNoticeInput>, today: LocalDate): LocalDate? =
    notices.firstOrNull { it.kind == "holiday" && it.holidayType == "halt" && it.date == today }?.date
