package app.kobuggi.hyuabot

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GlobalApplication : Application() {
    @Inject @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        applicationScope.launch {
            val enabled = userPreferencesRepository.analyticsConsent.first()
            FirebaseAnalytics.getInstance(applicationContext).setAnalyticsCollectionEnabled(enabled)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
        }
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            getString(R.string.shuttle_alarm_channel_id),
            getString(R.string.shuttle_alarm_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.shuttle_alarm_channel_desc)
        }
        nm.createNotificationChannel(channel)
    }
}
