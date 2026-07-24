package app.kobuggi.hyuabot.util

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleTransferQuery
import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class TransitRow(
    val name: String,
    @param:ColorRes val colorRes: Int,
    val detail: String,
    val vehicleType: TransitVehicleType,
    val timeline: List<TransitTimelineEntry> = emptyList(),
    val compactTitle: String = name,
    val compactTrailing: String = detail,
    val connectorTitle: String? = null,
    val connectorTravelMinutes: Int? = null,
)

enum class TransitVehicleType {
    SUBWAY,
    BUS,
}

data class TransitTimelineEntry(
    val destination: String,
    val minutes: Int?,
    val stops: Int?,
    val locationLabel: String?,
    val isRealtime: Boolean,
    val direction: Int,
)

private const val SUBWAY_TRANSFER_MINUTES = 5
private const val CHOJI_TRANSFER_MINUTES = 8
private const val BUS_STOP_KWANGMYEONG = 216000759
private const val BUS_STOP_ANSAN = 216000117

private data class TransferData(
    val subway: List<TransferSubwayStation>,
    val bus: List<TransferBus>,
)

private data class TransferSubwayStation(
    val stationID: String,
    val arrival: List<TransferSubwayArrival>,
    val timetable: List<TransferSubwayTimetable>,
)

private data class TransferSubwayArrival(
    val direction: String,
    val entries: List<TransferSubwayEntry>,
)

private data class TransferSubwayEntry(
    val minutes: Int,
    val isRealtime: Boolean,
    val location: String?,
    val stops: Int?,
    val terminalID: String,
    val terminalName: String,
)

private data class TransferSubwayTimetable(
    val direction: String,
    val time: LocalTime,
    val terminalID: String,
    val terminalName: String,
)

private data class TransferBus(
    val stopSeq: Int,
    val arrival: List<TransferBusArrival>,
    val log: List<LocalTime>,
)

private data class TransferBusArrival(
    val minutes: Int?,
    val stops: Int?,
    val isRealtime: Boolean,
)

private data class SubwayCandidate(
    val lineName: String,
    @param:ColorRes val colorRes: Int,
    val terminalID: String,
    val terminalName: String,
    val arrivalDate: ZonedDateTime,
    val minutes: Int?,
    val stops: Int?,
    val direction: Int,
    val isRealtime: Boolean,
)

fun localizedContext(context: Context): Context {
    val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: return context
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config)
}

fun currentShuttleWeekday(): String =
    if (ZonedDateTime.now(ZoneId.of("Asia/Seoul")).dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
        "weekends"
    } else {
        "weekdays"
    }

fun shuttleBusLogReferenceDates(today: LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))): List<LocalDate> =
    listOf(today.minusDays(7), today.minusDays(2), today.minusDays(1))

fun buildTransitRows(
    context: Context,
    stopName: String,
    data: ShuttleTransferQuery.Data,
): List<TransitRow> = buildTransitRows(context, stopName, data.toTransferData())

fun buildTransitRows(
    context: Context,
    stopName: String,
    data: ShuttleRealtimePageQuery.Data,
): List<TransitRow> = buildTransitRows(context, stopName, data.toTransferData())

private fun buildTransitRows(
    context: Context,
    stopName: String,
    data: TransferData,
): List<TransitRow> = when (stopName) {
    "dormitory_o", "shuttlecock_o" -> listOfNotNull(
        subwayRow(context, data, "K449", R.string.subway_line4, R.color.subway_line4),
        subwayRow(context, data, "K251", R.string.subway_suin, R.color.subway_suin),
        busRow(context, data, BUS_STOP_KWANGMYEONG, R.string.transfer_bus_kwangmyeong),
    )
    "terminal" -> listOfNotNull(
        busRow(context, data, BUS_STOP_ANSAN, R.string.transfer_bus_ansan),
    )
    else -> emptyList()
}

private fun subwayRow(
    context: Context,
    data: TransferData,
    stationID: String,
    @StringRes nameRes: Int,
    @ColorRes colorRes: Int,
): TransitRow? {
    val station = data.subway.firstOrNull { it.stationID == stationID } ?: return null
    val entries = station.arrival.flatMap { group ->
        group.entries
            .filter { it.isRealtime }
            .take(1)
            .map { group.direction to it }
    }
    if (entries.isEmpty()) return null
    val detail = entries.joinToString("   ") { (_, entry) ->
        context.getString(
            R.string.transfer_subway_format,
            entry.minutes,
            localizedSubwayStationName(context, entry.terminalID, entry.terminalName),
        )
    }
    val timeline = entries.map { (direction, entry) ->
        TransitTimelineEntry(
            destination = localizedSubwayStationName(context, entry.terminalID, entry.terminalName),
            minutes = entry.minutes,
            stops = entry.stops,
            locationLabel = entry.location,
            isRealtime = entry.isRealtime,
            direction = subwayDirection(direction),
        )
    }
    return TransitRow(context.getString(nameRes), colorRes, detail, TransitVehicleType.SUBWAY, timeline)
}

private fun subwayDirection(direction: String): Int =
    when (direction) {
        "down", "1" -> 1
        else -> -1
    }

private val SUBWAY_STATION_NAMES: Map<String, Int> = mapOf(
    "K209" to R.string.subway_station_K209,
    "K210" to R.string.subway_station_K210,
    "K233" to R.string.subway_station_K233,
    "K246" to R.string.subway_station_K246,
    "K258" to R.string.subway_station_K258,
    "K272" to R.string.subway_station_K272,
    "K409" to R.string.subway_station_K409,
    "K411" to R.string.subway_station_K411,
    "K419" to R.string.subway_station_K419,
    "K433" to R.string.subway_station_K433,
    "K443" to R.string.subway_station_K443,
    "K444" to R.string.subway_station_K444,
    "K453" to R.string.subway_station_K453,
    "K456" to R.string.subway_station_K456,
    "S07" to R.string.subway_station_S07,
    "S11" to R.string.subway_station_S11,
    "S16" to R.string.subway_station_S16,
)

fun localizedSubwayStationName(context: Context, stationID: String, fallback: String): String =
    SUBWAY_STATION_NAMES[stationID]?.let { context.getString(it) } ?: fallback

private fun busRow(
    context: Context,
    data: TransferData,
    stopSeq: Int,
    @StringRes nameRes: Int,
): TransitRow? {
    val bus = data.bus.firstOrNull { it.stopSeq == stopSeq } ?: return null
    val arrivals = bus.arrival.filter { it.minutes != null }.take(2)
    if (arrivals.isEmpty()) return null
    val detail = arrivals.joinToString("   ") { arrival ->
        val minutes = context.getString(R.string.transfer_bus_minutes_format, arrival.minutes)
        if (arrival.stops != null) {
            minutes + context.getString(R.string.transfer_bus_stops_suffix, arrival.stops)
        } else {
            minutes
        }
    }
    val destination = context.getString(nameRes)
    val timeline = arrivals.map {
        TransitTimelineEntry(
            destination = destination,
            minutes = it.minutes,
            stops = it.stops,
            locationLabel = it.stops?.let { stops -> context.getString(R.string.transfer_bus_stops_suffix, stops).trim() },
            isRealtime = it.isRealtime,
            direction = -1,
        )
    }
    return TransitRow(destination, R.color.green_bus, detail, TransitVehicleType.BUS, timeline)
}

fun buildShuttleConnectionRows(
    context: Context,
    stopName: String,
    destination: String,
    shuttle: ShuttleRealtimePageQuery.Entry,
    data: ShuttleRealtimePageQuery.Data?,
    showBusTransfer: Boolean,
    showSubwayTransfer: Boolean,
    subwayDestination: HomeSubwayTransferDestination,
): List<TransitRow> {
    if (data == null || stopName !in setOf("dormitory_o", "shuttlecock_o")) return emptyList()
    val transferData = data.toTransferData()
    return when (destination) {
        "STATION" -> if (showSubwayTransfer) {
            stationConnectionRows(context, shuttle, transferData, subwayDestination)
        } else {
            emptyList()
        }
        "TERMINAL" -> if (showBusTransfer) {
            terminalBusConnectionRows(context, shuttle, transferData)
        } else {
            emptyList()
        }
        "JUNGANG" -> if (showSubwayTransfer) {
            jungangConnectionRows(context, shuttle, transferData, subwayDestination)
        } else {
            emptyList()
        }
        else -> emptyList()
    }
}

private fun stationConnectionRows(
    context: Context,
    shuttle: ShuttleRealtimePageQuery.Entry,
    data: TransferData,
    destination: HomeSubwayTransferDestination,
): List<TransitRow> {
    val transferStart = shuttleTransferDate(shuttle, "station")
    val line4 = subwayCandidates(context, data, "K449", "up", R.string.subway_line4, R.color.subway_line4)
    val suin = subwayCandidates(
        context,
        data,
        "K251",
        "up",
        R.string.home_transfer_subway_suin_bundang_badge,
        R.color.home_subway_yellow,
    )
    return when (destination) {
        HomeSubwayTransferDestination.SEOUL ->
            earliestCandidate(line4, transferStart, SUBWAY_TRANSFER_MINUTES)
                ?.let { listOf(candidateRow(context, it, transferStart, SUBWAY_TRANSFER_MINUTES)) }
                .orEmpty()
        HomeSubwayTransferDestination.SUWON_YONGIN ->
            earliestCandidate(suin, transferStart, SUBWAY_TRANSFER_MINUTES)
                ?.let { listOf(candidateRow(context, it, transferStart, SUBWAY_TRANSFER_MINUTES)) }
                .orEmpty()
        HomeSubwayTransferDestination.OIDO -> {
            val candidates = oidoFirstLegCandidates(context, data)
            earliestCandidate(candidates, transferStart, SUBWAY_TRANSFER_MINUTES)
                ?.let { listOf(candidateRow(context, it, transferStart, SUBWAY_TRANSFER_MINUTES)) }
                .orEmpty()
        }
        HomeSubwayTransferDestination.SOSA -> sosaRows(context, data, transferStart)
        HomeSubwayTransferDestination.INCHEON -> incheonRows(context, data, transferStart)
    }.mapIndexed { index, row ->
        if (index == 0) {
            row.copy(
                connectorTitle = context.getString(
                    R.string.shuttle_connection_transfer_format,
                    context.getString(R.string.home_transfer_subway_connector),
                ),
                connectorTravelMinutes = SUBWAY_TRANSFER_MINUTES,
            )
        } else {
            row
        }
    }
}

private fun sosaRows(
    context: Context,
    data: TransferData,
    transferStart: ZonedDateTime?,
): List<TransitRow> {
    val firstLegs = eligibleCandidates(
        chojiFirstLegCandidates(context, data),
        transferStart,
        SUBWAY_TRANSFER_MINUTES,
    )
    val secondLegs = subwayTimetableCandidates(
        context,
        data,
        "S26",
        "up",
        R.string.home_transfer_subway_seohae_badge,
        R.color.subway_seohae,
    ) { it <= "S16" && it.startsWith("S") }
    val path = firstLegs.mapNotNull { first ->
        val second = earliestCandidate(secondLegs, first.arrivalDate, CHOJI_TRANSFER_MINUTES)
            ?: return@mapNotNull null
        listOf(first, second)
    }.minByOrNull { it.last().arrivalDate } ?: return emptyList()
    return listOf(
        candidateRow(context, path[0], transferStart, SUBWAY_TRANSFER_MINUTES),
        candidateRow(
            context,
            path[1],
            path[0].arrivalDate,
            CHOJI_TRANSFER_MINUTES,
            context.getString(R.string.home_transfer_subway_choji_connector),
            CHOJI_TRANSFER_MINUTES,
        ),
    )
}

private fun incheonRows(
    context: Context,
    data: TransferData,
    transferStart: ZonedDateTime?,
): List<TransitRow> {
    val direct = subwayCandidates(
        context,
        data,
        "K251",
        "down",
        R.string.home_transfer_subway_suin_bundang_badge,
        R.color.home_subway_yellow,
    ) { it > "K258" && it.startsWith("K2") }
        .let { eligibleCandidates(it, transferStart, SUBWAY_TRANSFER_MINUTES) }
        .map(::listOf)
    val firstLegs = eligibleCandidates(
        oidoFirstLegCandidates(context, data),
        transferStart,
        SUBWAY_TRANSFER_MINUTES,
    )
    val secondLegs = subwayCandidates(
        context,
        data,
        "K258",
        "down",
        R.string.home_transfer_subway_suin_bundang_badge,
        R.color.home_subway_yellow,
    ) { it > "K258" && it.startsWith("K2") }
    val transfer = firstLegs.mapNotNull { first ->
        val second = earliestCandidate(secondLegs, first.arrivalDate, SUBWAY_TRANSFER_MINUTES)
            ?: return@mapNotNull null
        listOf(first, second)
    }
    val path = (direct + transfer).minByOrNull { it.last().arrivalDate } ?: return emptyList()
    return path.mapIndexed { index, candidate ->
        candidateRow(
            context = context,
            candidate = candidate,
            transferStart = if (index == 0) transferStart else path[index - 1].arrivalDate,
            travelMinutes = if (index == 0) SUBWAY_TRANSFER_MINUTES else null,
            connectorTitle = if (index == 0) null else context.getString(R.string.home_transfer_subway_oido_connector),
            connectorTravelMinutes = null,
        )
    }
}

private fun chojiFirstLegCandidates(context: Context, data: TransferData): List<SubwayCandidate> =
    subwayCandidates(
        context,
        data,
        "K449",
        "down",
        R.string.subway_line4,
        R.color.subway_line4,
    ) { it >= "K452" && it.startsWith("K4") } + subwayCandidates(
        context,
        data,
        "K251",
        "down",
        R.string.home_transfer_subway_suin_bundang_badge,
        R.color.home_subway_yellow,
    ) { it >= "K254" && it.startsWith("K2") }

private fun oidoFirstLegCandidates(context: Context, data: TransferData): List<SubwayCandidate> =
    subwayCandidates(
        context,
        data,
        "K449",
        "down",
        R.string.subway_line4,
        R.color.subway_line4,
    ) { it == "K456" } + subwayCandidates(
        context,
        data,
        "K251",
        "down",
        R.string.home_transfer_subway_suin_bundang_badge,
        R.color.home_subway_yellow,
    ) { it >= "K258" && it.startsWith("K2") }

private fun jungangConnectionRows(
    context: Context,
    shuttle: ShuttleRealtimePageQuery.Entry,
    data: TransferData,
    destination: HomeSubwayTransferDestination,
): List<TransitRow> {
    val direction = when (destination) {
        HomeSubwayTransferDestination.SEOUL -> "up"
        HomeSubwayTransferDestination.OIDO,
        HomeSubwayTransferDestination.SOSA -> "down"
        else -> return emptyList()
    }
    val transferStart = shuttleTransferDate(shuttle, "jungang_stn")
    val candidate = earliestCandidate(
        subwayCandidates(context, data, "K450", direction, R.string.subway_line4, R.color.subway_line4),
        transferStart,
        0,
    ) ?: return emptyList()
    return listOf(
        candidateRow(context, candidate, transferStart, null).copy(
            connectorTitle = context.getString(
                R.string.shuttle_connection_transfer_format,
                context.getString(R.string.shuttle_tab_jungang_station),
            ),
        ),
    )
}

private fun terminalBusConnectionRows(
    context: Context,
    shuttle: ShuttleRealtimePageQuery.Entry,
    data: TransferData,
): List<TransitRow> {
    val transferStart = shuttleTransferDate(shuttle, "terminal")
    val bus = data.bus.firstOrNull { it.stopSeq == BUS_STOP_KWANGMYEONG } ?: return emptyList()
    val realtime = bus.arrival
        .filter { it.isRealtime && it.minutes != null }
        .map { arrival ->
            val arrivalDate = nowInSeoul().plusMinutes(arrival.minutes!!.toLong())
            arrival to arrivalDate
        }
        .filter { (_, date) -> transferStart == null || !date.isBefore(transferStart) }
        .minByOrNull { (_, date) -> date }
    val arrivalDate = realtime?.second ?: bus.log
        .map(::upcomingDateTime)
        .filter { transferStart == null || !it.isBefore(transferStart) }
        .minOrNull()
        ?: return emptyList()
    val trailing = transferWaitingText(context, transferStart, arrivalDate, null)
    val detail = if (realtime != null) {
        realtime.first.stops?.let { context.getString(R.string.home_transfer_bus50_realtime_stops, it) }
            ?: context.getString(R.string.home_transfer_realtime_minutes, realtime.first.minutes)
    } else {
        context.getString(R.string.home_transfer_bus50_log_arrival_record, arrivalDate.hour, arrivalDate.minute)
    }
    val badge = context.getString(R.string.home_transfer_bus50_badge)
    return listOf(
        TransitRow(
            name = badge,
            colorRes = R.color.green_bus,
            detail = detail,
            vehicleType = TransitVehicleType.BUS,
            compactTitle = badge,
            compactTrailing = trailing ?: detail,
            connectorTitle = context.getString(
                R.string.shuttle_connection_transfer_format,
                context.getString(R.string.home_transfer_bus50_connector),
            ),
        ),
    )
}

private fun candidateRow(
    context: Context,
    candidate: SubwayCandidate,
    transferStart: ZonedDateTime?,
    travelMinutes: Int?,
    connectorTitle: String? = null,
    connectorTravelMinutes: Int? = null,
): TransitRow {
    val destination = localizedSubwayStationName(context, candidate.terminalID, candidate.terminalName)
    val detail = if (candidate.isRealtime) {
        candidate.stops?.let { context.getString(R.string.home_transfer_subway_realtime_stops, it) }
            ?: candidate.minutes?.let { context.getString(R.string.home_transfer_realtime_minutes, it) }
            ?: ""
    } else {
        context.getString(
            R.string.home_transfer_subway_timetable_arrival,
            candidate.arrivalDate.hour,
            candidate.arrivalDate.minute,
        )
    }
    return TransitRow(
        name = candidate.lineName,
        colorRes = candidate.colorRes,
        detail = detail,
        vehicleType = TransitVehicleType.SUBWAY,
        timeline = listOf(
            TransitTimelineEntry(
                destination = destination,
                minutes = candidate.minutes,
                stops = candidate.stops,
                locationLabel = null,
                isRealtime = candidate.isRealtime,
                direction = candidate.direction,
            ),
        ),
        compactTitle = context.getString(R.string.home_transfer_subway_title, destination),
        compactTrailing = transferWaitingText(context, transferStart, candidate.arrivalDate, travelMinutes) ?: detail,
        connectorTitle = connectorTitle,
        connectorTravelMinutes = connectorTravelMinutes,
    )
}

private fun subwayCandidates(
    context: Context,
    data: TransferData,
    stationID: String,
    direction: String,
    @StringRes lineNameRes: Int,
    @ColorRes colorRes: Int,
    isEligible: (String) -> Boolean = { true },
): List<SubwayCandidate> {
    val group = data.subway.firstOrNull { it.stationID == stationID }
        ?.arrival
        ?.firstOrNull { it.direction == direction }
        ?: return emptyList()
    val now = nowInSeoul()
    return group.entries.filter { isEligible(it.terminalID) }.map {
        SubwayCandidate(
            lineName = context.getString(lineNameRes),
            colorRes = colorRes,
            terminalID = it.terminalID,
            terminalName = it.terminalName,
            arrivalDate = now.plusMinutes(it.minutes.toLong()),
            minutes = it.minutes,
            stops = it.stops,
            direction = subwayDirection(direction),
            isRealtime = it.isRealtime,
        )
    }
}

private fun subwayTimetableCandidates(
    context: Context,
    data: TransferData,
    stationID: String,
    direction: String,
    @StringRes lineNameRes: Int,
    @ColorRes colorRes: Int,
    isEligible: (String) -> Boolean,
): List<SubwayCandidate> =
    data.subway.firstOrNull { it.stationID == stationID }
        ?.timetable
        ?.filter { it.direction == direction && isEligible(it.terminalID) }
        ?.map {
            val date = upcomingDateTime(it.time)
            SubwayCandidate(
                lineName = context.getString(lineNameRes),
                colorRes = colorRes,
                terminalID = it.terminalID,
                terminalName = it.terminalName,
                arrivalDate = date,
                minutes = Duration.between(nowInSeoul(), date).toMinutes().coerceAtLeast(0).toInt(),
                stops = null,
                direction = subwayDirection(direction),
                isRealtime = false,
            )
        }
        .orEmpty()

private fun earliestCandidate(
    candidates: List<SubwayCandidate>,
    transferStart: ZonedDateTime?,
    minimumTransferMinutes: Int,
): SubwayCandidate? =
    eligibleCandidates(candidates, transferStart, minimumTransferMinutes).minByOrNull { it.arrivalDate }

private fun eligibleCandidates(
    candidates: List<SubwayCandidate>,
    transferStart: ZonedDateTime?,
    minimumTransferMinutes: Int,
): List<SubwayCandidate> {
    if (transferStart == null) return candidates
    return candidates.filter {
        Duration.between(transferStart, it.arrivalDate).toMinutes() >= minimumTransferMinutes
    }
}

private fun shuttleTransferDate(
    shuttle: ShuttleRealtimePageQuery.Entry,
    stopName: String,
): ZonedDateTime? =
    shuttle.stops.firstOrNull { it.stop == stopName }?.time?.let(::upcomingDateTime)

private fun transferWaitingText(
    context: Context,
    transferStart: ZonedDateTime?,
    arrivalDate: ZonedDateTime,
    travelMinutes: Int?,
): String? {
    if (transferStart == null) return null
    val buffer = Duration.between(transferStart, arrivalDate).toMinutes().coerceAtLeast(0).toInt()
    val waiting = (buffer - (travelMinutes ?: 0)).coerceAtLeast(0)
    return if (waiting == 0) {
        context.getString(R.string.home_transfer_wait_immediate)
    } else {
        context.getString(R.string.home_transfer_wait_minutes, waiting)
    }
}

private fun upcomingDateTime(time: LocalTime): ZonedDateTime {
    val now = nowInSeoul()
    var result = now.toLocalDate().atTime(time).atZone(now.zone)
    if (result.isBefore(now)) result = result.plusDays(1)
    return result
}

private fun nowInSeoul(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

private fun ShuttleTransferQuery.Data.toTransferData(): TransferData =
    TransferData(
        subway = subway.map { station ->
            TransferSubwayStation(
                stationID = station.stationID,
                arrival = station.arrival.map { arrival ->
                    TransferSubwayArrival(
                        direction = arrival.direction,
                        entries = arrival.entries.map { entry ->
                            TransferSubwayEntry(
                                minutes = entry.minutes,
                                isRealtime = entry.isRealtime,
                                location = entry.location,
                                stops = entry.stops,
                                terminalID = entry.terminal.stationID,
                                terminalName = entry.terminal.name,
                            )
                        },
                    )
                },
                timetable = emptyList(),
            )
        },
        bus = bus.map { item ->
            TransferBus(
                stopSeq = item.stop.seq,
                arrival = item.arrival.map { arrival ->
                    TransferBusArrival(arrival.minutes, arrival.stops, arrival.isRealtime)
                },
                log = emptyList(),
            )
        },
    )

private fun ShuttleRealtimePageQuery.Data.toTransferData(): TransferData =
    TransferData(
        subway = subway.map { station ->
            TransferSubwayStation(
                stationID = station.stationID,
                arrival = station.arrival.map { arrival ->
                    TransferSubwayArrival(
                        direction = arrival.direction,
                        entries = arrival.entries.map { entry ->
                            TransferSubwayEntry(
                                minutes = entry.minutes,
                                isRealtime = entry.isRealtime,
                                location = entry.location,
                                stops = entry.stops,
                                terminalID = entry.terminal.stationID,
                                terminalName = entry.terminal.name,
                            )
                        },
                    )
                },
                timetable = station.timetable.map {
                    TransferSubwayTimetable(
                        direction = it.direction,
                        time = it.time,
                        terminalID = it.terminal.stationID,
                        terminalName = it.terminal.name,
                    )
                },
            )
        },
        bus = transferBus.map { item ->
            TransferBus(
                stopSeq = item.stop.seq,
                arrival = item.arrival.map { arrival ->
                    TransferBusArrival(arrival.minutes, arrival.stops, arrival.isRealtime)
                },
                log = item.log.map { it.time },
            )
        },
    )
