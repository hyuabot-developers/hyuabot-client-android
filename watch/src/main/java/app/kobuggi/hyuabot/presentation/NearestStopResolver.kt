package app.kobuggi.hyuabot.presentation

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object NearestStopResolver {
    const val MAX_LOCATION_ACCURACY_METERS = 500f
    const val MAX_STOP_DISTANCE_METERS = 3_000.0

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    private val stops = listOf(
        StopCoordinate("dormitory", 37.29339607529377, 126.83630604103446),
        StopCoordinate("shuttlecock", 37.29875417910844, 126.83784054072336),
        StopCoordinate("station", 37.309700971618255, 126.85207173389148),
        StopCoordinate("terminal", 37.319338173415936, 126.8455263115596),
        StopCoordinate("jungang", 37.31487247528457, 126.83963540399434),
    )

    fun resolve(latitude: Double, longitude: Double, accuracyMeters: Float): String? {
        if (accuracyMeters < 0 || accuracyMeters > MAX_LOCATION_ACCURACY_METERS) return null

        val nearest = stops.minByOrNull {
            distanceMeters(latitude, longitude, it.latitude, it.longitude)
        } ?: return null
        val distance = distanceMeters(latitude, longitude, nearest.latitude, nearest.longitude)
        return nearest.id.takeIf { distance <= MAX_STOP_DISTANCE_METERS }
    }

    private fun distanceMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
    ): Double {
        val latitudeDelta = Math.toRadians(endLatitude - startLatitude)
        val longitudeDelta = Math.toRadians(endLongitude - startLongitude)
        val startLatitudeRadians = Math.toRadians(startLatitude)
        val endLatitudeRadians = Math.toRadians(endLatitude)
        val haversine = sin(latitudeDelta / 2).pow(2) +
            cos(startLatitudeRadians) * cos(endLatitudeRadians) * sin(longitudeDelta / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(haversine))
    }

    private data class StopCoordinate(
        val id: String,
        val latitude: Double,
        val longitude: Double,
    )
}
