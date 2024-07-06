package app.kobuggi.hyuabot.service.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.NotificationService.Companion.NOTIFICATION_GROUP_ID
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {
    private lateinit var manager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    companion object {
        const val ALARM_CHANNEL_ID = "ReadingRoomExtendNotification"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(ALARM_CHANNEL_ID, "Reading Room Extend Notification", NotificationManager.IMPORTANCE_DEFAULT))
        builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)

        val alarmIntent = Intent(context, AlarmService::class.java)
        val requestCode = intent?.extras!!.getInt("alarmRequestCode")
        val title = intent.extras!!.getString("content")

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            PendingIntent.getActivity(context, requestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = builder.setContentTitle(title)
            .setGroup(NOTIFICATION_GROUP_ID)
            .setContentTitle(context.getString(R.string.reading_room_extend_noti_title))
            .setContentText(context.getString(R.string.reading_room_extend_noti_content))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(999, notification)
    }
}
