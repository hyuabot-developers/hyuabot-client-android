package app.kobuggi.hyuabot.util

import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

/** The caller passes one bus stop or one subway station at a time. */
object TransitFreshnessChecker {
    private const val BUS_STALE_AFTER_SECONDS = 120L
    private const val SUBWAY_STALE_AFTER_SECONDS = 180L

    fun latestUpdate(timestamps: Iterable<ZonedDateTime?>): Instant? =
        timestamps.mapNotNull { it?.toInstant() }.maxOrNull()

    fun isBusStale(timestamps: Iterable<ZonedDateTime?>, now: Instant): Boolean =
        isStale(latestUpdate(timestamps), now, BUS_STALE_AFTER_SECONDS)

    fun isSubwayStale(timestamps: Iterable<ZonedDateTime?>, now: Instant): Boolean =
        isStale(latestUpdate(timestamps), now, SUBWAY_STALE_AFTER_SECONDS)

    fun staleBusUpdates(stations: Iterable<List<ZonedDateTime?>>, now: Instant): List<Instant> =
        stations.filter { isBusStale(it, now) }.mapNotNull(::latestUpdate)

    fun staleSubwayUpdates(stations: Iterable<List<ZonedDateTime?>>, now: Instant): List<Instant> =
        stations.filter { isSubwayStale(it, now) }.mapNotNull(::latestUpdate)

    fun ageMinutes(latestUpdate: Instant?, now: Instant): Long? = latestUpdate?.let {
        Duration.between(it, now).toMinutes().coerceAtLeast(0)
    }

    private fun isStale(latestUpdate: Instant?, now: Instant, thresholdSeconds: Long): Boolean =
        latestUpdate?.plusSeconds(thresholdSeconds)?.let { !now.isBefore(it) } ?: false
}
