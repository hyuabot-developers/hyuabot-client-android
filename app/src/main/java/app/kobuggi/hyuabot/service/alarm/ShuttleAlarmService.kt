package app.kobuggi.hyuabot.service.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.widget.ShuttleWidgetSupport
import com.google.android.gms.location.Priority
import kotlin.math.ceil
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ShuttleAlarmService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var updateJob: Job? = null

    companion object {
        const val ACTION_START_BOARDING = "app.kobuggi.hyuabot.START_BOARDING_ALARM"
        const val ACTION_START_ALIGHTING = "app.kobuggi.hyuabot.START_ALIGHTING_ALARM"
        const val ACTION_CANCEL = "app.kobuggi.hyuabot.CANCEL_ALARM"

        const val EXTRA_STOP_NAME = "extra_stop_name"
        const val EXTRA_STOP_LAT = "extra_stop_lat"
        const val EXTRA_STOP_LNG = "extra_stop_lng"
        const val EXTRA_MINUTES = "extra_minutes"
        const val EXTRA_DEPARTURE_TIME_MILLIS = "extra_departure_time_millis"
        const val EXTRA_ALARM_KEY = "extra_alarm_key"
        const val EXTRA_DEST_STOP_NAME = "extra_dest_stop_name"
        const val EXTRA_DEST_STOP_LAT = "extra_dest_stop_lat"
        const val EXTRA_DEST_STOP_LNG = "extra_dest_stop_lng"

        const val NOTIFICATION_ID_ONGOING = 2001
        const val NOTIFICATION_ID_ALERT = 2002
        private const val UPDATE_INTERVAL_MS = 5_000L
        private const val LOCATION_MAX_AGE_MS = 15_000L
        private const val LOCATION_TIMEOUT_MS = 3_000L

        enum class ActiveAlarmType {
            BOARDING,
            ALIGHTING
        }

        @Volatile
        private var activeAlarmType: ActiveAlarmType? = null

        @Volatile
        private var activeAlarmLabel: String? = null

        @Volatile
        private var activeAlarmKey: String? = null

        fun activeAlarmType(): ActiveAlarmType? = activeAlarmType
        fun activeAlarmLabel(): String? = activeAlarmLabel
        fun activeAlarmKey(): String? = activeAlarmKey
        fun isBoardingAlarmActive(alarmKey: String): Boolean = activeAlarmType == ActiveAlarmType.BOARDING && activeAlarmKey == alarmKey
        fun isAlightingAlarmActive(alarmKey: String): Boolean = activeAlarmType == ActiveAlarmType.ALIGHTING && activeAlarmKey == alarmKey

        fun buildAlarmKey(boardingStopId: String, timetableSeq: Int): String {
            return "$boardingStopId|$timetableSeq"
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                val alarmKey = intent.getStringExtra(EXTRA_ALARM_KEY)
                if (alarmKey == null || alarmKey == activeAlarmKey) {
                    stopAlarm()
                }
                return START_NOT_STICKY
            }
            ACTION_START_BOARDING -> {
                val stopName = intent.getStringExtra(EXTRA_STOP_NAME) ?: return START_NOT_STICKY
                val stopLat = intent.getDoubleExtra(EXTRA_STOP_LAT, 0.0)
                val stopLng = intent.getDoubleExtra(EXTRA_STOP_LNG, 0.0)
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)
                val departureTimeMillis = intent.getLongExtra(
                    EXTRA_DEPARTURE_TIME_MILLIS,
                    System.currentTimeMillis() + minutes * 60_000L
                )
                val alarmKey = intent.getStringExtra(EXTRA_ALARM_KEY).orEmpty()
                startBoardingAlert(alarmKey, stopName, stopLat, stopLng, departureTimeMillis)
            }
            ACTION_START_ALIGHTING -> {
                val destName = intent.getStringExtra(EXTRA_DEST_STOP_NAME) ?: return START_NOT_STICKY
                val destLat = intent.getDoubleExtra(EXTRA_DEST_STOP_LAT, 0.0)
                val destLng = intent.getDoubleExtra(EXTRA_DEST_STOP_LNG, 0.0)
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)
                val alarmKey = intent.getStringExtra(EXTRA_ALARM_KEY).orEmpty()
                startAlightingAlert(alarmKey, destName, destLat, destLng, minutes)
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startBoardingAlert(alarmKey: String, stopName: String, stopLat: Double, stopLng: Double, departureTimeMillis: Long) {
        val cancelPi = buildCancelPendingIntent()
        activeAlarmType = ActiveAlarmType.BOARDING
        activeAlarmLabel = stopName
        activeAlarmKey = alarmKey
        val initialMinutes = remainingMinutesUntil(departureTimeMillis)
        val totalMinutes = initialMinutes.coerceAtLeast(1)
        val notification = buildBoardingNotification(stopName, initialMinutes, null, null, cancelPi, 0)
        startForeground(NOTIFICATION_ID_ONGOING, notification)

        updateJob?.cancel()
        updateJob = serviceScope.launch {
            val stopDeadline = departureTimeMillis + UPDATE_INTERVAL_MS
            while (isActive && System.currentTimeMillis() < stopDeadline) {
                val remainingMinutes = remainingMinutesUntil(departureTimeMillis)
                val progress = boardingProgress(totalMinutes, remainingMinutes)
                val location = getAlarmLocation()
                if (location != null) {
                    val result = FloatArray(2)
                    Location.distanceBetween(location.latitude, location.longitude, stopLat, stopLng, result)
                    val distance = result[0].toInt()
                    val direction = bearingToDirection(result[1].toDouble())
                    updateBoardingNotification(stopName, remainingMinutes, distance, direction, cancelPi, progress)
                } else {
                    updateBoardingNotification(stopName, remainingMinutes, null, null, cancelPi, progress)
                }
                if (System.currentTimeMillis() >= departureTimeMillis) {
                    stopAlarm()
                    return@launch
                }
                delay(UPDATE_INTERVAL_MS)
            }
            stopAlarm()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAlightingAlert(alarmKey: String, destStopName: String, destLat: Double, destLng: Double, minutes: Int) {
        val cancelPi = buildCancelPendingIntent()
        activeAlarmType = ActiveAlarmType.ALIGHTING
        activeAlarmLabel = destStopName
        activeAlarmKey = alarmKey
        val notification = buildAlightingNotification(destStopName, null, cancelPi)
        startForeground(NOTIFICATION_ID_ONGOING, notification)

        updateJob?.cancel()
        updateJob = serviceScope.launch {
            val timeout = System.currentTimeMillis() + (minutes + 60) * 60_000L
            while (isActive && System.currentTimeMillis() < timeout) {
                val location = getAlarmLocation()
                if (location != null) {
                    val result = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, destLat, destLng, result)
                    val distance = result[0].toInt()
                    if (distance <= 500) {
                        fireAlightingAlert(destStopName, distance)
                        stopAlarm()
                        return@launch
                    }
                    updateAlightingNotification(destStopName, distance, cancelPi)
                }
                delay(UPDATE_INTERVAL_MS)
            }
            stopAlarm()
        }
    }

    private fun buildCancelPendingIntent(): PendingIntent {
        val cancelIntent = Intent(this, ShuttleAlarmService::class.java).apply {
            action = ACTION_CANCEL
        }
        return PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private suspend fun getAlarmLocation(): Location? {
        return ShuttleWidgetSupport.getLocation(
            context = applicationContext,
            priority = Priority.PRIORITY_HIGH_ACCURACY,
            maxAgeMillis = LOCATION_MAX_AGE_MS,
            currentTimeoutMillis = LOCATION_TIMEOUT_MS
        )
    }

    private fun updateBoardingNotification(stopName: String, minutes: Int, distanceM: Int?, direction: String?, cancelPi: PendingIntent, progress: Int) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_ONGOING, buildBoardingNotification(stopName, minutes, distanceM, direction, cancelPi, progress))
    }

    private fun updateAlightingNotification(destStopName: String, distanceM: Int, cancelPi: PendingIntent) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_ONGOING, buildAlightingNotification(destStopName, distanceM, cancelPi))
    }

    private fun fireAlightingAlert(destStopName: String, distanceM: Int) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, getString(R.string.shuttle_alarm_channel_id))
            .setContentTitle(getString(R.string.shuttle_alarm_alighting_alert_title))
            .setContentText(getString(R.string.shuttle_alarm_alighting_alert_content, destStopName, distanceM))
            .setSmallIcon(R.drawable.ic_notification_shuttle)
            .setColor(ContextCompat.getColor(this, R.color.hanyang_blue))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID_ALERT, notification)
    }

    private fun buildBoardingNotification(
        stopName: String,
        minutes: Int,
        distanceM: Int?,
        direction: String?,
        cancelPi: PendingIntent,
        progress: Int
    ): Notification {
        val title = getString(R.string.shuttle_alarm_boarding_title, stopName)
        val content = if (distanceM != null && direction != null) {
            getString(R.string.shuttle_alarm_boarding_content, minutes, direction, distanceM)
        } else {
            getString(R.string.shuttle_alarm_boarding_initial, minutes)
        }
        val shortText = if (minutes > 0) getString(R.string.shuttle_alarm_boarding_short, minutes) else getString(R.string.shuttle_alarm_now)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            buildLiveUpdateNotification(title, content, shortText, progress, cancelPi)
        } else {
            NotificationCompat.Builder(this, getString(R.string.shuttle_alarm_channel_id))
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification_shuttle)
                .setColor(ContextCompat.getColor(this, R.color.hanyang_blue))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, progress, false)
                .addAction(android.R.drawable.ic_delete, getString(R.string.shuttle_alarm_cancel), cancelPi)
                .build()
        }
    }

    private fun buildAlightingNotification(destStopName: String, distanceM: Int?, cancelPi: PendingIntent): Notification {
        val title = getString(R.string.shuttle_alarm_alighting_title, destStopName)
        val content = if (distanceM != null) {
            getString(R.string.shuttle_alarm_alighting_content, distanceM)
        } else {
            getString(R.string.shuttle_alarm_alighting_preparing)
        }
        val shortText = if (distanceM != null) {
            getString(R.string.shuttle_alarm_distance_short, distanceM)
        } else {
            getString(R.string.shuttle_alarm_tracking_short)
        }
        val progress = distanceM?.let { alightingProgress(it) } ?: 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            buildLiveUpdateNotification(title, content, shortText, progress, cancelPi)
        } else {
            NotificationCompat.Builder(this, getString(R.string.shuttle_alarm_channel_id))
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification_shuttle)
                .setColor(ContextCompat.getColor(this, R.color.hanyang_blue))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, progress, distanceM == null)
                .addAction(android.R.drawable.ic_delete, getString(R.string.shuttle_alarm_cancel), cancelPi)
                .build()
        }
    }

    private fun buildLiveUpdateNotification(
        title: String,
        content: String,
        shortText: String,
        progress: Int,
        cancelPi: PendingIntent
    ): Notification {
        val color = ContextCompat.getColor(this, R.color.hanyang_blue)
        val style = Notification.ProgressStyle()
            .addProgressSegment(Notification.ProgressStyle.Segment(100).setColor(color))
            .setProgress(progress.coerceIn(0, 100))
            .setProgressStartIcon(Icon.createWithResource(this, R.drawable.ic_live_update_shuttle).setTint(color))
            .setProgressTrackerIcon(Icon.createWithResource(this, R.drawable.ic_live_update_arrow_right).setTint(color))
            .setStyledByProgress(true)
        return Notification.Builder(this, getString(R.string.shuttle_alarm_channel_id))
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification_shuttle)
            .setColor(color)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setRequestPromotedOngoing(true)
            .setShortCriticalText(shortText)
            .setStyle(style)
            .addAction(android.R.drawable.ic_delete, getString(R.string.shuttle_alarm_cancel), cancelPi)
            .build()
    }

    private fun boardingProgress(totalMinutes: Int, remainingMinutes: Int): Int {
        if (totalMinutes <= 0) return 100
        return (((totalMinutes - remainingMinutes).coerceAtLeast(0) * 100) / totalMinutes).coerceIn(0, 100)
    }

    private fun remainingMinutesUntil(timeMillis: Long): Int {
        return ceil((timeMillis - System.currentTimeMillis()) / 60_000.0).toInt().coerceAtLeast(0)
    }

    private fun alightingProgress(distanceM: Int): Int {
        val cappedDistance = min(distanceM, 2_000)
        return (100 - cappedDistance / 20).coerceIn(0, 100)
    }

    private fun stopAlarm() {
        updateJob?.cancel()
        updateJob = null
        activeAlarmType = null
        activeAlarmLabel = null
        activeAlarmKey = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun bearingToDirection(bearing: Double): String {
        val b = ((bearing % 360) + 360) % 360
        return when {
            b < 22.5 || b >= 337.5 -> getString(R.string.direction_north)
            b < 67.5 -> getString(R.string.direction_northeast)
            b < 112.5 -> getString(R.string.direction_east)
            b < 157.5 -> getString(R.string.direction_southeast)
            b < 202.5 -> getString(R.string.direction_south)
            b < 247.5 -> getString(R.string.direction_southwest)
            b < 292.5 -> getString(R.string.direction_west)
            else -> getString(R.string.direction_northwest)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        activeAlarmType = null
        activeAlarmLabel = null
        activeAlarmKey = null
        updateJob?.cancel()
        serviceScope.cancel()
    }
}
