package app.kobuggi.hyuabot

import android.app.Application
import androidx.room.Room
import app.kobuggi.hyuabot.function.AppDatabase
import com.google.android.gms.ads.MobileAds
import java.io.File

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}