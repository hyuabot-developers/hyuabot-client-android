package app.kobuggi.hyuabot

import android.app.Application
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.kobuggi.hyuabot.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GlobalApplication : Application() {
    private val themePrefKey = stringPreferencesKey("theme")
    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate() {
        super.onCreate()
        val theme : Flow<String> = dataStore.data.map { it[themePrefKey] ?: "system"}
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            theme.collect{
                when (it) {
                    "system" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    "off" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    "on" -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
        }
        val localeCode = getSharedPreferences("hyuabot", 0).getString("locale", "")
        if (localeCode != "" && localeCode != null) {
            LocaleHelper.setLocale(localeCode)
        }
    }
}