package app.kobuggi.hyuabot.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.ReadingRoomCardListAdapter
import app.kobuggi.hyuabot.adapter.RestaurantCardListAdapter
import app.kobuggi.hyuabot.config.NetworkService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.CampusRequest
import app.kobuggi.hyuabot.model.ReadingRoom
import app.kobuggi.hyuabot.model.ReadingRoomList
import app.kobuggi.hyuabot.model.RestaurantList
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
import java.util.concurrent.TimeUnit

class ReadingRoomActivity : AppCompatActivity() {
    lateinit var readingRoomCardListAdapter: ReadingRoomCardListAdapter
    lateinit var networkService : NetworkService
    lateinit var disposable : Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_room)

        // 광고 로드
        val builder = AdLoader.Builder(this, BuildConfig.admob_unit_id)
        val config = this.resources.configuration
        builder.forNativeAd{
            val template = findViewById<TemplateView>(R.id.reading_room_admob_template)
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
            .subscribe(this::callReadingRoomEndpoint, this::onError)

        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.reading_room_swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            callReadingRoomEndpoint(0)
            refreshLayout.isRefreshing = false
        }

        val toolbar = findViewById<Toolbar>(R.id.reading_room_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (disposable.isDisposed) {
            disposable = Observable.interval(0, 1,
                TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callReadingRoomEndpoint, this::onError)
        }
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    @SuppressLint("CheckResult")
    private fun callReadingRoomEndpoint(aLong : Long){
        val observable = networkService.getReadingRoom(CampusRequest("erica"))
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { data -> data.rooms }
            .subscribe(this::updateReadingRoomInfo, this::onError)
    }

    @SuppressLint("SetTextI18n")
    private fun updateReadingRoomInfo(data: ArrayList<ReadingRoom>) {
        readingRoomCardListAdapter = ReadingRoomCardListAdapter(data)
        val readingRoomCardView = findViewById<RecyclerView>(R.id.reading_room_list)
        readingRoomCardView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        readingRoomCardView.adapter = readingRoomCardListAdapter
    }

    private fun onError(throwable: Throwable) {
        Log.d("Fetch Error", throwable.message!!)
    }
}