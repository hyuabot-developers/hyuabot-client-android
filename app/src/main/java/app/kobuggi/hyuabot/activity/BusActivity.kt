package app.kobuggi.hyuabot.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.GlobalActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.BusCardListAdapter
import app.kobuggi.hyuabot.config.AppServerService
import app.kobuggi.hyuabot.function.getDarkMode
import app.kobuggi.hyuabot.model.Bus
import app.kobuggi.hyuabot.model.BusCardItem
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

class BusActivity : GlobalActivity() {
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
    private var busCardListAdapter: BusCardListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus)
        loadNativeAd()
        val toolbar = findViewById<Toolbar>(R.id.bus_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        Toast.makeText(this, getString(R.string.bus_popup), Toast.LENGTH_SHORT).show()
        fetchBusDepartureInfo()
    }

    private fun fetchBusDepartureInfo() = Observable.interval(0, 1, TimeUnit.MINUTES)
        .subscribe{
            val request = appServerService.getBus()
            request.enqueue(object : Callback<Bus> {
                override fun onResponse(call: Call<Bus>, response: Response<Bus>) {
                    if(response.isSuccessful && response.body() != null){
                        if(busCardListAdapter == null){
                            initBusDepartureInfo(response.body()!!)
                        } else {
                            updateBusDepartureInfo(response.body()!!)
                        }
                    }
                }

                override fun onFailure(call: Call<Bus>, t: Throwable) {

                }
            })
        }

    private fun initBusDepartureInfo(busDepartureInfo : Bus){
        val busDepartureData = arrayListOf(
            BusCardItem("10-1", "#33CC99", this.getString(R.string.guest_house), this.getString(R.string.sangnoksu_station), busDepartureInfo.greenBusForStation, 10),
            BusCardItem("10-1", "#33CC99", this.getString(R.string.guest_house), this.getString(R.string.purgio_6th), busDepartureInfo.greenBusForCampus, 20),
            BusCardItem("707-1", "#0075C8", this.getString(R.string.main_gate), this.getString(R.string.suwon_station), busDepartureInfo.blueBus, 20),
            BusCardItem("3102", "#FF0000", this.getString(R.string.guest_house), this.getString(R.string.gangnam_station), busDepartureInfo.redBus, 20)
        )
        busCardListAdapter = BusCardListAdapter(busDepartureData, this)
        val busCardListView = findViewById<RecyclerView>(R.id.bus_card_list_view)
        busCardListView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        busCardListView.adapter = busCardListAdapter
    }

    private fun updateBusDepartureInfo(busDepartureInfo : Bus){
        val busDepartureData = arrayListOf(
            BusCardItem("10-1", "#33CC99", this.getString(R.string.guest_house), this.getString(R.string.sangnoksu_station), busDepartureInfo.greenBusForStation, 10),
            BusCardItem("10-1", "#33CC99", this.getString(R.string.guest_house), this.getString(R.string.purgio_6th), busDepartureInfo.greenBusForCampus, 20),
            BusCardItem("707-1", "#0075C8", this.getString(R.string.main_gate), this.getString(R.string.suwon_station), busDepartureInfo.blueBus, 20),
            BusCardItem("3102", "#FF0000", this.getString(R.string.guest_house), this.getString(R.string.gangnam_station), busDepartureInfo.redBus, 20)
        )
        busCardListAdapter!!.notifyItemRangeChanged(0, 4, busDepartureData)
    }
}