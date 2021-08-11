package app.kobuggi.hyuabot.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.RestaurantCardListAdapter
import app.kobuggi.hyuabot.config.NetworkService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.RestaurantList
import app.kobuggi.hyuabot.model.Shuttle
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
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


class MainActivity : AppCompatActivity() {
    var nativeAd : NativeAd? = null
    lateinit var networkService : NetworkService
    lateinit var disposable : Disposable

    private lateinit var shuttleCardResidenceToStation : CardView
    private lateinit var shuttleCardResidenceToTerminal : CardView
    private lateinit var shuttleCardShuttlecockToStation : CardView
    private lateinit var shuttleCardShuttlecockToTerminal : CardView
    private lateinit var shuttleCardStation : CardView
    private lateinit var shuttleCardTerminal : CardView
    private lateinit var shuttleCardShuttlecockToResidence : CardView
    private lateinit var restaurantCardListAdapter: RestaurantCardListAdapter

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

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
        shuttleMenuButton.setOnClickListener { openShuttleActivity() }

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

        shuttleCardResidenceToStation = findViewById(R.id.shuttle_card_dorm_to_station)
        shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_title).text = "기숙사 → 한대앞"
        
        shuttleCardResidenceToTerminal = findViewById(R.id.shuttle_card_dorm_to_terminal)
        shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_title).text = "기숙사 → 예술인"

        shuttleCardShuttlecockToStation = findViewById(R.id.shuttle_card_shuttlecock_to_station)
        shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_title).text = "셔틀콕 → 한대앞"
        
        shuttleCardShuttlecockToTerminal = findViewById(R.id.shuttle_card_shuttlecock_to_terminal)
        shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_title).text = "셔틀콕 → 예술인"

        shuttleCardStation = findViewById(R.id.shuttle_card_station)
        shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_title).text = "한대앞"

        shuttleCardTerminal = findViewById(R.id.shuttle_card_terminal)
        shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_title).text = "예술인"
        
        shuttleCardShuttlecockToResidence = findViewById(R.id.shuttle_card_shuttlecock_to_residence)
        shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_title).text = "셔틀콕 건너편"

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

        this.callFoodAllEndpoint()
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

    override fun onDestroy() {
        super.onDestroy()
        if (nativeAd != null) {
            nativeAd!!.destroy()
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
            .subscribe(this::updateShuttleDepartureInfo, this::handleError)
    }

    @SuppressLint("CheckResult")
    private fun callFoodAllEndpoint(){
        val observable = networkService.getFoodAll()
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { data -> data }
            .subscribe(this::updateFoodInfo, this::handleError)
    }

    private fun onError(throwable: Throwable) {
        Log.d("Fetch Error", throwable.message!!)
    }


    @SuppressLint("SetTextI18n")
    private fun updateShuttleDepartureInfo(data: Shuttle) {
        val now = LocalTime.now()
        var timetable = data.Residence.forStation
        var remainedString : String
        when {
            timetable.size >= 2 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "다음 버스 : ${timetable[1].time} (${getHeadingString(timetable[1].type)})"
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timetable.size == 1 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "막차입니다."
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = ""
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = ""
                shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_time).text = "운행 종료"
            }
        }

        timetable = data.Residence.forTerminal
        when {
            timetable.size >= 2 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "다음 버스 : ${timetable[1].time} (${getHeadingString(timetable[1].type)})"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timetable.size == 1 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "막차입니다."
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = ""
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = ""
                shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = "운행 종료"
            }
        }

        timetable = data.Shuttlecock_O.forStation
        when {
            timetable.size >= 2 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "다음 버스 : ${timetable[1].time} (${getHeadingString(timetable[1].type)})"
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timetable.size == 1 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "막차입니다."
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = ""
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = ""
                shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_time).text = "운행 종료"
            }
        }

        timetable = data.Shuttlecock_O.forTerminal
        when {
            timetable.size >= 2 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "다음 버스 : ${timetable[1].time} (${getHeadingString(timetable[1].type)})"
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timetable.size == 1 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "막차입니다."
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = ""
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = ""
                shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = "운행 종료"
            }
        }

        timetable = data.Subway.forStation
        when {
            timetable.size >= 2 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "다음 버스 : ${timetable[1].time} (${getHeadingString(timetable[1].type)})"
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timetable.size == 1 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "막차입니다."
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_this_bus).text = ""
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_next_bus).text = ""
                shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_time).text = "운행 종료"
            }
        }

        timetable = data.Terminal.forTerminal
        when {
            timetable.size >= 2 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "다음 버스 : ${timetable[1].time} (${getHeadingString(timetable[1].type)})"
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timetable.size == 1 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "막차입니다."
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_this_bus).text = ""
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_next_bus).text = ""
                shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_time).text = "운행 종료"
            }
        }

        timetable = data.Shuttlecock_I.forTerminal
        when {
            timetable.size >= 2 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "다음 버스 : ${timetable[1].time} (${getHeadingString(timetable[1].type)})"
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            timetable.size == 1 -> {
                remainedString = "${Duration.between(now, LocalTime.parse(timetable[0].time, formatter)).toMinutes().toInt()} 분 후 도착"
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_this_bus).text = "이번 버스 : ${timetable[0].time} (${getHeadingString(timetable[0].type)})"
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_next_bus).text = "막차입니다."
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_time).text = remainedString
                (shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_time).text as Spannable).setSpan(RelativeSizeSpan(0.75f), remainedString.length - 6, remainedString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> {
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_this_bus).text = ""
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_next_bus).text = ""
                shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_time).text = "운행 종료"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateFoodInfo(data: RestaurantList) {
        restaurantCardListAdapter = RestaurantCardListAdapter(data)
        val foodCardListView = findViewById<RecyclerView>(R.id.food_card_list)
        foodCardListView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        foodCardListView.adapter = restaurantCardListAdapter
    }

    private fun getHeadingString(heading: String) : String{
        return  if (heading == "C") "순환" else "직행"
    }

    private fun handleError(t: Throwable) {
        Log.d("Fetch Error", t.message!!)
    }

    private fun openShuttleActivity(){
        val shuttleActivity = Intent(this, ShuttleActivity::class.java)
        startActivity(shuttleActivity)
    }
}