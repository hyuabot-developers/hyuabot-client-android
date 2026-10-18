package app.kobuggi.hyuabot.ui.bus.realtime

import java.time.Instant

fun maxRealtimeUpdatedAt(updatedAts: Iterable<Instant?>): Instant? =
    updatedAts.filterNotNull().maxOrNull()
