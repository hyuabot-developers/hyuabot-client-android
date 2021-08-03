package app.kobuggi.hyuabot.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.function.getDarkMode
import com.google.android.ads.nativetemplates.NativeTemplateStyle
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
        val config = this.resources.configuration
        builder.forNativeAd{
            val template = findViewById<TemplateView>(R.id.home_admob_template)
            val bgColor = ColorDrawable(if(getDarkMode(config)) Color.BLACK else Color.WHITE)
            val textColor = if(getDarkMode(config)) Color.WHITE else Color.BLACK
            val templateStyle = NativeTemplateStyle.Builder()
                .withMainBackgroundColor(bgColor)
                .withPrimaryTextBackgroundColor(bgColor)
                .withSecondaryTextBackgroundColor(bgColor)
                .withTertiaryTextBackgroundColor(bgColor)
                .withCallToActionBackgroundColor(bgColor)
                .withPrimaryTextTypefaceColor(textColor)
                .withSecondaryTextTypefaceColor(textColor)
                .withTertiaryTextTypefaceColor(textColor)
                .build()
            template.setStyles(templateStyle)
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