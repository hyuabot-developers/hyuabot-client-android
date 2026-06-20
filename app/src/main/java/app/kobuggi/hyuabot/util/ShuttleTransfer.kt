package app.kobuggi.hyuabot.util

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleTransferQuery
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

data class TransitRow(
    val name: String,
    @param:ColorRes val colorRes: Int,
    val detail: String,
    val vehicleType: TransitVehicleType,
    val timeline: List<TransitTimelineEntry> = emptyList(),
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

private const val BUS_STOP_KWANGMYEONG = 216000759
private const val BUS_STOP_ANSAN = 216000117

private data class TransferData(
    val subway: List<TransferSubwayStation>,
    val bus: List<TransferBus>,
)

private data class TransferSubwayStation(
    val stationID: String,
    val arrival: List<TransferSubwayArrival>,
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

private data class TransferBus(
    val stopSeq: Int,
    val arrival: List<TransferBusArrival>,
)

private data class TransferBusArrival(
    val minutes: Int?,
    val stops: Int?,
    val isRealtime: Boolean,
)

fun localizedContext(context: Context): Context {
    val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: return context
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config)
}

fun currentShuttleWeekday(): String = when (ZonedDateTime.now(ZoneId.of("Asia/Seoul")).dayOfWeek) {
    DayOfWeek.SATURDAY -> "saturday"
    DayOfWeek.SUNDAY -> "sunday"
    else -> "weekday"
}

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
            localizedStationName(context, entry.terminalID, entry.terminalName),
        )
    }
    val timeline = entries.map { (direction, entry) ->
        TransitTimelineEntry(
            destination = localizedStationName(context, entry.terminalID, entry.terminalName),
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
)

private fun localizedStationName(context: Context, stationID: String, fallback: String): String =
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
            )
        },
        bus = bus.map { item ->
            TransferBus(
                stopSeq = item.stop.seq,
                arrival = item.arrival.map { arrival ->
                    TransferBusArrival(arrival.minutes, arrival.stops, arrival.isRealtime)
                },
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
            )
        },
        bus = transferBus.map { item ->
            TransferBus(
                stopSeq = item.stop.seq,
                arrival = item.arrival.map { arrival ->
                    TransferBusArrival(arrival.minutes, arrival.stops, arrival.isRealtime)
                },
            )
        },
    )
