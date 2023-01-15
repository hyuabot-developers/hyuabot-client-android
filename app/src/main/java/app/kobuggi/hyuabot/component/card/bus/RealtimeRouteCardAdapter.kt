package app.kobuggi.hyuabot.component.card.bus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.CardBusRealtimeBinding
import app.kobuggi.hyuabot.databinding.CardShuttleRealtimeBinding
import app.kobuggi.hyuabot.model.bus.BusRouteStartStopItem
import app.kobuggi.hyuabot.model.bus.BusStopItem
import app.kobuggi.hyuabot.model.bus.BusStopRouteItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListRouteStopItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListStopItem
import app.kobuggi.hyuabot.model.shuttle.Destination
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeListResponse
import app.kobuggi.hyuabot.ui.bus.realtime.RealtimeItem
import app.kobuggi.hyuabot.ui.bus.realtime.RealtimeRouteItem
import app.kobuggi.hyuabot.util.TimetableUtil

class RealtimeRouteCardAdapter (
    private val context: Context,
    private var bookmarkIndex: Int,
    private val onClickBookmark: (Int) -> Unit,
    private val onClickTimetableButton: (Int, String, Int) -> Unit
) : RecyclerView.Adapter<RealtimeRouteCardAdapter.ViewHolder>() {
    private val cardList: List<CardItem> = arrayListOf(
        CardItem(R.string.bus_city, listOf(RealtimeRouteItem(), RealtimeRouteItem())),
        CardItem(R.string.bus_seoul, listOf(RealtimeRouteItem(), RealtimeRouteItem(), RealtimeRouteItem(), RealtimeRouteItem())),
        CardItem(R.string.bus_suwon, listOf(RealtimeRouteItem(), RealtimeRouteItem())),
    )
    inner class ViewHolder(private val binding: CardBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int, cardItem: CardItem) {
            val resources = context.resources
            binding.busTypeName.text = resources.getString(cardItem.title)
            binding.addBookmarkButton.setOnClickListener {
                onClickBookmark(bindingAdapterPosition)
            }
            if (index == bookmarkIndex) {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_checked)
            } else {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked)
            }
            val adapter = RealtimeRouteItemAdapter(context, cardItem.routeList, onClickTimetableButton)
            binding.busRealtimeRouteRecyclerView.adapter = adapter
            binding.busRealtimeRouteRecyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_bus_realtime, parent, false)
        return ViewHolder(CardBusRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, cardList[position])
    }

    override fun getItemCount(): Int = cardList.size

    fun updateBookmarkIndex(bookmarkIndex: Int) {
        this.bookmarkIndex = bookmarkIndex
        notifyItemRangeChanged(0, 3)
    }

    fun updateSangnoksuData(data: BusStopItem) {
        cardList[0].routeList[0].routeID = data.routes[0].routeID
        cardList[0].routeList[0].routeName = data.routes[0].routeName
        cardList[0].routeList[0].stopID = R.string.sangnoksu_stn
        cardList[0].routeList[0].startStopID = data.routes[0].startStop.stopID
        cardList[0].routeList[0].timetable = data.routes[0].startStop.timetable
        notifyItemChanged(0)
    }

    fun updateMainGateData(data: BusStopItem) {
        val route3100 = data.routes.find { it.routeName == "3100" }
        val route3101 = data.routes.find { it.routeName == "3101" }
        val route3100N = data.routes.find { it.routeName == "3100N" }
        val routeSuwon = data.routes.find { it.routeName == "707-1" }


        cardList[1].routeList[1].routeID = route3100N?.routeID ?: 0
        cardList[1].routeList[1].routeName = route3100N?.routeName ?: ""
        cardList[1].routeList[1].stopID = R.string.main_gate
        cardList[1].routeList[1].startStopID = route3100N?.startStop?.stopID ?: 0
        cardList[1].routeList[1].timetable = route3100N?.startStop?.timetable?.map { TimetableUtil.add24Hour(it) }?.sorted() ?: listOf()
        val realtime3100N = arrayListOf<RealtimeItem>()
        realtime3100N.clear()
        for (i in route3100N?.realtime ?: listOf()) {
            realtime3100N.add(RealtimeItem(route3100N?.routeName ?: "", i.remainedStop, i.remainedTime, i.remainedSeat, i.lowPlate))
        }
        cardList[1].routeList[1].realtime = realtime3100N

        cardList[1].routeList[2].routeID = route3100?.routeID ?: 0
        cardList[1].routeList[2].routeName = route3100?.routeName ?: ""
        cardList[1].routeList[2].stopID = R.string.main_gate
        cardList[1].routeList[2].startStopID = route3100?.startStop?.stopID ?: 0
        cardList[1].routeList[2].timetable = route3100?.startStop?.timetable ?: listOf()
        val realtime3100 = arrayListOf<RealtimeItem>()
        realtime3100.clear()
        for (i in route3100?.realtime ?: listOf()) {
            realtime3100.add(RealtimeItem(route3100?.routeName ?: "", i.remainedStop, i.remainedTime, i.remainedSeat, i.lowPlate))
        }
        cardList[1].routeList[2].realtime = realtime3100

        cardList[1].routeList[3].routeID = route3101?.routeID ?: 0
        cardList[1].routeList[3].routeName = route3101?.routeName ?: ""
        cardList[1].routeList[3].stopID = R.string.main_gate
        cardList[1].routeList[3].startStopID = route3101?.startStop?.stopID ?: 0
        cardList[1].routeList[3].timetable = route3101?.startStop?.timetable ?: listOf()
        val realtime3101 = arrayListOf<RealtimeItem>()
        realtime3101.clear()
        for (i in route3101?.realtime ?: listOf()) {
            realtime3101.add(RealtimeItem(route3101?.routeName ?: "", i.remainedStop, i.remainedTime, i.remainedSeat, i.lowPlate))
        }
        cardList[1].routeList[3].realtime = realtime3101

        cardList[2].routeList[0].routeID = routeSuwon?.routeID ?: 0
        cardList[2].routeList[0].routeName = routeSuwon?.routeName ?: ""
        cardList[2].routeList[0].stopID = R.string.main_gate
        cardList[2].routeList[0].startStopID = routeSuwon?.startStop?.stopID ?: 0
        cardList[2].routeList[0].timetable = routeSuwon?.startStop?.timetable ?: listOf()
        val realtimeSuwon = arrayListOf<RealtimeItem>()
        realtimeSuwon.clear()
        for (i in routeSuwon?.realtime ?: listOf()) {
            realtimeSuwon.add(RealtimeItem(routeSuwon?.routeName ?: "", i.remainedStop, i.remainedTime, i.remainedSeat, i.lowPlate))
        }
        cardList[2].routeList[0].realtime = realtimeSuwon

        notifyItemRangeChanged(1, 2)
    }

    fun updateConventionCenterData(data: BusStopItem) {
        val routeSangnoksu = data.routes.find { it.routeName == "10-1" }
        val routeSeoul = data.routes.find { it.routeName == "3102" }


        cardList[1].routeList[0].routeID = routeSeoul?.routeID ?: 0
        cardList[1].routeList[0].routeName = routeSeoul?.routeName ?: ""
        cardList[1].routeList[0].stopID = R.string.convention_center
        cardList[1].routeList[0].startStopID = routeSeoul?.startStop?.stopID ?: 0
        cardList[1].routeList[0].timetable = routeSeoul?.startStop?.timetable ?: listOf()
        val realtimeSeoul = arrayListOf<RealtimeItem>()
        realtimeSeoul.clear()
        for (i in routeSeoul?.realtime ?: listOf()) {
            realtimeSeoul.add(RealtimeItem(routeSeoul?.routeName ?: "", i.remainedStop, i.remainedTime, i.remainedSeat, i.lowPlate))
        }
        cardList[1].routeList[0].realtime = realtimeSeoul

        cardList[0].routeList[1].routeID = routeSangnoksu?.routeID ?: 0
        cardList[0].routeList[1].routeName = routeSangnoksu?.routeName ?: ""
        cardList[0].routeList[1].stopID = R.string.convention_center
        cardList[0].routeList[1].startStopID = routeSangnoksu?.startStop?.stopID ?: 0
        cardList[0].routeList[1].timetable = routeSangnoksu?.startStop?.timetable ?: listOf()
        val realtimeSangnoksu = arrayListOf<RealtimeItem>()
        realtimeSangnoksu.clear()
        for (i in routeSangnoksu?.realtime ?: listOf()) {
            realtimeSangnoksu.add(RealtimeItem(routeSangnoksu?.routeName ?: "", i.remainedStop, i.remainedTime, i.remainedSeat, i.lowPlate))
        }
        cardList[0].routeList[1].realtime = realtimeSangnoksu

        notifyItemRangeChanged(0, 2)
    }

    fun updateSeonganHighSchoolData(data: BusStopItem) {
        val realtimeList = arrayListOf<RealtimeItem>()

        cardList[2].routeList[1].routeName = "110/707/909"
        realtimeList.clear()
        for (route in data.routes){
            for (realtimeItem in route.realtime){
                realtimeList.add(RealtimeItem(route.routeName, realtimeItem.remainedStop, realtimeItem.remainedTime, realtimeItem.remainedSeat, realtimeItem.lowPlate))
            }
        }
        cardList[2].routeList[1].realtime = realtimeList.sortedBy { it.remainedStop }
        notifyItemChanged(2)
    }
}