package app.kobuggi.hyuabot.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class WatchLocationProvider(context: Context) {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(LocationManager::class.java)

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): Location? = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
        suspendCancellableCoroutine { continuation ->
            val provider = preferredProvider()
            if (provider == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
            locationManager.getCurrentLocation(
                provider,
                cancellationSignal,
                appContext.mainExecutor,
            ) { location ->
                if (continuation.isActive) continuation.resume(location)
            }
        }
    }

    private fun preferredProvider(): String? = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.FUSED_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
    ).firstOrNull { provider ->
        locationManager.allProviders.contains(provider) && locationManager.isProviderEnabled(provider)
    }

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 8_000L
    }
}
