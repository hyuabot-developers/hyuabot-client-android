package app.kobuggi.hyuabot.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.CampusRequest
import app.kobuggi.hyuabot.model.SubwayERICA
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

class SubwayActivity : AppCompatActivity() {
    private lateinit var subwayCardLine4Seoul : CardView
    private lateinit var subwayCardLine4Oido : CardView
    private lateinit var subwayCardLineSuinSeoul : CardView
    private lateinit var subwayCardLineSuinIncheon : CardView

    private lateinit var networkService: AppServerService
    private lateinit var disposable : Disposable
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subway)

        // 광고 로드
        val builder = AdLoader.Builder(this, BuildConfig.admob_unit_id)
        val config = this.resources.configuration
        builder.forNativeAd{
            val template = findViewById<TemplateView>(R.id.subway_admob_template)
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
        subwayCardLine4Seoul = findViewById(R.id.subway_card_line_4_seoul)
        subwayCardLine4Oido = findViewById(R.id.subway_card_line_4_oido)
        subwayCardLineSuinSeoul = findViewById(R.id.subway_card_line_suin_seoul)
        subwayCardLineSuinIncheon = findViewById(R.id.subway_card_line_suin_incheon)

        // 각 카드 별 제목 및 부제목 지정
        subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_title).text = "4호선(한대앞역)"
        subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_subtitle).text = "서울·당고개 방면"

        subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_title).text = "4호선(한대앞역)"
        subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_subtitle).text = "안산·오이도 방면"

        subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_title).text = "수인분당선(한대앞역)"
        subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_subtitle).text = "서울·왕십리 방면"
        subwayCardLineSuinSeoul.findViewById<ImageView>(R.id.subway_current_circle).setImageResource(R.drawable.subway_stop_circle_line_suin)

        subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_title).text = "수인분당선(한대앞역)"
        subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_subtitle).text = "인천·오이도 방면"
        subwayCardLineSuinIncheon.findViewById<ImageView>(R.id.subway_current_circle).setImageResource(R.drawable.subway_stop_circle_line_suin)

        val toolbar = findViewById<Toolbar>(R.id.subway_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
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
            .build().create(AppServerService::class.java)

        disposable = Observable.interval(0, 1, TimeUnit.MINUTES)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::callAPIForSubwayActivity, this::onError)

    }

    override fun onResume() {
        super.onResume()
        if (disposable.isDisposed) {
            disposable = Observable.interval(0, 1,
                TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callAPIForSubwayActivity, this::onError)
        }
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    private fun callAPIForSubwayActivity(aLong: Long){
        callSubwayEndpoint()
    }

    @SuppressLint("CheckResult")
    private fun callSubwayEndpoint(){
        val observable = networkService.getSubwayERICA(CampusRequest(campus = "erica"))
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { data -> data }
            .subscribe(this::updateSubwayDepartureInfo, this::onError)
    }

    private fun onError(throwable: Throwable) {
        Log.d("Fetch Error", throwable.message!!)
    }


    @SuppressLint("SetTextI18n")
    private fun updateSubwayDepartureInfo(data: SubwayERICA) {
        val now = LocalTime.now()

        // 4호선 서울 방향
        var realtime = data.line4.realtime.upLine
        var timetable = data.line4.timetable.upLine

        when{
            realtime.size >= 2 -> {
                subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_next).text = "${realtime[1].time.toInt()}분 (${realtime[1].terminalStn}행)"
            }
            realtime.size == 1 -> {
                subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                if(timetable.size == 1){
                    subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                } else {
                    subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes()}분 (${timetable[1].terminalStn}행)"
                    }
                    timetable.size == 1 -> {
                        subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_next).text = ""
                    }
                    else -> {
                        subwayCardLine4Seoul.findViewById<TextView>(R.id.subway_card_next).text = "운행 종료"
                    }
                }
            }
        }

        // 4호선 오이도 방향
        realtime = data.line4.realtime.downLine
        timetable = data.line4.timetable.downLine

        when{
            realtime.size >= 2 -> {
                subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_next).text = "${realtime[1].time.toInt()}분 (${realtime[1].terminalStn}행)"
            }
            realtime.size == 1 -> {
                subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                if(timetable.size == 1){
                    subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                } else {
                    subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes()}분 (${timetable[1].terminalStn}행)"
                    }
                    timetable.size == 1 -> {
                        subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_next).text = ""
                    }
                    else -> {
                        subwayCardLine4Oido.findViewById<TextView>(R.id.subway_card_next).text = "운행 종료"
                    }
                }
            }
        }

        // 수인분당선 서울 방향
        realtime = data.lineSuin.realtime.upLine
        timetable = data.lineSuin.timetable.upLine

        when{
            realtime.size >= 2 -> {
                subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_next).text = "${realtime[1].time.toInt()}분 (${realtime[1].terminalStn}행)"
            }
            realtime.size == 1 -> {
                subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                if(timetable.size == 1){
                    subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                } else {
                    subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes()}분 (${timetable[1].terminalStn}행)"
                    }
                    timetable.size == 1 -> {
                        subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_next).text = ""
                    }
                    else -> {
                        subwayCardLineSuinSeoul.findViewById<TextView>(R.id.subway_card_next).text = "운행 종료"
                    }
                }
            }
        }

        // 수인분당선 인천 방향
        realtime = data.lineSuin.realtime.downLine
        timetable = data.lineSuin.timetable.downLine

        when{
            realtime.size >= 2 -> {
                subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_next).text = "${realtime[1].time.toInt()}분 (${realtime[1].terminalStn}행)"
            }
            realtime.size == 1 -> {
                subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_this).text = "${realtime[0].time.toInt()}분 (${realtime[0].terminalStn}행)"
                if(timetable.size == 1){
                    subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                } else {
                    subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes()}분 (${timetable[1].terminalStn}행)"
                    }
                    timetable.size == 1 -> {
                        subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes()}분 (${timetable[0].terminalStn}행)"
                        subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_next).text = ""
                    }
                    else -> {
                        subwayCardLineSuinIncheon.findViewById<TextView>(R.id.subway_card_next).text = "운행 종료"
                    }
                }
            }
        }
    }
}