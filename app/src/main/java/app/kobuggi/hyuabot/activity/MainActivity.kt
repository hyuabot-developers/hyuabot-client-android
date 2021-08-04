package app.kobuggi.hyuabot.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.config.NetworkClient
import app.kobuggi.hyuabot.function.getDarkMode
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


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
                .withPrimaryTextTypefaceColor(textColor)
                .withSecondaryTextTypefaceColor(textColor)
                .build()
            template.setStyles(templateStyle)
            template.setNativeAd(it)
        }
        val adLoader = builder.build()
        adLoader.loadAd(AdRequest.Builder().build())

        val expandMenu = findViewById<LinearLayout>(R.id.expanded_menu)
        val expandMenuButton = findViewById<TextView>(R.id.expanded_menu_button)
        expandMenuButton.setOnClickListener {
            if(expandMenu.visibility == View.VISIBLE){
                expandMenu.visibility = View.GONE
                expandMenuButton.text = "전체 보기"
            } else {
                expandMenu.visibility = View.VISIBLE
                expandMenuButton.text = "줄이기"
            }
        }

        val shuttleMenuButton = findViewById<RelativeLayout>(R.id.menu_shuttle_button)
        val busMenuButton = findViewById<RelativeLayout>(R.id.menu_bus_button)
        busMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_bus, null))
        busMenuButton.findViewById<TextView>(R.id.button_label).text = "버스"

        val subwayMenuButton = findViewById<RelativeLayout>(R.id.menu_subway_button)
        subwayMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_metro, null))
        subwayMenuButton.findViewById<TextView>(R.id.button_label).text = "전철"

        val foodMenuButton = findViewById<RelativeLayout>(R.id.menu_food_button)
        foodMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_restaurant, null))
        foodMenuButton.findViewById<TextView>(R.id.button_label).text = "학식"

        val libraryMenuButton = findViewById<RelativeLayout>(R.id.menu_library_button)
        libraryMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_library, null))
        libraryMenuButton.findViewById<TextView>(R.id.button_label).text = "열람실"

        val contactMenuButton = findViewById<RelativeLayout>(R.id.menu_contact_button)
        contactMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_contact, null))
        contactMenuButton.findViewById<TextView>(R.id.button_label).text = "전화부"

        val mapMenuButton = findViewById<RelativeLayout>(R.id.menu_map_button)
        mapMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_map, null))
        mapMenuButton.findViewById<TextView>(R.id.button_label).text = "지도"

        val calendarMenuButton = findViewById<RelativeLayout>(R.id.menu_calendar_button)
        calendarMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_calendar, null))
        calendarMenuButton.findViewById<TextView>(R.id.button_label).text = "학사력"

        NetworkClient.getServer().getShuttleAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                    items -> Log.d("response", items.toString())
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (nativeAd != null) {
            nativeAd!!.destroy()
        }
    }
}