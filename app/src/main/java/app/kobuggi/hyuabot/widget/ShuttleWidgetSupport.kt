package app.kobuggi.hyuabot.widget

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

internal enum class ShuttleError { NONE, NO_PERMISSION, NO_LOCATION, NO_DATA }

internal data class ShuttleGroup(val destination: String, val times: List<String>)

internal object ShuttleWidgetSupport {
    val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private const val LOCATION_MAX_AGE_MILLIS = 60_000L

    fun hasLocationPermission(context: Context): Boolean {
        val foreground =
            ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!foreground) {
            return false
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }
        return ContextCompat.checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getLocation(
        context: Context,
        priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        maxAgeMillis: Long = LOCATION_MAX_AGE_MILLIS,
        currentTimeoutMillis: Long = 6000L
    ): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val last = withTimeoutOrNull(2000) { awaitTask(client.lastLocation) }
        if (last != null && isFresh(last, maxAgeMillis)) return last
        val tokenSource = CancellationTokenSource()
        val current = withTimeoutOrNull(currentTimeoutMillis) {
            awaitTask(client.getCurrentLocation(priority, tokenSource.token))
        }
        return current ?: last
    }

    private fun isFresh(location: Location, maxAgeMillis: Long): Boolean {
        val ageMillis = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000
        return ageMillis in 0..maxAgeMillis
    }

    private suspend fun <T> awaitTask(task: Task<T>): T? =
        suspendCancellableCoroutine { cont ->
            task.addOnSuccessListener { cont.resume(it) }
            task.addOnFailureListener { cont.resume(null) }
            task.addOnCanceledListener { cont.resume(null) }
        }

    fun distanceTo(stop: ShuttleWidgetQuery.Stop, location: Location): Double {
        val dLat = stop.latitude - location.latitude
        val dLng = stop.longitude - location.longitude
        return dLat * dLat + dLng * dLng
    }

    fun makeGroups(
        context: Context,
        timetable: ShuttleWidgetQuery.Timetable,
    ): List<ShuttleGroup> = timetable.destination.mapNotNull { group ->
        val times = group.entries.take(5).map { it.time.format(TIME_FORMAT) }
        if (times.isEmpty()) {
            null
        } else {
            ShuttleGroup(destinationDisplayName(context, group.destination), times)
        }
    }

    fun stopDisplayName(context: Context, name: String): String = when (name) {
        "dormitory_o" -> context.getString(R.string.shuttle_tab_dormitory_out)
        "shuttlecock_o" -> context.getString(R.string.shuttle_tab_shuttlecock_out)
        "station" -> context.getString(R.string.shuttle_tab_station)
        "terminal" -> context.getString(R.string.shuttle_tab_terminal)
        "jungang_stn" -> context.getString(R.string.shuttle_tab_jungang_station)
        "shuttlecock_i" -> context.getString(R.string.shuttle_tab_shuttlecock_in)
        else -> name
    }

    fun destinationDisplayName(context: Context, code: String): String = when (code) {
        "STATION" -> context.getString(R.string.shuttle_bound_for_station)
        "TERMINAL" -> context.getString(R.string.shuttle_bound_for_terminal)
        "JUNGANG" -> context.getString(R.string.shuttle_bound_for_jungang_station)
        "CAMPUS" -> context.getString(R.string.shuttle_bound_for_dormitory)
        else -> code
    }
}
