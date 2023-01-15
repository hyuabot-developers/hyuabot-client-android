package app.kobuggi.hyuabot.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging


class HYUBOTFCMService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "From: ${message.from}")
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + message.data)
            val roomID = message.from!!.split("/")[2]
            getSharedPreferences("hyuabot", 0).edit().putBoolean(roomID, false).apply()
            Firebase.messaging.unsubscribeFromTopic(roomID)
                .addOnCompleteListener {
                    Log.d(TAG, "unsubscribeFromTopic: $roomID")
                }
            sendNotification(message)
        } else {
            Log.d(TAG, "Message data payload: null")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        val pref = getSharedPreferences("hyuabot", 0)
        pref.edit().putString("fcm_token", token).apply()
    }

    private fun sendNotification(message: RemoteMessage) {
        val notificationID = (System.currentTimeMillis() / 7).toInt()

        val pendingIntent =  if (packageManager.getLaunchIntentForPackage("kr.ac.hanyang.library") != null) {
            val intent = packageManager.getLaunchIntentForPackage("kr.ac.hanyang.library")
            intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            PendingIntent.getActivity(this, notificationID, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            val marketURL = "market://details?id=kr.ac.hanyang.library"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(marketURL))
            PendingIntent.getActivity(this, notificationID, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        val channelID = getString(R.string.default_notification_channel_id)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.hanyang_library)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelID, "HYUABOT", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FCMService"
    }
}