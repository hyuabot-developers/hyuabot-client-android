package app.kobuggi.hyuabot.service.alarm

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AlarmService: Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
