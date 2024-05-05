package app.kobuggi.hyuabot.service.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import app.kobuggi.hyuabot.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(private val userDataStorePreferences: DataStore<Preferences>) : UserPreferencesRepositoryImpl {
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

    private companion object {
        private val BUS_STOP_ID_KEY = intPreferencesKey("bus_stop_id")
    }
}
