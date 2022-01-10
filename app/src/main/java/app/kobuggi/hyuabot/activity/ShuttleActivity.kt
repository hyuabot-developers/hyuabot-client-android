package app.kobuggi.hyuabot.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.ui.BindingActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.ShuttleCardListAdapter
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.model.*
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ShuttleActivity : BindingActivity() {

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
    private lateinit var shuttleCardListAdapter : ShuttleCardListAdapter
    private var subwayDataType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shuttle)

        Toast.makeText(this, resources.getString(R.string.shuttle_popup), Toast.LENGTH_SHORT).show()
        loadNativeAd()
        connectToolbarFunction()
        fetchShuttleCardPeriodically(subwayDataType)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.shuttle_action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.shuttle_subway_line_4_seoul ->{
                subwayDataType = 0
            }
            R.id.shuttle_subway_line_4_oido ->{
                subwayDataType = 1
            }
            R.id.shuttle_subway_line_suin_seoul -> {
                subwayDataType = 2
            }
            R.id.shuttle_subway_line_suin_incheon -> {
                subwayDataType = 3
            }
        }
        fetchShuttleData(subwayDataType)
        return true
    }


    // 툴바 연결
    private fun connectToolbarFunction(){
        val toolbar = findViewById<Toolbar>(R.id.shuttle_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    // 셔틀 정보 새로고침(1분 간격)
    private fun fetchShuttleCardPeriodically(subwayDataType : Int) = Observable.interval(0, 1, TimeUnit.MINUTES)
        .subscribe {fetchShuttleData(subwayDataType)}

    // 셔틀 정보 로딩
    private fun fetchShuttleData(subwayDataType : Int){
        val shuttleRequest = appServerService.getShuttleAll()
        val shuttleCardListview = findViewById<RecyclerView>(R.id.shuttle_card_list)
        val shuttleCardProgressBar = findViewById<ProgressBar>(R.id.shuttle_card_list_loading_bar)
        val shuttleCardListStatus = findViewById<TextView>(R.id.shuttle_card_list_status)
        shuttleRequest.enqueue(object : Callback<Shuttle> {
            override fun onResponse(call: Call<Shuttle>, response: Response<Shuttle>) {
                if(response.isSuccessful && response.body() != null){
                    val shuttleResponseBody = response.body()!!
                    val subwayRequest = appServerService.getSubwayERICA(CampusRequest(campus = "erica"))
                    subwayRequest.enqueue(object : Callback<SubwayERICA> {
                        override fun onResponse(call: Call<SubwayERICA>, response: Response<SubwayERICA>) {
                            if(response.isSuccessful && response.body() != null){
                                val subwayResponseBody = response.body()!!
                                val subwayRealtimeData = when(subwayDataType){
                                    0 -> subwayResponseBody.line4.realtime.upLine
                                    1 -> subwayResponseBody.line4.realtime.downLine
                                    2 -> subwayResponseBody.lineSuin.realtime.upLine
                                    else -> subwayResponseBody.lineSuin.realtime.downLine
                                }
                                val subwayTimetableData = when(subwayDataType){
                                    0 -> subwayResponseBody.line4.timetable.upLine
                                    1 -> subwayResponseBody.line4.timetable.downLine
                                    2 -> subwayResponseBody.lineSuin.timetable.upLine
                                    else -> subwayResponseBody.lineSuin.timetable.downLine
                                }
                                val shuttleCardList = arrayListOf(
                                    ShuttleCardItem(R.string.dorm, R.string.station, shuttleResponseBody.Residence.forStation, subwayRealtimeData, subwayTimetableData),
                                    ShuttleCardItem(R.string.dorm, R.string.terminal, shuttleResponseBody.Residence.forTerminal, subwayRealtimeData, subwayTimetableData),
                                    ShuttleCardItem(R.string.shuttlecock_o, R.string.station, shuttleResponseBody.Shuttlecock_O.forStation, subwayRealtimeData, subwayTimetableData),
                                    ShuttleCardItem(R.string.shuttlecock_o, R.string.terminal, shuttleResponseBody.Shuttlecock_O.forTerminal, subwayRealtimeData, subwayTimetableData),
                                    ShuttleCardItem(R.string.station, R.string.campus, shuttleResponseBody.Subway.forStation, subwayRealtimeData, subwayTimetableData),
                                    ShuttleCardItem(R.string.station, R.string.terminal, shuttleResponseBody.Subway.forTerminal, subwayRealtimeData, subwayTimetableData),
                                    ShuttleCardItem(R.string.terminal, R.string.campus, shuttleResponseBody.Terminal.forTerminal, subwayRealtimeData, subwayTimetableData),
                                    ShuttleCardItem(R.string.shuttlecock_i, R.string.dorm, shuttleResponseBody.Shuttlecock_I.forTerminal, subwayRealtimeData, subwayTimetableData),
                                )

                                shuttleCardListAdapter = ShuttleCardListAdapter(shuttleCardList, this@ShuttleActivity, subwayDataType)
                                shuttleCardListview.layoutManager = LinearLayoutManager(this@ShuttleActivity, RecyclerView.VERTICAL, false)
                                shuttleCardListview.adapter = shuttleCardListAdapter
                                shuttleCardProgressBar.visibility = View.GONE
                                shuttleCardListview.visibility = View.VISIBLE
                                shuttleCardListStatus.visibility = View.GONE
                            } else {
                                val shuttleCardList = arrayListOf(
                                    ShuttleCardItem(R.string.dorm, R.string.station, shuttleResponseBody.Residence.forStation, listOf(), listOf()),
                                    ShuttleCardItem(R.string.dorm, R.string.terminal, shuttleResponseBody.Residence.forTerminal, listOf(), listOf()),
                                    ShuttleCardItem(R.string.shuttlecock_o, R.string.station, shuttleResponseBody.Shuttlecock_O.forStation, listOf(), listOf()),
                                    ShuttleCardItem(R.string.shuttlecock_o, R.string.terminal, shuttleResponseBody.Shuttlecock_O.forTerminal, listOf(), listOf()),
                                    ShuttleCardItem(R.string.station, R.string.campus, shuttleResponseBody.Subway.forStation, listOf(), listOf()),
                                    ShuttleCardItem(R.string.station, R.string.terminal, shuttleResponseBody.Subway.forTerminal, listOf(), listOf()),
                                    ShuttleCardItem(R.string.terminal, R.string.campus, shuttleResponseBody.Terminal.forTerminal, listOf(), listOf()),
                                    ShuttleCardItem(R.string.shuttlecock_i, R.string.dorm, shuttleResponseBody.Shuttlecock_I.forTerminal, listOf(), listOf()),
                                )

                                shuttleCardListAdapter = ShuttleCardListAdapter(shuttleCardList, this@ShuttleActivity, subwayDataType)
                                shuttleCardListview.layoutManager = LinearLayoutManager(this@ShuttleActivity, RecyclerView.VERTICAL, false)
                                shuttleCardListview.adapter = shuttleCardListAdapter
                                shuttleCardProgressBar.visibility = View.GONE
                                shuttleCardListview.visibility = View.VISIBLE
                                shuttleCardListStatus.visibility = View.GONE
                            }
                        }

                        override fun onFailure(call: Call<SubwayERICA>, t: Throwable) {
                            val shuttleCardList = arrayListOf(
                                ShuttleCardItem(R.string.dorm, R.string.station, shuttleResponseBody.Residence.forStation, listOf(), listOf()),
                                ShuttleCardItem(R.string.dorm, R.string.terminal, shuttleResponseBody.Residence.forTerminal, listOf(), listOf()),
                                ShuttleCardItem(R.string.shuttlecock_o, R.string.station, shuttleResponseBody.Shuttlecock_O.forStation, listOf(), listOf()),
                                ShuttleCardItem(R.string.shuttlecock_o, R.string.terminal, shuttleResponseBody.Shuttlecock_O.forTerminal, listOf(), listOf()),
                                ShuttleCardItem(R.string.station, R.string.campus, shuttleResponseBody.Subway.forStation, listOf(), listOf()),
                                ShuttleCardItem(R.string.station, R.string.terminal, shuttleResponseBody.Subway.forTerminal, listOf(), listOf()),
                                ShuttleCardItem(R.string.terminal, R.string.campus, shuttleResponseBody.Terminal.forTerminal, listOf(), listOf()),
                                ShuttleCardItem(R.string.shuttlecock_i, R.string.dorm, shuttleResponseBody.Shuttlecock_I.forTerminal, listOf(), listOf()),
                            )

                            shuttleCardListAdapter = ShuttleCardListAdapter(shuttleCardList, this@ShuttleActivity, subwayDataType)
                            shuttleCardListview.layoutManager = LinearLayoutManager(this@ShuttleActivity, RecyclerView.VERTICAL, false)
                            shuttleCardListview.adapter = shuttleCardListAdapter
                            shuttleCardProgressBar.visibility = View.GONE
                            shuttleCardListview.visibility = View.VISIBLE
                            shuttleCardListStatus.visibility = View.GONE
                        }
                    })
                } else {
                    shuttleCardProgressBar.visibility = View.GONE
                    shuttleCardListStatus.text = response.message()
                    shuttleCardListStatus.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<Shuttle>, t: Throwable) {
                shuttleCardProgressBar.visibility = View.GONE
                shuttleCardListStatus.text = t.message
                shuttleCardListStatus.visibility = View.VISIBLE
            }
        })
    }
}