package app.kobuggi.hyuabot.service.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepositoryImpl {
    suspend fun setBusStop(busStopID: Int)
    suspend fun getBusStop(): Flow<Int>
    suspend fun toggleReadingRoomNotification(readingRoomID: Int)
    suspend fun turnOffNotification(readingRoomID: Int)
    suspend fun setReadingRoomExtendNotification(timeString: String?)
    suspend fun setTheme(theme: String?)
    suspend fun setCampusID(campusID: Int)
    suspend fun getContactVersion(): Flow<String?>
    suspend fun setContactVersion(version: String)
}
