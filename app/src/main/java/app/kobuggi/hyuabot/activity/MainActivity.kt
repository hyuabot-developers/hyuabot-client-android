package app.kobuggi.hyuabot.activity

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions


class MainActivity : AppCompatActivity() {
    var nativeAd : NativeAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 광고 로드
        val builder = AdLoader.Builder(this, BuildConfig.admob_unit_id)
        builder.forNativeAd{
            val template = findViewById<TemplateView>(R.id.home_admob_template)
            template.setNativeAd(it)
        }
        val adLoader = builder.build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (nativeAd != null) {
            nativeAd!!.destroy()
        }
    }
}