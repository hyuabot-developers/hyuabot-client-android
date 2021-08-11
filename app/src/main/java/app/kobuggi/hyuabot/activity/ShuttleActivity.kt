package app.kobuggi.hyuabot.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.function.getDarkMode
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest

class ShuttleActivity : AppCompatActivity() {

    private lateinit var shuttleCardResidenceToStation : CardView
    private lateinit var shuttleCardResidenceToTerminal : CardView
    private lateinit var shuttleCardShuttlecockToStation : CardView
    private lateinit var shuttleCardShuttlecockToTerminal : CardView
    private lateinit var shuttleCardStationToCampus : CardView
    private lateinit var shuttleCardStationToTerminal : CardView
    private lateinit var shuttleCardTerminalToCampus : CardView
    private lateinit var shuttleCardShuttlecockToResidence : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shuttle)

        // 광고 로드
        val builder = AdLoader.Builder(this, BuildConfig.admob_unit_id)
        val config = this.resources.configuration
        builder.forNativeAd{
            val template = findViewById<TemplateView>(R.id.shuttle_admob_template)
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

        // 각 카드 ID 별로 변수 할당
        shuttleCardResidenceToStation = findViewById(R.id.shuttle_card_dorm_to_station)
        shuttleCardResidenceToTerminal = findViewById(R.id.shuttle_card_dorm_to_terminal)
        shuttleCardShuttlecockToStation = findViewById(R.id.shuttle_card_shuttlecock_to_station)
        shuttleCardShuttlecockToTerminal = findViewById(R.id.shuttle_card_shuttlecock_to_terminal)
        shuttleCardStationToCampus = findViewById(R.id.shuttle_card_station_to_campus)
        shuttleCardStationToTerminal = findViewById(R.id.shuttle_card_station_to_terminal)
        shuttleCardTerminalToCampus = findViewById(R.id.shuttle_card_terminal_to_campus)
        shuttleCardShuttlecockToResidence = findViewById(R.id.shuttle_card_shuttlecock_to_residence)

        // 각 카드 별 제목 및 부제목 지정
        shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "기숙사"
        shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_heading).text = "한대앞역 방면"

        shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "기숙사"
        shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_heading).text = "예술인 방면"

        shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "셔틀콕"
        shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_heading).text = "한대앞역 방면"

        shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "셔틀콕"
        shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_heading).text = "예술인 방면"

        shuttleCardStationToCampus.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "한대앞역"
        shuttleCardStationToCampus.findViewById<TextView>(R.id.shuttle_card_heading).text = "기숙사, 셔틀콕 방면"

        shuttleCardStationToTerminal.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "한대앞역"
        shuttleCardStationToTerminal.findViewById<TextView>(R.id.shuttle_card_heading).text = "예술인 방면"

        shuttleCardTerminalToCampus.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "예술인"
        shuttleCardTerminalToCampus.findViewById<TextView>(R.id.shuttle_card_heading).text = "기숙사, 셔틀콕 방면"

        shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_bus_stop).text = "셔틀콕 건너편"
        shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_heading).text = "기숙사 방면"
    }
}