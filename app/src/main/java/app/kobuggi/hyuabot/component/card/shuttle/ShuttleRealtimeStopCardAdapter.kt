package app.kobuggi.hyuabot.component.card.shuttle

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.CardShuttleRealtimeBinding
import app.kobuggi.hyuabot.model.bus.BusRouteStartStopItem
import app.kobuggi.hyuabot.model.bus.BusStopRouteItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListRouteStopItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListStopItem
import app.kobuggi.hyuabot.model.shuttle.Destination
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeListResponse

class ShuttleRealtimeStopCardAdapter(
    private val context: Context,
    private var bookmarkIndex: Int,
    private val onClickBookmark: (Int) -> Unit,
    private val onClickInformation: (Int) -> Unit,
    private val onClickTimetableButton: (Int, Int) -> Unit
) : RecyclerView.Adapter<ShuttleRealtimeStopCardAdapter.ViewHolder>() {
    private val stopList: List<StopItem> = arrayListOf(
        StopItem(R.string.dormitory_o, listOf(Destination.STATION, Destination.TERMINAL, Destination.JUNGANG_STN), listOf()),
        StopItem(R.string.shuttlecock_o, listOf(Destination.STATION, Destination.TERMINAL, Destination.JUNGANG_STN), listOf()),
        StopItem(R.string.station, listOf(Destination.CAMPUS, Destination.TERMINAL, Destination.JUNGANG_STN), listOf()),
        StopItem(R.string.terminal, listOf(Destination.CAMPUS), listOf()),
        StopItem(R.string.jungang_stn, listOf(Destination.CAMPUS), listOf()),
        StopItem(R.string.shuttlecock_i, listOf(Destination.DORMITORY), listOf()),
    )
    private val subCardDataList: List<ShuttleSubCardData> = listOf(
        ShuttleSubCardData(R.string.shuttle_sub_card_subway_title, R.string.shuttle_sub_card_subway_subtitle, R.string.shuttle_sub_card_subway_left_title, R.string.shuttle_sub_card_subway_right_title,
            SubCardItemList(SubwayRealtimeListResponse(listOf(), listOf()), SubwayRealtimeListResponse(listOf(), listOf()))),
        ShuttleSubCardData(R.string.shuttle_sub_card_suwon_title, R.string.shuttle_sub_card_suwon_subtitle, R.string.shuttle_sub_card_suwon_left_title, R.string.shuttle_sub_card_suwon_right_title,
            SubCardItemList(
                SubwayRealtimeListResponse(listOf(), listOf()),
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf())
            ),
        ),
        ShuttleSubCardData(R.string.shuttle_sub_card_sangnoksu_title, R.string.shuttle_sub_card_sangnoksu_subtitle, R.string.shuttle_sub_card_sangnoksu_left_title, R.string.shuttle_sub_card_sangnoksu_right_title,
            SubCardItemList(
                ArrivalListStopItem("", listOf()),
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf())
            ),
        ),
        ShuttleSubCardData(R.string.shuttle_sub_card_gwangmyeong_title, R.string.shuttle_sub_card_gwangmyeong_subtitle, R.string.shuttle_sub_card_gwangmyeong_left_title, R.string.shuttle_sub_card_gwangmyeong_right_title,
            SubCardItemList(
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf()),
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf())
            ),
        ),
        ShuttleSubCardData(-1, -1, -1, -1, SubCardItemList(ArrivalListStopItem("", listOf()), ArrivalListStopItem("", listOf()))),
        ShuttleSubCardData(-1, -1, -1, -1, SubCardItemList(ArrivalListStopItem("", listOf()), ArrivalListStopItem("", listOf()))),
    )
    data class StopItem(val stopID: Int, val routeType: List<Destination>, var arrivalList: List<ArrivalListRouteStopItem>)
    inner class ViewHolder(private val binding: CardShuttleRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int, stopItem: StopItem, subCardData: ShuttleSubCardData) {
            val resources = GlobalApplication.getAppResources()
            binding.shuttleRealtimeStopName.text = resources.getString(stopItem.stopID)
            binding.shuttleRealtimeToStationHeaderTimetable.setOnClickListener { onClickTimetableButton(stopItem.stopID, R.string.shuttle_bound_for_station) }
            binding.shuttleRealtimeToTerminalHeaderTimetable.setOnClickListener { onClickTimetableButton(stopItem.stopID, R.string.shuttle_bound_for_terminal) }
            binding.shuttleRealtimeToJungangStnHeaderTimetable.setOnClickListener { onClickTimetableButton(stopItem.stopID, R.string.shuttle_bound_for_jungang_stn) }
            binding.shuttleRealtimeToDormitoryHeaderTimetable.setOnClickListener { onClickTimetableButton(stopItem.stopID, R.string.shuttle_bound_for_dormitory) }
            binding.shuttleRealtimeToCampusHeaderTimetable.setOnClickListener { onClickTimetableButton(stopItem.stopID, R.string.shuttle_bound_for_campus) }
            if (index == bookmarkIndex) {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_checked)
            } else {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked)
            }
            binding.infoButton.setOnClickListener {
                onClickInformation(index)
            }
            binding.addBookmarkButton.setOnClickListener { onClickBookmark(index) }
            if (stopItem.routeType.contains(Destination.STATION)) {
                binding.shuttleRealtimeToStationHeader.visibility = View.VISIBLE
                binding.shuttleRealtimeToStationRecyclerView.visibility = View.VISIBLE
            } else {
                binding.shuttleRealtimeToStationHeader.visibility = View.GONE
                binding.shuttleRealtimeToStationRecyclerView.visibility = View.GONE
            }
            if (stopItem.routeType.contains(Destination.TERMINAL)) {
                binding.shuttleRealtimeToTerminalHeader.visibility = View.VISIBLE
                binding.shuttleRealtimeToTerminalRecyclerView.visibility = View.VISIBLE
            } else {
                binding.shuttleRealtimeToTerminalHeader.visibility = View.GONE
                binding.shuttleRealtimeToTerminalRecyclerView.visibility = View.GONE
            }
            if (stopItem.routeType.contains(Destination.CAMPUS)) {
                binding.shuttleRealtimeToCampusHeader.visibility = View.VISIBLE
                binding.shuttleRealtimeToCampusRecyclerView.visibility = View.VISIBLE
            } else {
                binding.shuttleRealtimeToCampusHeader.visibility = View.GONE
                binding.shuttleRealtimeToCampusRecyclerView.visibility = View.GONE
            }
            if (stopItem.routeType.contains(Destination.JUNGANG_STN)) {
                binding.shuttleRealtimeToJungangStnHeader.visibility = View.VISIBLE
                binding.shuttleRealtimeToJungangStnRecyclerView.visibility = View.VISIBLE
            } else {
                binding.shuttleRealtimeToJungangStnHeader.visibility = View.GONE
                binding.shuttleRealtimeToJungangStnRecyclerView.visibility = View.GONE
            }
            if (stopItem.routeType.contains(Destination.DORMITORY)) {
                binding.shuttleRealtimeToDormitoryHeader.visibility = View.VISIBLE
                binding.shuttleRealtimeToDormitoryRecyclerView.visibility = View.VISIBLE
            } else {
                binding.shuttleRealtimeToDormitoryHeader.visibility = View.GONE
                binding.shuttleRealtimeToDormitoryRecyclerView.visibility = View.GONE
            }
            when (stopItem.stopID) {
                R.string.dormitory_o, R.string.shuttlecock_o -> {
                    val arrivalToStation = arrayListOf<ArrivalItem>()
                    val arrivalToTerminal = arrayListOf<ArrivalItem>()
                    val arrivalToJungangStn = arrayListOf<ArrivalItem>()
                    stopItem.arrivalList
                        .forEach {
                            it.arrivalList.forEach { arrivalTime ->
                                when (it.routeTag) {
                                    "DH" -> arrivalToStation.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                    "DY" -> arrivalToTerminal.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                    "DJ" -> {
                                        arrivalToStation.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                        arrivalToJungangStn.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                    }
                                    "C" -> {
                                        arrivalToStation.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                        arrivalToTerminal.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                    }
                                }
                            }
                        }
                    val adapterToStation = ShuttleRealtimeItemAdapter(Destination.STATION, arrivalToStation.sortedBy { it.time })
                    val adapterToTerminal = ShuttleRealtimeItemAdapter(Destination.TERMINAL, arrivalToTerminal.sortedBy { it.time })
                    val adapterToJungangStn = ShuttleRealtimeItemAdapter(Destination.JUNGANG_STN, arrivalToJungangStn.sortedBy { it.time })
                    binding.shuttleRealtimeToStationRecyclerView.adapter = adapterToStation
                    binding.shuttleRealtimeToTerminalRecyclerView.adapter = adapterToTerminal
                    binding.shuttleRealtimeToJungangStnRecyclerView.adapter = adapterToJungangStn
                    binding.shuttleRealtimeToStationRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.shuttleRealtimeToTerminalRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.shuttleRealtimeToJungangStnRecyclerView.layoutManager = LinearLayoutManager(context)
                    if (arrivalToStation.size > 0){
                        binding.shuttleRealtimeToStationRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToStationNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToStationRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToStationNoData.visibility = View.VISIBLE
                    }
                    if (arrivalToTerminal.size > 0){
                        binding.shuttleRealtimeToTerminalRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToTerminalNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToTerminalRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToTerminalNoData.visibility = View.VISIBLE
                    }
                    if (arrivalToJungangStn.size > 0){
                        binding.shuttleRealtimeToJungangStnRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToJungangStnNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToJungangStnRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToJungangStnNoData.visibility = View.VISIBLE
                    }
                }
                R.string.station -> {
                    val arrivalToCampus = arrayListOf<ArrivalItem>()
                    val arrivalToTerminal = arrayListOf<ArrivalItem>()
                    val arrivalToJungangStn = arrayListOf<ArrivalItem>()
                    stopItem.arrivalList
                        .forEach{
                            it.arrivalList.forEach { arrivalTime ->
                                when (it.routeTag) {
                                    "DH" -> arrivalToCampus.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                    "DJ" -> {
                                        arrivalToCampus.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                        arrivalToJungangStn.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                    }
                                    "C" -> {
                                        arrivalToCampus.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                        arrivalToTerminal.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                                    }
                                }
                            }
                        }
                    val adapterToCampus = ShuttleRealtimeItemAdapter(Destination.CAMPUS, arrivalToCampus.sortedBy { it.time })
                    val adapterToTerminal = ShuttleRealtimeItemAdapter(Destination.TERMINAL, arrivalToTerminal.sortedBy { it.time })
                    val adapterToJungangStn = ShuttleRealtimeItemAdapter(Destination.JUNGANG_STN, arrivalToJungangStn.sortedBy { it.time })
                    binding.shuttleRealtimeToCampusRecyclerView.adapter = adapterToCampus
                    binding.shuttleRealtimeToTerminalRecyclerView.adapter = adapterToTerminal
                    binding.shuttleRealtimeToJungangStnRecyclerView.adapter = adapterToJungangStn
                    binding.shuttleRealtimeToCampusRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.shuttleRealtimeToTerminalRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.shuttleRealtimeToJungangStnRecyclerView.layoutManager = LinearLayoutManager(context)
                    if (arrivalToCampus.size > 0){
                        binding.shuttleRealtimeToCampusRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToCampusNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToCampusRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToCampusNoData.visibility = View.VISIBLE
                    }
                    if (arrivalToTerminal.size > 0){
                        binding.shuttleRealtimeToTerminalRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToTerminalNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToTerminalRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToTerminalNoData.visibility = View.VISIBLE
                    }
                    if (arrivalToJungangStn.size > 0){
                        binding.shuttleRealtimeToJungangStnRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToJungangStnNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToJungangStnRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToJungangStnNoData.visibility = View.VISIBLE
                    }
                }
                R.string.terminal, R.string.jungang_stn -> {
                    val arrivalToCampus = arrayListOf<ArrivalItem>()
                    stopItem.arrivalList.forEach {
                        it.arrivalList.forEach { arrivalTime ->
                            arrivalToCampus.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                        }
                    }
                    val adapterToCampus = ShuttleRealtimeItemAdapter(Destination.CAMPUS, arrivalToCampus.sortedBy { it.time }, 11)
                    binding.shuttleRealtimeToCampusRecyclerView.adapter = adapterToCampus
                    binding.shuttleRealtimeToCampusRecyclerView.layoutManager = LinearLayoutManager(context)
                    if (arrivalToCampus.size > 0){
                        binding.shuttleRealtimeToCampusRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToCampusNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToCampusRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToCampusNoData.visibility = View.VISIBLE
                    }
                }
                R.string.shuttlecock_i -> {
                    val arrivalToDormitory = arrayListOf<ArrivalItem>()
                    stopItem.arrivalList
                        .filter { it.routeName.endsWith("D") }
                        .forEach { it.arrivalList.forEach { arrivalTime ->
                            arrivalToDormitory.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                        }
                    }
                    val adapterToDormitory = ShuttleRealtimeItemAdapter(Destination.DORMITORY, arrivalToDormitory.sortedBy { it.time }, 11)
                    binding.shuttleRealtimeToDormitoryRecyclerView.adapter = adapterToDormitory
                    binding.shuttleRealtimeToDormitoryRecyclerView.layoutManager = LinearLayoutManager(context)
                    if (arrivalToDormitory.size > 0){
                        binding.shuttleRealtimeToDormitoryRecyclerView.visibility = View.VISIBLE
                        binding.shuttleRealtimeToDormitoryNoData.visibility = View.GONE
                    } else {
                        binding.shuttleRealtimeToDormitoryRecyclerView.visibility = View.GONE
                        binding.shuttleRealtimeToDormitoryNoData.visibility = View.VISIBLE
                    }
                }
            }
            if (subCardData.title > 0){
                binding.shuttleSubCardTitle.text = resources.getString(subCardData.title)
                binding.shuttleSubCardSubtitle.text = resources.getString(subCardData.subtitle)
                binding.shuttleSubCardLeftTitle.text = resources.getString(subCardData.leftTitle)
                binding.shuttleSubCardRightTitle.text = resources.getString(subCardData.rightTitle)
                val leftAdapter = ShuttleSubCardItemAdapter(index, subCardData.data.left, View.TEXT_ALIGNMENT_VIEW_START)
                val rightAdapter = ShuttleSubCardItemAdapter(index, subCardData.data.right, View.TEXT_ALIGNMENT_VIEW_END)
                if (leftAdapter.itemCount > 0){
                    binding.shuttleSubCardLeftRecyclerView.adapter = leftAdapter
                    binding.shuttleSubCardLeftRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.shuttleSubCardLeftRecyclerView.visibility = View.VISIBLE
                    binding.shuttleSubCardLeftNoData.visibility = View.GONE
                } else {
                    binding.shuttleSubCardLeftRecyclerView.visibility = View.GONE
                    binding.shuttleSubCardLeftNoData.visibility = View.VISIBLE
                }
                if (rightAdapter.itemCount > 0){
                    binding.shuttleSubCardRightRecyclerView.adapter = rightAdapter
                    binding.shuttleSubCardRightRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.shuttleSubCardRightRecyclerView.visibility = View.VISIBLE
                    binding.shuttleSubCardRightNoData.visibility = View.GONE
                } else {
                    binding.shuttleSubCardRightRecyclerView.visibility = View.GONE
                    binding.shuttleSubCardRightNoData.visibility = View.VISIBLE
                }
                binding.shuttleSubCardLeftRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.shuttleSubCardRightRecyclerView.layoutManager = LinearLayoutManager(context)
            } else {
                binding.shuttleSubCard.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_shuttle_realtime, parent, false)
        return ViewHolder(CardShuttleRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, stopList[position], subCardDataList[position])
    }

    override fun getItemCount(): Int = stopList.size

    fun updateStopList(arrivalItemList: List<ArrivalListStopItem>) {
        stopList[0].arrivalList = arrivalItemList.find { it.stopName == "dormitory_o" }?.routeList ?: arrayListOf()
        stopList[1].arrivalList = arrivalItemList.find { it.stopName == "shuttlecock_o" }?.routeList ?: arrayListOf()
        stopList[2].arrivalList = arrivalItemList.find { it.stopName == "station" }?.routeList ?: arrayListOf()
        subCardDataList[2].data.left = arrivalItemList.find { it.stopName == "station" } ?: ArrivalListStopItem("", listOf())
        stopList[3].arrivalList = arrivalItemList.find { it.stopName == "terminal" }?.routeList ?: arrayListOf()
        stopList[4].arrivalList = arrivalItemList.find { it.stopName == "jungang_stn" }?.routeList ?: arrayListOf()
        stopList[5].arrivalList = arrivalItemList.find { it.stopName == "shuttlecock_i" }?.routeList ?: arrayListOf()
        notifyItemRangeChanged(0, 6)
    }

    fun updateSubwayArrival(subwayRealtimeArrival: SubwayRealtimeListResponse) {
        subCardDataList[0].data.left = subwayRealtimeArrival
        subCardDataList[0].data.right = subwayRealtimeArrival
        subCardDataList[1].data.left = subwayRealtimeArrival
        notifyItemRangeChanged(0, 2)
    }

    fun updateBusArrivalToSuwon(busArrival: BusStopRouteItem){
        subCardDataList[1].data.right = busArrival
        notifyItemChanged(1)
    }

    fun updateBusArrivalFromSangnoksu(busArrival: BusStopRouteItem){
        subCardDataList[2].data.right = busArrival
        notifyItemChanged(2)
    }

    fun updateBusArrivalFromGwangmyeong(busArrival: BusStopRouteItem){
        subCardDataList[3].data.left = busArrival
        notifyItemChanged(3)
    }

    fun updateBusArrivalToGwangmyeong(busArrival: BusStopRouteItem){
        subCardDataList[3].data.right = busArrival
        notifyItemChanged(3)
    }

    fun updateBookmarkIndex(bookmarkIndex: Int) {
        this.bookmarkIndex = bookmarkIndex
        notifyItemRangeChanged(0, 6)
    }
}