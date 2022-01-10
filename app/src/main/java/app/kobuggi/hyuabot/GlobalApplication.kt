package app.kobuggi.hyuabot

import android.app.Application
import app.kobuggi.hyuabot.di.apiModule
import app.kobuggi.hyuabot.di.databaseModule
import app.kobuggi.hyuabot.di.networkModule
import app.kobuggi.hyuabot.di.viewModelModule
import com.google.android.gms.ads.MobileAds
import org.koin.core.context.startKoin

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)

        startKoin{
            networkModule
            apiModule
            databaseModule
            viewModelModule
        }
    }
}