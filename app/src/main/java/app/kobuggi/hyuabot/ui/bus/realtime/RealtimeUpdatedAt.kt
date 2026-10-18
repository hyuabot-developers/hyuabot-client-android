package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.BusRealtimePageQuery

import java.time.Instant

fun maxRealtimeUpdatedAt(updatedAts: Iterable<Instant?>): Instant? =
    updatedAts.filterNotNull().maxOrNull()

fun busSectionUpdatedAt(bus: BusRealtimePageQuery.Bus?): Instant? =
    bus?.realtime?.map { it.updatedAt.toInstant() }?.let(::maxRealtimeUpdatedAt)
