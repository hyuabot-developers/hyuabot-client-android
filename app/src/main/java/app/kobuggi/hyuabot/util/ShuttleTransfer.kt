package app.kobuggi.hyuabot.util

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import app.kobuggi.hyuabot.R
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
)

private const val BUS_STOP_KWANGMYEONG = 216000759
private const val BUS_STOP_ANSAN = 216000117

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
    data: ShuttleTransferQuery.Data,
    stationID: String,
    @StringRes nameRes: Int,
    @ColorRes colorRes: Int,
): TransitRow? {
    val station = data.subway.firstOrNull { it.stationID == stationID } ?: return null
    val entries = station.arrival.mapNotNull { group ->
        group.entries.firstOrNull()
    }
    if (entries.isEmpty()) return null
    val detail = entries.joinToString("   ") {
        context.getString(
            R.string.transfer_subway_format,
            it.minutes,
            localizedStationName(context, it.terminal.stationID, it.terminal.name),
        )
    }
    val timeline = entries.map {
        TransitTimelineEntry(
            destination = localizedStationName(context, it.terminal.stationID, it.terminal.name),
            minutes = it.minutes,
            stops = it.stops,
            locationLabel = it.location,
            isRealtime = it.isRealtime,
        )
    }
    return TransitRow(context.getString(nameRes), colorRes, detail, TransitVehicleType.SUBWAY, timeline)
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
    data: ShuttleTransferQuery.Data,
    stopSeq: Int,
    @StringRes nameRes: Int,
): TransitRow? {
    val bus = data.bus.firstOrNull { it.stop.seq == stopSeq } ?: return null
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
        )
    }
    return TransitRow(destination, R.color.green_bus, detail, TransitVehicleType.BUS, timeline)
}
