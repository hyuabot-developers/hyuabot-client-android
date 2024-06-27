package app.kobuggi.hyuabot.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("NotificationService", "Refreshed token: $token")
    }
}
