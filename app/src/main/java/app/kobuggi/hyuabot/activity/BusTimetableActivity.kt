package app.kobuggi.hyuabot.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.doOnAttach
import androidx.viewpager2.widget.ViewPager2
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.BusTimetableStateAdapter
import app.kobuggi.hyuabot.adapter.ShuttleTimetableStateAdapter
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.model.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
import java.util.*
import java.util.concurrent.TimeUnit

class BusTimetableActivity : AppCompatActivity() {
    lateinit var viewPager2: ViewPager2
    lateinit var tabLayout: TabLayout
    private val tabTextList = arrayListOf("평일", "토요일", "일요일")
    private val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_timetable)

        Toast.makeText(this, this.resources.getString(R.string.bus_timetable_popup, intent.getStringExtra("routeID")), Toast.LENGTH_SHORT).show()
        viewPager2 = findViewById(R.id.bus_timetable_tab_pager)
        tabLayout = findViewById(R.id.bus_timetable_tab_layout)

        fetchBusTimetable()
    }

    // 셔틀 정류장 정보 로딩
    private fun fetchBusTimetable(){
        val routeID = if(intent.getStringExtra("routeID") != null) {
            intent.getStringExtra("routeID")
        } else {
            ""
        }

        val shuttleStopInfoRequest = appServerService.getBusTimetable(BusByRouteRequest(routeID!!))
        shuttleStopInfoRequest.enqueue(object : Callback<BusTimetableByDay> {
            override fun onResponse(call: Call<BusTimetableByDay>, response: Response<BusTimetableByDay>) {
                if(response.isSuccessful && response.body() != null){
                    val busTimetableResponseBody = response.body()!!
                    viewPager2.adapter = BusTimetableStateAdapter(this@BusTimetableActivity,
                        busTimetableResponseBody.weekdays,
                        busTimetableResponseBody.sat,
                        busTimetableResponseBody.sun
                    )
                    viewPager2.doOnAttach {
                        if (dayOfWeek == Calendar.SUNDAY){
                            viewPager2.setCurrentItem(2, true)
                        } else if (dayOfWeek == Calendar.SATURDAY){
                            viewPager2.setCurrentItem(1, true)
                        } else{
                            viewPager2.setCurrentItem(0, true)
                        }
                    }

                    TabLayoutMediator(tabLayout, viewPager2){
                            tab, position -> tab.text = tabTextList[position]
                    }.attach()
                }
            }

            override fun onFailure(call: Call<BusTimetableByDay>, t: Throwable) {

            }
        })
    }
}