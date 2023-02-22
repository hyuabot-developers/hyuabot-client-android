package app.kobuggi.hyuabot

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GlobalApplication : Application() {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: GlobalApplication
            private set

        fun getApplicationContext(): Context {
            return instance.applicationContext
        }
    }
}