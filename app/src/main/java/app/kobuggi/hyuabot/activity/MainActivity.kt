package app.kobuggi.hyuabot.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.GlobalActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.HomeShuttleCardListAdapter
import app.kobuggi.hyuabot.adapter.RestaurantHomeCardListAdapter
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.model.RestaurantList
import app.kobuggi.hyuabot.model.Shuttle
import app.kobuggi.hyuabot.model.ShuttleDataItem
import com.google.android.gms.ads.nativead.NativeAd
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.*
import java.util.concurrent.TimeUnit


class MainActivity : GlobalActivity() {
    private var nativeAd : NativeAd? = null

    private lateinit var restaurantCardListAdapter: RestaurantHomeCardListAdapter
    private lateinit var shuttleCardListAdapter: HomeShuttleCardListAdapter

    // 네트워크 클라이언트
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.server_url)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val appServerService = retrofit.create(AppServerService::class.java)
    
    // 버튼 정보 Array
    private val buttonIDList = listOf(
        R.id.menu_shuttle_button, R.id.menu_bus_button, R.id.menu_subway_button, R.id.menu_food_button,
        R.id.menu_library_button, R.id.menu_contact_button, R.id.menu_map_button, R.id.menu_calendar_button
    )
    private val buttonLabelList = listOf(
        R.string.shuttle, R.string.bus, R.string.subway, R.string.food,
        R.string.library, R.string.contact, R.string.map, R.string.calendar
    )
    private val buttonIconList = listOf(
        R.drawable.menu_shuttle, R.drawable.menu_bus, R.drawable.menu_metro, R.drawable.menu_restaurant,
        R.drawable.menu_library, R.drawable.menu_contact, R.drawable.menu_map, R.drawable.menu_calendar
    )
    private val newActivitiesWhenButtonClicked = listOf(
        ShuttleActivity::class.java, BusActivity::class.java, SubwayActivity::class.java, RestaurantActivity::class.java,
        ReadingRoomActivity::class.java, ContactActivity::class.java, MapActivity::class.java, CalendarActivity::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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

        createButtons()
        loadNativeAd()
        updateFoodMenuListView()
        updateShuttleListViewItem()
    }

    // 액티비티에서 빠져나갈 때
    override fun onDestroy() {
        super.onDestroy()
        if (nativeAd != null) {
            nativeAd!!.destroy()
        }
    }

    // 버튼 라벨, 아이콘 맵핑
    private fun createButtons(){
        for (i in 0..7){
            val button = findViewById<RelativeLayout>(buttonIDList[i])
            button.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, buttonIconList[i], null))
            button.findViewById<TextView>(R.id.button_label).text = resources.getString(buttonLabelList[i])
            button.setOnClickListener {
                val newActivity = Intent(this, newActivitiesWhenButtonClicked[i])
                startActivity(newActivity)
            }
        }
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
                    restaurantCardListAdapter = RestaurantHomeCardListAdapter(response.body()!!)
                    foodCardListView.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
                    foodCardListView.adapter = restaurantCardListAdapter
                    foodCardListProgressBar.visibility = View.GONE
                    foodCardListStatus.visibility = View.GONE
                    foodCardListView.visibility = View.VISIBLE
                } else {
                    foodCardListProgressBar.visibility = View.GONE
                    foodCardListStatus.visibility = View.VISIBLE
                    foodCardListStatus.text = resources.getString(R.string.fetch_food_error)
                    foodCardListView.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<RestaurantList>, t: Throwable) {
                foodCardListProgressBar.visibility = View.GONE
                foodCardListStatus.visibility = View.VISIBLE
                foodCardListStatus.text = resources.getString(R.string.fetch_food_error)
                foodCardListView.visibility = View.GONE
            }
        })
    }

    // 셔틀 정보 업데이트 (1분 간격)
   private fun updateShuttleListViewItem() = Observable.interval(0,1, TimeUnit.MINUTES)
        .subscribe {
            val request = appServerService.getShuttleAll()
            val shuttleCardListview = findViewById<RecyclerView>(R.id.shuttle_card_list_home)
            val shuttleCardProgressBar = findViewById<ProgressBar>(R.id.shuttle_card_list_loading_bar)
            val shuttleCardListStatus = findViewById<TextView>(R.id.shuttle_card_list_status)
            request.enqueue(object : Callback<Shuttle>{
                override fun onResponse(call: Call<Shuttle>, response: Response<Shuttle>) {
                    if(response.isSuccessful && response.body() != null){
                        val shuttleArrivalList = arrayListOf<ShuttleDataItem>()
                        val responseBody = response.body()!!
                        shuttleArrivalList.addAll(listOf(
                            ShuttleDataItem(R.string.dorm_to_station, responseBody.Residence.forStation),
                            ShuttleDataItem(R.string.dorm_to_terminal, responseBody.Residence.forTerminal),
                            ShuttleDataItem(R.string.shuttlecock_to_station, responseBody.Shuttlecock_O.forStation),
                            ShuttleDataItem(R.string.shuttlecock_to_terminal, responseBody.Shuttlecock_O.forTerminal),
                            ShuttleDataItem(R.string.station, responseBody.Subway.forStation),
                            ShuttleDataItem(R.string.terminal, responseBody.Terminal.forTerminal),
                            ShuttleDataItem(R.string.shuttlecock_i, responseBody.Shuttlecock_I.forTerminal)
                        ))
                        shuttleCardListAdapter = HomeShuttleCardListAdapter(shuttleArrivalList, this@MainActivity)
                        shuttleCardListview.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
                        shuttleCardListview.adapter = shuttleCardListAdapter
                        shuttleCardProgressBar.visibility = View.GONE
                        shuttleCardListStatus.visibility = View.GONE
                        shuttleCardListview.visibility = View.VISIBLE
                    } else {
                        shuttleCardProgressBar.visibility = View.GONE
                        shuttleCardListStatus.visibility = View.VISIBLE
                        shuttleCardListStatus.text = resources.getString(R.string.fetch_shuttle_error)
                        shuttleCardListview.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<Shuttle>, t: Throwable) {
                    shuttleCardProgressBar.visibility = View.GONE
                    shuttleCardListStatus.visibility = View.VISIBLE
                    shuttleCardListStatus.text = resources.getString(R.string.fetch_shuttle_error)
                    shuttleCardListview.visibility = View.GONE
                }
            })
        }
}