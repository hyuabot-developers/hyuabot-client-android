package app.kobuggi.hyuabot.util

import java.time.LocalDate
import java.time.ZonedDateTime

const val IMMINENT_THRESHOLD_MINUTES = 3

/** Imminent when remaining minutes is 0, 1, or 2. Negative values are never imminent. */
fun isImminent(remainingMinutes: Int): Boolean = remainingMinutes in 0 until IMMINENT_THRESHOLD_MINUTES

/** Priority: STALE > IMMINENT_LAST_RUN > IMMINENT > LAST_RUN > REALTIME > SCHEDULED. */
fun judgeItem(
    remainingMinutes: Int,
    isRealtime: Boolean,
    isLastRun: Boolean,
    isStale: Boolean = false,
): ItemStatus {
    val imminent = isImminent(remainingMinutes)
    return when {
        isStale -> ItemStatus.STALE
        imminent && isLastRun -> ItemStatus.IMMINENT_LAST_RUN
        imminent -> ItemStatus.IMMINENT
        isLastRun -> ItemStatus.LAST_RUN
        isRealtime -> ItemStatus.REALTIME
        else -> ItemStatus.SCHEDULED
    }
}

/**
 * Priority: ERROR > HALT (shuttle only) > ENDED > NORMAL.
 *
 * HALT applies only when [haltDate] equals the KST calendar date of [now].
 * ENDED applies when no items remain, or when BUS/SUBWAY is in the late-night cutoff.
 */
fun judgeList(
    mode: TransportMode,
    now: ZonedDateTime,
    hasError: Boolean,
    haltDate: LocalDate?,
    remainingCount: Int,
): ListStatus {
    if (hasError) return ListStatus.Error
    val kstNow = now.withZoneSameInstant(ServiceClock.ZONE)
    if (mode == TransportMode.SHUTTLE && haltDate != null && haltDate == kstNow.toLocalDate()) {
        return ListStatus.Halt(haltDate)
    }
    if (remainingCount == 0) return ListStatus.Ended
    if (mode != TransportMode.SHUTTLE && ServiceClock.isLateNightCutoff(kstNow)) return ListStatus.Ended
    return ListStatus.Normal
}
