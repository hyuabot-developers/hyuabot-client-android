package app.kobuggi.hyuabot.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.ui.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NotificationService", "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val topicName = message.from?.substringAfterLast("/").orEmpty()
        val readingRoomKey = message.data["id"]
            ?.takeIf { it.isNotBlank() }
            ?: topicName.takeIf { it.startsWith("reading_room_") }
        val readingRoomId = readingRoomKey?.removePrefix("reading_room_")?.toIntOrNull()
        if (readingRoomId != null) {
            serviceScope.launch { userPreferencesRepository.turnOffNotification(readingRoomId) }
            Firebase.messaging.unsubscribeFromTopic(topicName.ifBlank { readingRoomKey.orEmpty() }).addOnSuccessListener {
                Log.d("NotificationService", "Unsubscribed from reading room $readingRoomId")
            }.addOnFailureListener {
                Log.e("NotificationService", "Failed to unsubscribe from reading room $readingRoomId")
            }
        }

        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.reading_room_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)

        val notifyId = readingRoomId ?: System.currentTimeMillis().toInt()
        val contentIntent = message.data["url"]
            ?.takeIf { it.isNotBlank() }
            ?.let { url ->
                Intent(Intent.ACTION_VIEW, url.toUri(), this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            ?: packageManager.getLaunchIntentForPackage("kr.ac.hanyang.library")
        val availableSeats = message.data["available"]?.takeIf { it.isNotBlank() }
        val readingRoomName = readingRoomKey?.let(::readingRoomName)
        val notificationTitle = if (readingRoomName != null && availableSeats != null) {
            getString(R.string.reading_room_notification_title_format, readingRoomName)
        } else {
            message.data["title"] ?: message.notification?.title ?: getString(R.string.reading_room_noti_title)
        }
        val notificationBody = if (readingRoomName != null && availableSeats != null) {
            getString(R.string.reading_room_notification_body_format, availableSeats)
        } else {
            message.data["body"] ?: message.notification?.body
        }
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setGroup(NOTIFICATION_GROUP_ID)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(
                contentIntent?.let {
                    PendingIntent.getActivity(this, notifyId, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                }
            )
            .setAutoCancel(true)
            .build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(notifyId, notification)
    }

    private fun readingRoomName(key: String): String = getString(
        when (key) {
            "reading_room_1" -> R.string.reading_room_1
            "reading_room_53" -> R.string.reading_room_53
            "reading_room_54" -> R.string.reading_room_54
            "reading_room_55" -> R.string.reading_room_55
            "reading_room_56" -> R.string.reading_room_56
            "reading_room_61" -> R.string.reading_room_61
            "reading_room_63" -> R.string.reading_room_63
            "reading_room_131" -> R.string.reading_room_131
            "reading_room_132" -> R.string.reading_room_132
            else -> R.string.reading_room_unknown
        }
    )

    companion object {
        const val CHANNEL_ID = "ReadingRoomNotification"
        const val NOTIFICATION_GROUP_ID = "app.kobuggi.hyuabot.reading_room_notification"
    }
}
