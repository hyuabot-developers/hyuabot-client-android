package app.kobuggi.hyuabot.ui.subway.realtime

import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import java.time.Instant

fun maxRealtimeUpdatedAt(updatedAts: Iterable<Instant?>): Instant? =
    updatedAts.filterNotNull().maxOrNull()

fun subwaySectionUpdatedAt(subway: SubwayRealtimePageQuery.Subway?): Instant? =
    subway?.realtime?.map { it.updatedAt.toInstant() }?.let(::maxRealtimeUpdatedAt)
