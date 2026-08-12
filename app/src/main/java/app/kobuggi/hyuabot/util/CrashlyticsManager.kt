package app.kobuggi.hyuabot.util

import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics

/** Keeps emulator-generated reports out of the production Crashlytics data. */
object CrashlyticsManager {
    fun setCollectionEnabled(userConsent: Boolean) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled =
            userConsent && !isEmulator()
    }
}

internal fun isEmulator(build: EmulatorBuild = EmulatorBuild.current()): Boolean =
    build.fingerprint.startsWith("generic") ||
        build.fingerprint.startsWith("unknown") ||
        build.fingerprint.contains("emulator") ||
        build.model.contains("google_sdk", ignoreCase = true) ||
        build.model.contains("emulator", ignoreCase = true) ||
        build.model.contains("android sdk built for", ignoreCase = true) ||
        build.manufacturer.contains("genymotion", ignoreCase = true) ||
        (build.brand.startsWith("generic") && build.device.startsWith("generic")) ||
        build.product.contains("sdk", ignoreCase = true) ||
        build.product.contains("emulator", ignoreCase = true) ||
        build.hardware.equals("goldfish", ignoreCase = true) ||
        build.hardware.equals("ranchu", ignoreCase = true)

internal data class EmulatorBuild(
    val fingerprint: String,
    val model: String,
    val manufacturer: String,
    val brand: String,
    val device: String,
    val product: String,
    val hardware: String,
) {
    companion object {
        fun current() = EmulatorBuild(
            fingerprint = Build.FINGERPRINT,
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            brand = Build.BRAND,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            hardware = Build.HARDWARE,
        )
    }
}
