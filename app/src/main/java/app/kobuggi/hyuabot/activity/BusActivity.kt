package app.kobuggi.hyuabot.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.Bus
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
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class BusActivity : AppCompatActivity() {
    private lateinit var greenBusCardToStation : CardView
    private lateinit var greenBusCardToCampus : CardView
    private lateinit var blueBusCard : CardView
    private lateinit var redBusCard : CardView

    private lateinit var networkService: AppServerService
    private lateinit var disposable : Disposable
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus)

        // 광고 로드
        val builder = AdLoader.Builder(this, BuildConfig.admob_unit_id)
        val config = this.resources.configuration
        builder.forNativeAd{
            val template = findViewById<TemplateView>(R.id.bus_admob_template)
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

        greenBusCardToStation = findViewById(R.id.bus_card_10_1_to_station)
        greenBusCardToCampus = findViewById(R.id.bus_card_10_1_to_campus)
        blueBusCard = findViewById(R.id.bus_card_707_1_to_suwon)
        redBusCard = findViewById(R.id.bus_card_3102_to_seoul)

        greenBusCardToStation.findViewById<TextView>(R.id.bus_card_bus_stop).text = "한양대게스트하우스"
        greenBusCardToStation.findViewById<TextView>(R.id.bus_card_heading).text = "상록수역 방면"

        greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_bus_stop).text = "상록수역"
        greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_heading).text = "푸르지오6차후문 방면"

        blueBusCard.findViewById<TextView>(R.id.bus_card_line_id).text = "707-1"
        blueBusCard.findViewById<TextView>(R.id.bus_card_line_id).setTextColor(Color.parseColor("#0075C8"))
        blueBusCard.findViewById<TextView>(R.id.bus_card_bus_stop).text = "한양대정문"
        blueBusCard.findViewById<TextView>(R.id.bus_card_heading).text = "수원역 방면"

        redBusCard.findViewById<TextView>(R.id.bus_card_line_id).text = "3102"
        redBusCard.findViewById<TextView>(R.id.bus_card_line_id).setTextColor(Color.parseColor("#FF0000"))
        redBusCard.findViewById<TextView>(R.id.bus_card_bus_stop).text = "한양대게스트하우스"
        redBusCard.findViewById<TextView>(R.id.bus_card_heading).text = "강남역 방면"

        val toolbar = findViewById<Toolbar>(R.id.bus_app_bar)
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

        networkService.getShuttleAll()
            .subscribeOn(Schedulers.io())
            .subs
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    @SuppressLint("SetTextI18n")
    private fun updateBusDepartureInfo(data: Bus){
        val now = LocalTime.now()
        val day = LocalDate.now()
        
        // 10-1 상록수역 방향
        var realtime = data.greenBusForStation.realtime
        var timetable = when(day.dayOfWeek){
            DayOfWeek.SATURDAY -> data.greenBusForStation.timetable.sat.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            DayOfWeek.SUNDAY -> data.greenBusForStation.timetable.sun.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            else -> data.greenBusForStation.timetable.weekdays.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
        }

        when{
            realtime.size >= 2 -> {
                greenBusCardToStation.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                greenBusCardToStation.findViewById<TextView>(R.id.bus_card_next).text = "${realtime[1].time}분 (${realtime[1].location}전)"
            }
            realtime.size == 1 -> {
                greenBusCardToStation.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                if(timetable.isNotEmpty()){
                    greenBusCardToStation.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 10}분 (회차점 대기)"
                } else {
                    greenBusCardToStation.findViewById<TextView>(R.id.bus_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        greenBusCardToStation.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 10}분 (회차점 대기)"
                        greenBusCardToStation.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes() + 10}분 (회차점 대기)"
                    }
                    timetable.size == 1 -> {
                        greenBusCardToStation.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 10}분 (회차점 대기)"
                        greenBusCardToStation.findViewById<TextView>(R.id.bus_card_next).text = ""
                    }
                    else -> {
                        greenBusCardToStation.findViewById<TextView>(R.id.bus_card_next).text = "운행 종료"
                    }
                }
            }
        }

        // 10-1 푸르지오방향
        realtime = data.greenBusForCampus.realtime
        timetable = when(day.dayOfWeek){
            DayOfWeek.SATURDAY -> data.greenBusForStation.timetable.sat.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            DayOfWeek.SUNDAY -> data.greenBusForStation.timetable.sun.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            else -> data.greenBusForStation.timetable.weekdays.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
        }

        when{
            realtime.size >= 2 -> {
                greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_next).text = "${realtime[1].time}분 (${realtime[1].location}전)"
            }
            realtime.size == 1 -> {
                greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                if(timetable.isNotEmpty()){
                    greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                } else {
                    greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                        greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                    }
                    timetable.size == 1 -> {
                        greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                        greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_next).text = ""
                    }
                    else -> {
                        greenBusCardToCampus.findViewById<TextView>(R.id.bus_card_this).text = "운행 종료"
                    }
                }
            }
        }

        // 3102 강남역 방향
        realtime = data.redBus.realtime
        timetable = when(day.dayOfWeek){
            DayOfWeek.SATURDAY -> data.redBus.timetable.sat.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            DayOfWeek.SUNDAY -> data.redBus.timetable.sun.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            else -> data.redBus.timetable.weekdays.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
        }

        when{
            realtime.size >= 2 -> {
                redBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                redBusCard.findViewById<TextView>(R.id.bus_card_next).text = "${realtime[1].time}분 (${realtime[1].location}전)"
            }
            realtime.size == 1 -> {
                redBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                if(timetable.isNotEmpty()){
                    redBusCard.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                } else {
                    redBusCard.findViewById<TextView>(R.id.bus_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        redBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                        redBusCard.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                    }
                    timetable.size == 1 -> {
                        redBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                        redBusCard.findViewById<TextView>(R.id.bus_card_next).text = ""
                    }
                    else -> {
                        redBusCard.findViewById<TextView>(R.id.bus_card_next).text = "운행 종료"
                    }
                }
            }
        }

        // 707-1 수원역 방향
        realtime = data.blueBus.realtime
        timetable = when(day.dayOfWeek){
            DayOfWeek.SATURDAY -> data.blueBus.timetable.sat.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            DayOfWeek.SUNDAY -> data.blueBus.timetable.sun.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
            else -> data.blueBus.timetable.weekdays.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }
        }

        when{
            realtime.size >= 2 -> {
                blueBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                blueBusCard.findViewById<TextView>(R.id.bus_card_next).text = "${realtime[1].time}분 (${realtime[1].location}전)"
            }
            realtime.size == 1 -> {
                blueBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${realtime[0].time}분 (${realtime[0].location}전)"
                if(timetable.isNotEmpty()){
                    blueBusCard.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                } else {
                    blueBusCard.findViewById<TextView>(R.id.bus_card_next).text = "막차 운행중"
                }
            }
            else -> {
                when {
                    timetable.size >= 2 -> {
                        blueBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                        blueBusCard.findViewById<TextView>(R.id.bus_card_next).text = "${Duration.between(now, LocalTime.parse(timetable[1].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                    }
                    timetable.size == 1 -> {
                        blueBusCard.findViewById<TextView>(R.id.bus_card_this).text = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes() + 20}분 (회차점 대기)"
                        blueBusCard.findViewById<TextView>(R.id.bus_card_next).text = ""
                    }
                    else -> {
                        blueBusCard.findViewById<TextView>(R.id.bus_card_next).text = "운행 종료"
                    }
                }
            }
        }
    }
}