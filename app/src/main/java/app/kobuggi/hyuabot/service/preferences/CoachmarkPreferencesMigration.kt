package app.kobuggi.hyuabot.service.preferences

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks

internal class CoachmarkPreferencesMigration(
    private val legacyAppDataExists: () -> Boolean,
) : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        MIGRATED_KEY !in currentData

    override suspend fun migrate(currentData: Preferences): Preferences {
        if (MIGRATED_KEY in currentData) return currentData
        val hasOldData = currentData.asMap().keys.any { it != MIGRATED_KEY } || legacyAppDataExists()
        return mutablePreferencesOf().also { migrated ->
            currentData.asMap().forEach { (key, value) -> migrated.putAny(key, value) }
            if (hasOldData) {
                migrated[INITIALIZED_KEY] = true
                val seen = migrated[SEEN_KEY] ?: emptySet()
                val reset = migrated[RESET_KEY] ?: emptySet()
                migrated[SEEN_KEY] = seen + (Coachmarks.USER_VISIBLE_KEYS - reset)
            }
            migrated[MIGRATED_KEY] = true
        }
    }

    override suspend fun cleanUp() = Unit

    @Suppress("UNCHECKED_CAST")
    private fun MutablePreferences.putAny(key: Preferences.Key<*>, value: Any) {
        this[key as Preferences.Key<Any>] = value
    }

    companion object {
        private val MIGRATED_KEY = booleanPreferencesKey("coachmark_eligibility_migrated_v3")
        private val INITIALIZED_KEY = booleanPreferencesKey("coachmark_initialized")
        private val SEEN_KEY = stringSetPreferencesKey("coachmarks_seen")
        private val RESET_KEY = stringSetPreferencesKey("coachmark_reset_keys")

        fun legacyAppDataExists(context: Context): Boolean =
            listOf("pref", "home_experience", "location_disclosure").any { name ->
                context.getSharedPreferences(name, Context.MODE_PRIVATE).all.isNotEmpty()
            }
    }
}
