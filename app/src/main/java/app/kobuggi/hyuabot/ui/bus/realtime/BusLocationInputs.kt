package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.type.BusRouteStopInput

/** One valid route per location candidate; fetched separately from arrival data. */
internal fun busLocationInputs(): List<BusRouteStopInput> = listOf(
    216000068 to 216000379, 216000068 to 216000381, 216000068 to 216000383,
    216000096 to 216000719,
    216000061 to 121000060, 216000061 to 121000929, 216000061 to 121000974,
    216000061 to 121000970, 216000061 to 121000220,
    216000104 to 216000070, 216000104 to 202000106,
).map { (route, stop) -> BusRouteStopInput(route = route, stop = stop) }
