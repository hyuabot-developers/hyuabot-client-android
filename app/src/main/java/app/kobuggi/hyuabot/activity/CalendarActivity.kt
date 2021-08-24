package app.kobuggi.hyuabot.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.EventsCardListAdapter
import app.kobuggi.hyuabot.config.GitHubNetworkService
import app.kobuggi.hyuabot.model.Events
import app.kobuggi.hyuabot.model.EventsJson
import com.google.gson.GsonBuilder
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    lateinit var calendarView : CalendarView
    lateinit var events: ArrayList<Events>

    val eventsList = mutableMapOf<Int, ArrayList<Events>>()

    inner class DayViewContainer(view: View) : ViewContainer(view){
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    inner class MonthViewContainer(view: View) : ViewContainer(view){
        val monthHeader: TextView = view.findViewById(R.id.month_header_text)
    }

    inner class EventsViewContainer(view: View) : ViewContainer(view){
        val monthEventsList: RecyclerView = view.findViewById(R.id.event_card_list)
    }

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

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        events = if(eventsList[currentMonth.monthValue] != null){
            eventsList[currentMonth.monthValue]!!
        } else {
            ArrayList()
        }
        eventCardListAdapter = EventsCardListAdapter(events)

        calendarView = findViewById(R.id.calendar_view)
        calendarView.dayBinder = object : DayBinder<DayViewContainer>{
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
            }
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer>{
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.monthHeader.text = "${month.year}년 ${month.month}월"
            }
            override fun create(view: View) = MonthViewContainer(view)
        }


        calendarView.monthFooterBinder = object : MonthHeaderFooterBinder<EventsViewContainer>{
            override fun bind(container: EventsViewContainer, month: CalendarMonth) {
                Log.d("events", eventsList.toString())
                if(eventsList[month.month] != null){
                    eventCardListAdapter.replaceTo(eventsList[month.month]!!)
                } else {
                    eventCardListAdapter.replaceTo(ArrayList())
                }
                eventCardView = container.monthEventsList
                eventCardView.layoutManager = LinearLayoutManager(this@CalendarActivity, RecyclerView.VERTICAL, false)
                eventCardView.adapter = eventCardListAdapter
            }
            override fun create(view: View) = EventsViewContainer(view)
        }

        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
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
            if (eventsList[startDate.monthValue] == null){
                eventsList[startDate.monthValue] = ArrayList()
            }
            if (eventsList[endDate.monthValue] == null){
                eventsList[endDate.monthValue] = ArrayList()
            }
            eventsList[startDate.monthValue]!!.add(Events(key, startDate, endDate))
            eventsList[endDate.monthValue]!!.add(Events(key, startDate, endDate))
        }
    }

    private fun handleError(t: Throwable) {
        Log.d("Fetch Error", t.message!!)
    }

}