package app.kobuggi.hyuabot.service.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.NotificationService.Companion.NOTIFICATION_GROUP_ID
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var manager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private val scope by lazy { CoroutineScope(Dispatchers.IO) }

    companion object {
        const val ALARM_CHANNEL_ID = "ReadingRoomExtendNotification"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(ALARM_CHANNEL_ID, "Reading Room Extend Notification", NotificationManager.IMPORTANCE_HIGH))
        builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)

        val title = intent?.extras?.getString("content") ?: return
        val libraryAppIntent = context.packageManager.getLaunchIntentForPackage("kr.ac.hanyang.library")
        val notification = builder.setContentTitle(title)
            .setGroup(NOTIFICATION_GROUP_ID)
            .setContentTitle(context.getString(R.string.reading_room_extend_noti_title))
            .setContentText(context.getString(R.string.reading_room_extend_noti_content))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(libraryAppIntent?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE) })
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(999, notification)
        scope.launch { userPreferencesRepository.setReadingRoomExtendNotification(null) }
    }
}
