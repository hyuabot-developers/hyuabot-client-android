package app.kobuggi.hyuabot.ui.shuttle.realtime

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

fun buildShuttleAlarmCheckpointTimes(
    routeStops: List<Pair<String, LocalTime>>,
    boardingStopId: String,
    departureTimeMillis: Long
): LongArray {
    val checkpointStops = shuttleAlarmCheckpointStops(routeStops, boardingStopId)
    if (checkpointStops.isEmpty()) return longArrayOf(departureTimeMillis)
    if (checkpointStops.size <= 1) return longArrayOf(departureTimeMillis)

    val zoneId = ZoneId.systemDefault()
    val departureDate = Instant.ofEpochMilli(departureTimeMillis).atZone(zoneId).toLocalDate()
    val checkpointTimes = LongArray(checkpointStops.size)
    checkpointTimes[checkpointTimes.lastIndex] = departureTimeMillis

    var nextTimeMillis = departureTimeMillis
    for (index in checkpointStops.lastIndex - 1 downTo 0) {
        var checkpointTime = departureDate.atTime(checkpointStops[index].second).atZone(zoneId)
        while (checkpointTime.toInstant().toEpochMilli() >= nextTimeMillis) {
            checkpointTime = checkpointTime.minusDays(1)
        }
        val checkpointTimeMillis = checkpointTime.toInstant().toEpochMilli()
        checkpointTimes[index] = checkpointTimeMillis
        nextTimeMillis = checkpointTimeMillis
    }

    return checkpointTimes
}

fun buildShuttleAlarmCheckpointStopIds(
    routeStops: List<Pair<String, LocalTime>>,
    boardingStopId: String
): List<String> {
    return shuttleAlarmCheckpointStops(routeStops, boardingStopId).map { it.first }
}

fun buildShuttleAlarmDestinationStopIds(
    routeStops: List<Pair<String, LocalTime>>,
    boardingStopId: String
): List<String> {
    return shuttleAlarmDestinationStops(routeStops, boardingStopId).map { it.first }
}

fun buildShuttleAlarmDestinationTimes(
    routeStops: List<Pair<String, LocalTime>>,
    boardingStopId: String,
    departureTimeMillis: Long
): LongArray {
    val destinationStops = shuttleAlarmDestinationStops(routeStops, boardingStopId)
    if (destinationStops.isEmpty()) return LongArray(0)

    val zoneId = ZoneId.systemDefault()
    val departureDate = Instant.ofEpochMilli(departureTimeMillis).atZone(zoneId).toLocalDate()
    val destinationTimes = LongArray(destinationStops.size)

    var previousTimeMillis = departureTimeMillis
    destinationStops.forEachIndexed { index, (_, time) ->
        var destinationTime = departureDate.atTime(time).atZone(zoneId)
        while (destinationTime.toInstant().toEpochMilli() <= previousTimeMillis) {
            destinationTime = destinationTime.plusDays(1)
        }
        val destinationTimeMillis = destinationTime.toInstant().toEpochMilli()
        destinationTimes[index] = destinationTimeMillis
        previousTimeMillis = destinationTimeMillis
    }

    return destinationTimes
}

fun shuttleAlarmLocationStopId(stopId: String): String {
    return when (stopId) {
        "dormitory_i" -> "dormitory_o"
        else -> stopId
    }
}

private fun shuttleAlarmCheckpointStops(
    routeStops: List<Pair<String, LocalTime>>,
    boardingStopId: String
): List<Pair<String, LocalTime>> {
    val boardingIndex = routeStops.indexOfFirst { it.first == boardingStopId }
    if (boardingIndex < 0) return emptyList()

    return routeStops.take(boardingIndex + 1)
}

private fun shuttleAlarmDestinationStops(
    routeStops: List<Pair<String, LocalTime>>,
    boardingStopId: String
): List<Pair<String, LocalTime>> {
    val boardingIndex = routeStops.indexOfFirst { it.first == boardingStopId }
    if (boardingIndex < 0 || boardingIndex >= routeStops.lastIndex) return emptyList()

    return routeStops.drop(boardingIndex + 1)
}
