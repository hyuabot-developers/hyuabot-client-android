package app.kobuggi.hyuabot

import android.app.Application
import android.content.Context
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltAndroidApp
class GlobalApplication : Application() {
    @Inject @ApplicationContext
    lateinit var context: Context

    override fun onCreate() {
        super.onCreate()
        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY)
    }
}
