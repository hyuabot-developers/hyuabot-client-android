package app.kobuggi.hyuabot.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NotificationService", "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val topic = message.from?.substringAfterLast("/").orEmpty().split("reading_room_").last()
        runBlocking { userPreferencesRepository.turnOffNotification(topic.toInt()) }
        Firebase.messaging.unsubscribeFromTopic(message.from?.substringAfterLast("/").orEmpty()).addOnSuccessListener {
            Log.d("NotificationService", "Unsubscribed from reading room $topic")
        }.addOnFailureListener {
            Log.e("NotificationService", "Failed to unsubscribe from reading room $topic")
        }

        val notificationChannel = NotificationChannel(CHANNEL_ID, "Reading Room Notification", NotificationManager.IMPORTANCE_DEFAULT)
        getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setGroup(NOTIFICATION_GROUP_ID)
            .setContentTitle(getString(R.string.reading_room_noti_title))
            .setContentText(message.data["body"])
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(topic.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "ReadingRoomNotification"
        const val NOTIFICATION_GROUP_ID = "app.kobuggi.hyuabot.reading_room_notification"
    }
}
