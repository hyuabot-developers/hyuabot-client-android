package app.kobuggi.hyuabot.service

import android.util.Log
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
    }
}
