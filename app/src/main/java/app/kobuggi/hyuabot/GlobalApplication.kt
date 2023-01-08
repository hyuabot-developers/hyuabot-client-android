package app.kobuggi.hyuabot

import android.app.Application
import android.content.res.Resources
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        res = resources
    }
    companion object {
        private lateinit var res: Resources
        fun getAppResources() : Resources {
            return res
        }
    }
}