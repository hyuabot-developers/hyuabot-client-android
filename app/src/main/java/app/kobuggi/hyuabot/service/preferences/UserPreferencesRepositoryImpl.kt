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
    suspend fun setCalendarVersion(version: String)
    suspend fun setShowShuttleDepartureTime(show: Boolean)
    suspend fun getShowShuttleDepartureTime(): Flow<Boolean>
    suspend fun setShowShuttleByDestination(show: Boolean)
    suspend fun getShowShuttleByDestination(): Flow<Boolean>
    suspend fun setShowShuttlePresence(show: Boolean)
    suspend fun getShowShuttlePresence(): Flow<Boolean>
    suspend fun setShowHomeBus50Transfer(show: Boolean)
    suspend fun getShowHomeBus50Transfer(): Flow<Boolean>
    suspend fun setShowHomeSubwayTransfer(show: Boolean)
    suspend fun getShowHomeSubwayTransfer(): Flow<Boolean>
    suspend fun setHomeSubwayTransferDestination(destination: String)
    suspend fun getHomeSubwayTransferDestination(): Flow<String>
    suspend fun setAnalyticsConsent(enabled: Boolean)
    suspend fun incrementLaunchCount(): Int
    suspend fun resetLaunchCount()
    suspend fun setReviewRequestedAt(timestamp: Long)
    fun coachmarkSeen(screen: String): Flow<Boolean>
    suspend fun markCoachmarkSeen(screen: String)
    suspend fun resetCoachmark(screen: String)
    suspend fun resetCoachmarks()
    suspend fun syncCoachmarkEligibility(isFreshInstall: Boolean, allCoachmarkKeys: Set<String>)
}
