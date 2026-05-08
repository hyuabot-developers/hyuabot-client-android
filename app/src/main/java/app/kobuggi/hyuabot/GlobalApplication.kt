package app.kobuggi.hyuabot

import android.app.Application
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
        applicationScope.launch {
            val enabled = userPreferencesRepository.analyticsConsent.first()
            FirebaseAnalytics.getInstance(applicationContext).setAnalyticsCollectionEnabled(enabled)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
        }
    }
}
