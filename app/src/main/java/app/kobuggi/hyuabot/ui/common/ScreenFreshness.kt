package app.kobuggi.hyuabot.ui.common

import java.time.Instant

/** Returns the oldest section reference among sections that contain realtime data. */
fun screenReferenceTime(sectionTimes: List<Instant?>): Instant? =
    sectionTimes.filterNotNull().minOrNull()
