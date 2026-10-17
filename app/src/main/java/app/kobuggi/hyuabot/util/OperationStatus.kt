package app.kobuggi.hyuabot.util

import java.time.LocalDate

enum class ItemStatus { STALE, IMMINENT_LAST_RUN, IMMINENT, LAST_RUN, REALTIME, SCHEDULED }

enum class TransportMode { SHUTTLE, BUS, SUBWAY }

sealed interface ListStatus {
    data object Error : ListStatus
    data class Halt(val noticeDate: LocalDate) : ListStatus
    data object Ended : ListStatus
    data object Normal : ListStatus
}
