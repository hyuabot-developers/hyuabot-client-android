package app.kobuggi.hyuabot.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.RestaurantCardListAdapter
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.RestaurantList
import app.kobuggi.hyuabot.model.Shuttle
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    var nativeAd : NativeAd? = null
    lateinit var networkService : AppServerService
    lateinit var foodObservable : Observable<RestaurantList>
    lateinit var shuttleObservable: Observable<Shuttle>

    private lateinit var shuttleCardResidenceToStation : CardView
    private lateinit var shuttleCardResidenceToTerminal : CardView
    private lateinit var shuttleCardShuttlecockToStation : CardView
    private lateinit var shuttleCardShuttlecockToTerminal : CardView
    private lateinit var shuttleCardStation : CardView
    private lateinit var shuttleCardTerminal : CardView
    private lateinit var shuttleCardShuttlecockToResidence : CardView
    private lateinit var restaurantCardListAdapter: RestaurantCardListAdapter

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    
    // 네트워크 클라이언트
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.server_url)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val appServerService = retrofit.create(AppServerService::class.java)

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
        shuttleMenuButton.setOnClickListener {}

        val busMenuButton = findViewById<RelativeLayout>(R.id.menu_bus_button)
        busMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_bus, null))
        busMenuButton.findViewById<TextView>(R.id.button_label).text = "버스"
        busMenuButton.setOnClickListener {
            val busActivity = Intent(this, BusActivity::class.java)
            startActivity(busActivity)
        }

        val subwayMenuButton = findViewById<RelativeLayout>(R.id.menu_subway_button)
        subwayMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_metro, null))
        subwayMenuButton.findViewById<TextView>(R.id.button_label).text = "전철"
        subwayMenuButton.setOnClickListener {
            val subwayActivity = Intent(this, SubwayActivity::class.java)
            startActivity(subwayActivity)
        }

        val foodMenuButton = findViewById<RelativeLayout>(R.id.menu_food_button)
        foodMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_restaurant, null))
        foodMenuButton.findViewById<TextView>(R.id.button_label).text = "학식"

        val libraryMenuButton = findViewById<RelativeLayout>(R.id.menu_library_button)
        libraryMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_library, null))
        libraryMenuButton.findViewById<TextView>(R.id.button_label).text = "열람실"
        libraryMenuButton.setOnClickListener{
            val readingRoomActivity = Intent(this, ReadingRoomActivity::class.java)
            startActivity(readingRoomActivity)
        }

        val contactMenuButton = findViewById<RelativeLayout>(R.id.menu_contact_button)
        contactMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_contact, null))
        contactMenuButton.findViewById<TextView>(R.id.button_label).text = "전화부"
        contactMenuButton.setOnClickListener {
            val contactActivity = Intent(this, ContactActivity::class.java)
            startActivity(contactActivity)
        }

        val mapMenuButton = findViewById<RelativeLayout>(R.id.menu_map_button)
        mapMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_map, null))
        mapMenuButton.findViewById<TextView>(R.id.button_label).text = "지도"
        mapMenuButton.setOnClickListener {
            val mapActivity = Intent(this, MapActivity::class.java)
            startActivity(mapActivity)
        }

        val calendarMenuButton = findViewById<RelativeLayout>(R.id.menu_calendar_button)
        calendarMenuButton.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.menu_calendar, null))
        calendarMenuButton.findViewById<TextView>(R.id.button_label).text = "학사력"
        calendarMenuButton.setOnClickListener {
            val calendarActivity = Intent(this, CalendarActivity::class.java)
            startActivity(calendarActivity)
        }

        shuttleCardResidenceToStation = findViewById(R.id.shuttle_card_dorm_to_station_home)
        shuttleCardResidenceToStation.findViewById<TextView>(R.id.shuttle_card_title).text = "기숙사 → 한대앞"
        
        shuttleCardResidenceToTerminal = findViewById(R.id.shuttle_card_dorm_to_terminal_home)
        shuttleCardResidenceToTerminal.findViewById<TextView>(R.id.shuttle_card_title).text = "기숙사 → 예술인"

        shuttleCardShuttlecockToStation = findViewById(R.id.shuttle_card_shuttlecock_to_station_home)
        shuttleCardShuttlecockToStation.findViewById<TextView>(R.id.shuttle_card_title).text = "셔틀콕 → 한대앞"
        
        shuttleCardShuttlecockToTerminal = findViewById(R.id.shuttle_card_shuttlecock_to_terminal_home)
        shuttleCardShuttlecockToTerminal.findViewById<TextView>(R.id.shuttle_card_title).text = "셔틀콕 → 예술인"

        shuttleCardStation = findViewById(R.id.shuttle_card_station_home)
        shuttleCardStation.findViewById<TextView>(R.id.shuttle_card_title).text = "한대앞"

        shuttleCardTerminal = findViewById(R.id.shuttle_card_terminal_home)
        shuttleCardTerminal.findViewById<TextView>(R.id.shuttle_card_title).text = "예술인"
        
        shuttleCardShuttlecockToResidence = findViewById(R.id.shuttle_card_shuttlecock_to_residence_home)
        shuttleCardShuttlecockToResidence.findViewById<TextView>(R.id.shuttle_card_title).text = "셔틀콕 건너편"


        updateFoodMenuListView()
    }

    // 다시 액티비티로 돌아올 때
    override fun onResume() {
        super.onResume()
    }

    // 액티비티에서 빠져나갈 때
    override fun onDestroy() {
        super.onDestroy()
        if (nativeAd != null) {
            nativeAd!!.destroy()
        }
    }

    // 다른 액티비티로 이동할 때
    override fun onPause() {
        super.onPause()
    }

    // 학식을 리스트뷰로 표현
    private fun updateFoodMenuListView(){
        val request = appServerService.getFoodAll()
        val foodCardListView = findViewById<RecyclerView>(R.id.food_card_list)
        val foodCardListProgressBar = findViewById<ProgressBar>(R.id.food_card_list_loading_bar)
        val foodCardListStatus = findViewById<TextView>(R.id.food_card_list_status)
        request.enqueue(object : Callback<RestaurantList>{
            override fun onResponse(call: Call<RestaurantList>, response: Response<RestaurantList>) {
                if(response.isSuccessful && response.body() != null){
                    restaurantCardListAdapter = RestaurantCardListAdapter(response.body()!!)
                    foodCardListView.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
                    foodCardListView.adapter = restaurantCardListAdapter
                    foodCardListProgressBar.visibility = View.GONE
                    foodCardListView.visibility = View.VISIBLE
                } else {
                    foodCardListProgressBar.visibility = View.GONE
                    foodCardListStatus.text = response.message()
                    foodCardListStatus.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<RestaurantList>, t: Throwable) {
                foodCardListProgressBar.visibility = View.GONE
                foodCardListStatus.text = t.message
                foodCardListStatus.visibility = View.VISIBLE
            }
        })
    }
    
    
    
    private fun getHeadingString(heading: String) : String{
        return  if (heading == "C") "순환" else "직행"
    }

}