package app.kobuggi.hyuabot.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.GlobalActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.ReadingRoomCardListAdapter
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.*
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ReadingRoomActivity : GlobalActivity() {
    lateinit var readingRoomCardListAdapter: ReadingRoomCardListAdapter

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
        setContentView(R.layout.activity_reading_room)

        // 광고 로드
        loadNativeAd()

        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.reading_room_swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            fetchReadingRoomData()
            refreshLayout.isRefreshing = false
        }

        val toolbar = findViewById<Toolbar>(R.id.reading_room_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        updateReadingRoomListViewItem()
    }

    // 열람실 정보 업데이트 (1분 간격)
    private fun updateReadingRoomListViewItem() = Observable.interval(0,1, TimeUnit.MINUTES)
        .subscribe {
        fetchReadingRoomData()
    }

    private fun fetchReadingRoomData() {
        val request = appServerService.getReadingRoom(CampusRequest(campus = "erica"))
        val readingRoomCardListview = findViewById<RecyclerView>(R.id.reading_room_list)
        val readingRoomProgressBar = findViewById<ProgressBar>(R.id.reading_room_list_loading_bar)
        val readingRoomListStatus = findViewById<TextView>(R.id.reading_room_list_status)
        request.enqueue(object : Callback<ReadingRoomList> {
            override fun onResponse(call: Call<ReadingRoomList>, response: Response<ReadingRoomList>) {
                if(response.isSuccessful && response.body() != null){
                    val responseBody = response.body()!!
                    readingRoomCardListAdapter = ReadingRoomCardListAdapter(responseBody.rooms.filter {it.activeTotal > 0})
                    readingRoomCardListview.layoutManager = LinearLayoutManager(this@ReadingRoomActivity, RecyclerView.VERTICAL, false)
                    readingRoomCardListview.adapter = readingRoomCardListAdapter
                    readingRoomProgressBar.visibility = View.GONE
                    readingRoomCardListview.visibility = View.VISIBLE
                } else {
                    readingRoomProgressBar.visibility = View.GONE
                    readingRoomListStatus.text = response.message()
                    readingRoomListStatus.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ReadingRoomList>, t: Throwable) {
                readingRoomProgressBar.visibility = View.GONE
                readingRoomListStatus.text = t.message
                readingRoomListStatus.visibility = View.VISIBLE
            }
        }
        )
    }
}