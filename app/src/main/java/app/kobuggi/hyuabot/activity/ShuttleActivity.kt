package app.kobuggi.hyuabot.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.config.NetworkService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.Shuttle
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ShuttleActivity : AppCompatActivity() {

    private lateinit var shuttleCardResidenceToStation : CardView
    private lateinit var shuttleCardResidenceToTerminal : CardView
    private lateinit var shuttleCardShuttlecockToStation : CardView
    private lateinit var shuttleCardShuttlecockToTerminal : CardView
    private lateinit var shuttleCardStationToCampus : CardView
    private lateinit var shuttleCardStationToTerminal : CardView
    private lateinit var shuttleCardTerminalToCampus : CardView
    private lateinit var shuttleCardShuttlecockToResidence : CardView

    private lateinit var networkService: NetworkService
    private lateinit var disposable : Disposable
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")


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

        findViewById<Toolbar>(R.id.shuttle_app_bar).setNavigationOnClickListener {
            finish()
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()


        networkService = Retrofit.Builder()
            .baseUrl(BuildConfig.server_url)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(NetworkService::class.java)

        disposable = Observable.interval(0, 1, TimeUnit.MINUTES)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::callShuttleEndpoint, this::onError)
    }

    override fun onResume() {
        super.onResume()
        if (disposable.isDisposed) {
            disposable = Observable.interval(0, 1,
                TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callShuttleEndpoint, this::onError)
        }
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    @SuppressLint("CheckResult")
    private fun callShuttleEndpoint(aLong : Long){
        val observable = networkService.getShuttleAll()
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { data -> data }
            .subscribe(this::updateShuttleDepartureInfo, this::onError)
    }

    private fun onError(throwable: Throwable) {
        Log.d("Fetch Error", throwable.message!!)
    }


    @SuppressLint("SetTextI18n")
    private fun updateShuttleDepartureInfo(data: Shuttle) {
        val now = LocalTime.now()
        var timetable = data.Residence.forStation
        when {
            timetable.size >= 2 -> {
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
            }
        }

        timetable = data.Residence.forTerminal
        when {
            timetable.size >= 2 -> {
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_next).text = ""
            }
        }

        timetable = data.Shuttlecock_O.forStation
        when {
            timetable.size >= 2 -> {
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
            }
        }

        timetable = data.Shuttlecock_O.forTerminal
        when {
            timetable.size >= 2 -> {
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
            }
        }

        timetable = data.Subway.forStation
        when {
            timetable.size >= 2 -> {
                shuttleCardStationToCampus.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardStationToCampus.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardStationToCampus.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardStationToCampus.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardStationToCampus.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
            }
        }

        timetable = data.Subway.forTerminal
        when {
            timetable.size >= 2 -> {
                shuttleCardStationToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardStationToTerminal.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardStationToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardStationToTerminal.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardStationToTerminal.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
            }
        }

        timetable = data.Terminal.forTerminal
        when {
            timetable.size >= 2 -> {
                shuttleCardTerminalToCampus.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardTerminalToCampus.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardTerminalToCampus.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardTerminalToCampus.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardTerminalToCampus.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
            }
        }

        timetable = data.Shuttlecock_I.forTerminal
        when {
            timetable.size >= 2 -> {
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[1].type)})"
            }
            timetable.size == 1 -> {
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()}분 (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_next).text = "막차입니다."
            }
            else -> {
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_this).text = "운행 종료"
            }
        }
    }

    private fun getHeadingString(heading: String) : String{
        return  if (heading == "C") "순환" else "직행"
    }
}