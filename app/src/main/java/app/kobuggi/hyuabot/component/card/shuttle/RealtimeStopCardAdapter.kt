package app.kobuggi.hyuabot.component.card.shuttle

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.CardShuttleRealtimeBinding
import app.kobuggi.hyuabot.model.shuttle.ArrivalItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListRouteStopItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListStopItem
import app.kobuggi.hyuabot.model.shuttle.Destination

class RealtimeStopCardAdapter(
    private val context: Context,
    private var bookmarkIndex: Int,
    private val onClickBookmark: (Int) -> Unit,
    private val onClickInformation: (Int) -> Unit,
    private val onClickTimetableButton: (Int, Int) -> Unit
) : RecyclerView.Adapter<RealtimeStopCardAdapter.ViewHolder>() {
    private val stopList: List<StopItem> = arrayListOf(
        StopItem(R.string.dormitory_o, linkedMapOf(Destination.STATION to listOf(), Destination.TERMINAL to listOf(), Destination.JUNGANG_STN to listOf())),
        StopItem(R.string.shuttlecock_o, linkedMapOf(Destination.STATION to listOf(), Destination.TERMINAL to listOf(), Destination.JUNGANG_STN to listOf())),
        StopItem(R.string.station, linkedMapOf(Destination.CAMPUS to listOf(), Destination.TERMINAL to listOf(), Destination.JUNGANG_STN to listOf())),
        StopItem(R.string.terminal, linkedMapOf(Destination.CAMPUS to listOf())),
        StopItem(R.string.jungang_stn, linkedMapOf(Destination.CAMPUS to listOf())),
        StopItem(R.string.shuttlecock_i, linkedMapOf(Destination.DORMITORY to listOf())),
    )
    data class StopItem(val stopID: Int, val routeType: HashMap<Destination, List<ArrivalItem>>, var arrivalList: List<ArrivalListRouteStopItem> = listOf())
    inner class ViewHolder(private val binding: CardShuttleRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(index: Int, stopItem: StopItem) {
            val resources = context.resources
            binding.shuttleRealtimeStopName.text = resources.getString(stopItem.stopID)

            val adapter = RealtimeStopListAdapter(context, stopItem.stopID, stopItem.routeType, onClickTimetableButton)
            binding.shuttleRealtimeListView.layoutManager = LinearLayoutManager(context)
            binding.shuttleRealtimeListView.adapter = adapter
            if (index == bookmarkIndex) {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_checked)
            } else {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked)
            }
            binding.infoButton.setOnClickListener {
                onClickInformation(index)
            }
            binding.addBookmarkButton.setOnClickListener { onClickBookmark(index) }
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
                    stopItem.routeType[Destination.STATION] = arrivalToStation
                    stopItem.routeType[Destination.TERMINAL] = arrivalToTerminal
                    stopItem.routeType[Destination.JUNGANG_STN] = arrivalToJungangStn
                    adapter.notifyDataSetChanged()
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
                    stopItem.routeType[Destination.CAMPUS] = arrivalToCampus
                    stopItem.routeType[Destination.TERMINAL] = arrivalToTerminal
                    stopItem.routeType[Destination.JUNGANG_STN] = arrivalToJungangStn
                    adapter.notifyDataSetChanged()
                }
                R.string.terminal, R.string.jungang_stn -> {
                    val arrivalToCampus = arrayListOf<ArrivalItem>()
                    stopItem.arrivalList.forEach {
                        it.arrivalList.forEach { arrivalTime ->
                            arrivalToCampus.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                        }
                    }
                    stopItem.routeType[Destination.CAMPUS] = arrivalToCampus
                    adapter.notifyDataSetChanged()
                }
                R.string.shuttlecock_i -> {
                    val arrivalToDormitory = arrayListOf<ArrivalItem>()
                    stopItem.arrivalList
                        .filter { it.routeName.endsWith("D") }
                        .forEach { it.arrivalList.forEach { arrivalTime ->
                            arrivalToDormitory.add(ArrivalItem(it.routeTag, it.routeName, arrivalTime))
                        }
                    }
                    stopItem.routeType[Destination.DORMITORY] = arrivalToDormitory
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_shuttle_realtime, parent, false)
        return ViewHolder(CardShuttleRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, stopList[position])
    }

    override fun getItemCount(): Int = stopList.size

    fun updateStopList(arrivalItemList: List<ArrivalListStopItem>) {
        stopList[0].arrivalList = arrivalItemList.find { it.stopName == "dormitory_o" }?.routeList ?: arrayListOf()
        stopList[1].arrivalList = arrivalItemList.find { it.stopName == "shuttlecock_o" }?.routeList ?: arrayListOf()
        stopList[2].arrivalList = arrivalItemList.find { it.stopName == "station" }?.routeList ?: arrayListOf()
        stopList[3].arrivalList = arrivalItemList.find { it.stopName == "terminal" }?.routeList ?: arrayListOf()
        stopList[4].arrivalList = arrivalItemList.find { it.stopName == "jungang_stn" }?.routeList ?: arrayListOf()
        stopList[5].arrivalList = arrivalItemList.find { it.stopName == "shuttlecock_i" }?.routeList ?: arrayListOf()
        notifyItemRangeChanged(0, 6)
    }

    fun updateBookmarkIndex(bookmarkIndex: Int) {
        this.bookmarkIndex = bookmarkIndex
        notifyItemRangeChanged(0, 6)
    }
}