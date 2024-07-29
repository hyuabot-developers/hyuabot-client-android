package app.kobuggi.hyuabot.service.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.kobuggi.hyuabot.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(private val userDataStorePreferences: DataStore<Preferences>) : UserPreferencesRepositoryImpl {
    val readingRoomNotifications: Flow<Set<String>> = userDataStorePreferences.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[READING_ROOM_NOTIFICATIONS_KEY] ?: emptySet()
        }

    val readingRoomExtendNotification: Flow<String> = userDataStorePreferences.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[READING_ROOM_EXTEND_NOTIFICATION_KEY] ?: ""
        }

    val theme: Flow<String?> = userDataStorePreferences.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[THEME_KEY]
        }

    override suspend fun setBusStop(busStopID: Int) {
        Result.runCatching {
            userDataStorePreferences.edit { preferences ->
                preferences[BUS_STOP_ID_KEY] = busStopID
            }
        }
    }

    override suspend fun getBusStop(): Flow<Int> {
        return userDataStorePreferences.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[BUS_STOP_ID_KEY] ?: R.string.bus_stop_convention
            }
    }

    override suspend fun toggleReadingRoomNotification(readingRoomID: Int) {
        Result.runCatching {
            userDataStorePreferences.edit { preferences ->
                val currentNotifications: Set<String> = preferences[READING_ROOM_NOTIFICATIONS_KEY] ?: emptySet()
                if (currentNotifications.contains("reading_room_$readingRoomID")) {
                    preferences[READING_ROOM_NOTIFICATIONS_KEY] = currentNotifications - "reading_room_$readingRoomID"
                } else {
                    preferences[READING_ROOM_NOTIFICATIONS_KEY] = currentNotifications + "reading_room_$readingRoomID"
                }
            }
        }
    }

    override suspend fun turnOffNotification(readingRoomID: Int) {
        Result.runCatching {
            userDataStorePreferences.edit { preferences ->
                val currentNotifications: Set<String> = preferences[READING_ROOM_NOTIFICATIONS_KEY] ?: emptySet()
                preferences[READING_ROOM_NOTIFICATIONS_KEY] = currentNotifications - "reading_room_$readingRoomID"
            }
        }
    }

    override suspend fun setReadingRoomExtendNotification(timeString: String?) {
        Result.runCatching {
            userDataStorePreferences.edit { preferences ->
                preferences[READING_ROOM_EXTEND_NOTIFICATION_KEY] = timeString ?: ""
            }
        }
    }

    override suspend fun setTheme(theme: String?) {
        Result.runCatching {
            if (theme != null) {
                userDataStorePreferences.edit { preferences ->
                    preferences[THEME_KEY] = theme
                }
            } else {
                userDataStorePreferences.edit { preferences ->
                    preferences.remove(THEME_KEY)
                }
            }
        }
    }

    private companion object {
        private val BUS_STOP_ID_KEY = intPreferencesKey("bus_stop_id")
        private val READING_ROOM_NOTIFICATIONS_KEY = stringSetPreferencesKey("reading_room_notifications")
        private val READING_ROOM_EXTEND_NOTIFICATION_KEY = stringPreferencesKey("reading_room_extend_notification")
        private val THEME_KEY = stringPreferencesKey("theme")
    }
}
