package app.kobuggi.hyuabot.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.EventsCardListAdapter
import app.kobuggi.hyuabot.config.GitHubNetworkService
import app.kobuggi.hyuabot.model.Events
import app.kobuggi.hyuabot.model.EventsJson
import com.google.gson.GsonBuilder
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class CalendarActivity : AppCompatActivity() {
    lateinit var eventCardListAdapter: EventsCardListAdapter
    lateinit var eventCardView: RecyclerView
    lateinit var gitHubNetworkService: GitHubNetworkService
    lateinit var calendarView : MaterialCalendarView
    lateinit var events: ArrayList<Events>
    private val currentMonth: YearMonth = YearMonth.now()

    val eventsList = mutableMapOf<Pair<Int, Int>, ArrayList<Events>>()
    val eventsSource: PublishSubject<ArrayList<Events>> = PublishSubject.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        gitHubNetworkService = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com")
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(GitHubNetworkService::class.java)
        this.callEventEndpoint()

        calendarView = findViewById(R.id.calendar_view)
        calendarView.setOnMonthChangedListener{_, calendarDay ->
            run {
                if (eventsList[Pair(calendarDay.year, calendarDay.month)] != null) {
                    events = eventsList[Pair(calendarDay.year, calendarDay.month)]!!
                    eventsSource.onNext(eventsList[Pair(calendarDay.year, calendarDay.month)]!!)
                    Log.d("events", eventsList[Pair(calendarDay.year, calendarDay.month)]!!.toString())
                    Log.d("events", eventCardListAdapter.itemCount.toString())
                } else {
                    eventsSource.onNext(ArrayList())
                }
            }
        }
        
        eventsSource.subscribe(observer)
        eventCardView = findViewById(R.id.event_card_list)
        eventCardView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        eventCardListAdapter = EventsCardListAdapter(ArrayList())
        eventCardView.adapter = eventCardListAdapter
    }

    override fun onDestroy() {
        eventsSource.onComplete()
        super.onDestroy()
    }

    @SuppressLint("CheckResult")
    private fun callEventEndpoint(){
        val observable = gitHubNetworkService.getEvent()
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { data -> data }
            .subscribe(this::updateEventInfo, this::handleError)
    }

    @SuppressLint("SetTextI18n")
    private fun updateEventInfo(data: EventsJson) {
        for(item in data.events){
            val key = item.title
            val startArray = item.startDate.split("/")
            val startDate = LocalDate.of(startArray[0].toInt(), startArray[1].toInt(), startArray[2].toInt())
            val endArray = item.endDate.split("/")
            val endDate = LocalDate.of(endArray[0].toInt(), endArray[1].toInt(), endArray[2].toInt())
            if (eventsList[Pair(startDate.year, startDate.monthValue)] == null){
                eventsList[Pair(startDate.year, startDate.monthValue)] = ArrayList()
            }
            if (eventsList[Pair(endDate.year, endDate.monthValue)] == null){
                eventsList[Pair(endDate.year, endDate.monthValue)] = ArrayList()
            }
            eventsList[Pair(startDate.year, startDate.monthValue)]!!.add(Events(key, startDate, endDate))

            if(Pair(startDate.year, startDate.monthValue) != Pair(endDate.year, endDate.monthValue)){
                eventsList[Pair(endDate.year, endDate.monthValue)]!!.add(Events(key, startDate, endDate))
            }
            if(eventsList[Pair(currentMonth.year, currentMonth.monthValue)] != null){
                eventsSource.onNext(eventsList[Pair(currentMonth.year, currentMonth.monthValue)]!!)
            }
        }
    }

    private fun handleError(t: Throwable) {
        Log.d("Fetch Error", t.message!!)
    }

    private val observer = object : DisposableObserver<ArrayList<Events>>(){
        override fun onComplete() {
            Log.d("complete", "complete")
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onNext(t: ArrayList<Events>) {
            eventCardListAdapter.replaceTo(t)
            eventCardListAdapter.notifyDataSetChanged()
        }

        override fun onError(e: Throwable) {

        }
    }
}