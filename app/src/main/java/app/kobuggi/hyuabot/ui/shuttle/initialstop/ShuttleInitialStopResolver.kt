package app.kobuggi.hyuabot.ui.shuttle.initialstop

data class ShuttleGeoCoordinate(
    val latitude: Double,
    val longitude: Double,
)

data class ShuttleInitialStopRuleCandidate(
    val sequence: Int,
    val stopName: String,
    val priority: Int,
    val polygon: List<ShuttleGeoCoordinate>,
)

object ShuttleInitialStopResolver {
    fun resolve(
        latitude: Double,
        longitude: Double,
        rules: List<ShuttleInitialStopRuleCandidate>,
    ): String? {
        if (!latitude.isFinite() || !longitude.isFinite()) return null
        val location = ShuttleGeoCoordinate(latitude, longitude)
        return rules
            .sortedWith(compareByDescending<ShuttleInitialStopRuleCandidate> { it.priority }.thenBy { it.sequence })
            .firstOrNull { contains(location, it.polygon) }
            ?.stopName
    }

    internal fun contains(
        location: ShuttleGeoCoordinate,
        polygon: List<ShuttleGeoCoordinate>,
    ): Boolean {
        if (polygon.size < 3 || polygon.any { !it.latitude.isFinite() || !it.longitude.isFinite() }) {
            return false
        }

        var inside = false
        var previous = polygon.last()
        polygon.forEach { current ->
            if (isOnSegment(location, previous, current)) return true
            val crossesLatitude = (current.latitude > location.latitude) != (previous.latitude > location.latitude)
            if (crossesLatitude) {
                val intersectionLongitude =
                    (previous.longitude - current.longitude) *
                        (location.latitude - current.latitude) /
                        (previous.latitude - current.latitude) +
                        current.longitude
                if (location.longitude < intersectionLongitude) inside = !inside
            }
            previous = current
        }
        return inside
    }

    private fun isOnSegment(
        point: ShuttleGeoCoordinate,
        start: ShuttleGeoCoordinate,
        end: ShuttleGeoCoordinate,
    ): Boolean {
        val cross =
            (point.latitude - start.latitude) * (end.longitude - start.longitude) -
                (point.longitude - start.longitude) * (end.latitude - start.latitude)
        if (kotlin.math.abs(cross) > COORDINATE_EPSILON) return false
        return point.latitude >= minOf(start.latitude, end.latitude) - COORDINATE_EPSILON &&
            point.latitude <= maxOf(start.latitude, end.latitude) + COORDINATE_EPSILON &&
            point.longitude >= minOf(start.longitude, end.longitude) - COORDINATE_EPSILON &&
            point.longitude <= maxOf(start.longitude, end.longitude) + COORDINATE_EPSILON
    }

    private const val COORDINATE_EPSILON = 1e-10
}
