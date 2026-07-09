package app.kobuggi.hyuabot.service.alarm

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.MainActivity

class ShuttleServiceNoticeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val noticeId = intent.getStringExtra(EXTRA_NOTICE_ID).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE) ?: context.getString(R.string.shuttle_service_notice_title)
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val notification = NotificationCompat.Builder(context, context.getString(R.string.shuttle_alarm_channel_id))
            .setSmallIcon(R.drawable.ic_notification_shuttle)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    noticeId.hashCode(),
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(noticeId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_NOTICE_ID = "notice_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }
}
