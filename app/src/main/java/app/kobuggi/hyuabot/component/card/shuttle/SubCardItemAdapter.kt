package app.kobuggi.hyuabot.component.card.shuttle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemShuttleSubCardItemBinding
import app.kobuggi.hyuabot.model.bus.BusRouteRealtimeItem
import app.kobuggi.hyuabot.model.bus.BusStopRouteItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListStopItem
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeItemResponse
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeListResponse
import java.time.Duration
import java.time.LocalTime

class SubCardItemAdapter (private val context: Context, private val cardIndex: Int, private val subCardItemList: SubCardItem, private val textAlignment: Int, private val count: Int = 3) : RecyclerView.Adapter<SubCardItemAdapter.ViewHolder>() {
    private val shuttleArrivalList = arrayListOf<Int>()
    init {
        if (subCardItemList.javaClass == ArrivalListStopItem::class.java){
            for (route in (subCardItemList as ArrivalListStopItem).routeList){
                shuttleArrivalList.addAll(route.arrivalList)
            }
            shuttleArrivalList.sort()
        }
    }
    inner class ViewHolder(private val binding: ItemShuttleSubCardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindSubwayArrival(subwayRealtimeItem: SubwayRealtimeItemResponse) {
            val resources = context.resources
            binding.shuttleSubCardItem.textAlignment = textAlignment
            binding.shuttleSubCardItem.text = resources.getString(R.string.shuttle_sub_card_subway_item, subwayRealtimeItem.time, subwayRealtimeItem.terminalStation)
        }

        fun bindBusRealtimeArrival(realtimeItem: BusRouteRealtimeItem) {
            val resources = context.resources
            binding.shuttleSubCardItem.textAlignment = textAlignment
            if (realtimeItem.remainedSeat >= 0){
                binding.shuttleSubCardItem.text = resources.getString(R.string.shuttle_sub_card_bus_item_seat, realtimeItem.remainedTime, realtimeItem.remainedSeat)
            } else {
                binding.shuttleSubCardItem.text = resources.getString(R.string.shuttle_sub_card_bus_item, realtimeItem.remainedTime)
            }
        }

        fun bindBusTimetableArrival(timetableItem: String) {
            val timeDelta = if (cardIndex == 1  && textAlignment == View.TEXT_ALIGNMENT_VIEW_END) {
                17
            } else if (cardIndex == 3 && textAlignment == View.TEXT_ALIGNMENT_VIEW_END) {
                20
            } else {
                0
            }
            val now = LocalTime.now()
            val arrivalTime = LocalTime.parse(timetableItem)
            val resources = context.resources
            binding.shuttleSubCardItem.textAlignment = textAlignment
            binding.shuttleSubCardItem.text = resources.getString(R.string.shuttle_sub_card_bus_item, Duration.between(now, arrivalTime).toMinutes() + timeDelta)
        }

        fun bindShuttleArrival(arrivalTime: Int) {
            val resources = context.resources
            binding.shuttleSubCardItem.textAlignment = textAlignment
            binding.shuttleSubCardItem.text = resources.getString(R.string.shuttle_sub_card_shuttle_item, arrivalTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle_sub_card_item, parent, false)
        return ViewHolder(ItemShuttleSubCardItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (subCardItemList.javaClass == SubwayRealtimeListResponse::class.java) {
            val subCardItemList = subCardItemList as SubwayRealtimeListResponse
            if(textAlignment == View.TEXT_ALIGNMENT_VIEW_START){
                if (cardIndex == 0){
                    holder.bindSubwayArrival(subCardItemList.down[position])
                } else {
                    holder.bindSubwayArrival(subCardItemList.up[position])
                }
            } else {
                holder.bindSubwayArrival(subCardItemList.up[position])
            }
        } else if (subCardItemList.javaClass == BusStopRouteItem::class.java) {
            val subCardItemList = subCardItemList as BusStopRouteItem
            if (cardIndex == 3 && textAlignment == View.TEXT_ALIGNMENT_VIEW_END) {
                if (position < subCardItemList.realtime.size) {
                    holder.bindBusRealtimeArrival(subCardItemList.realtime[position])
                } else {
                    holder.bindBusTimetableArrival(subCardItemList.startStop.timetable[position - subCardItemList.realtime.size])
                }
            } else {
                holder.bindBusTimetableArrival(subCardItemList.startStop.timetable[position])
            }
        } else if (subCardItemList.javaClass == ArrivalListStopItem::class.java) {
            holder.bindShuttleArrival(shuttleArrivalList[position])
        }
    }

    override fun getItemCount(): Int {
        return if (subCardItemList.javaClass == SubwayRealtimeListResponse::class.java) {
            val subCardItemList = subCardItemList as SubwayRealtimeListResponse
            if(textAlignment == View.TEXT_ALIGNMENT_VIEW_START){
                if (cardIndex == 0){
                    if (subCardItemList.down.size > count) count else subCardItemList.down.size
                } else {
                    if (subCardItemList.up.size > count) count else subCardItemList.up.size
                }
            } else {
                if (subCardItemList.up.size > count) count else subCardItemList.up.size
            }
        } else if (subCardItemList.javaClass == BusStopRouteItem::class.java) {
            val subCardItemList = subCardItemList as BusStopRouteItem
            if (cardIndex == 3 && textAlignment == View.TEXT_ALIGNMENT_VIEW_END) {
                if (subCardItemList.realtime.size + subCardItemList.startStop.timetable.size > count) count else subCardItemList.realtime.size + subCardItemList.startStop.timetable.size
            } else {
                if (subCardItemList.startStop.timetable.size > count) count else subCardItemList.startStop.timetable.size
            }
        } else if (subCardItemList.javaClass == ArrivalListStopItem::class.java) {
            if (shuttleArrivalList.size > count) count else shuttleArrivalList.size
        } else {
            0
        }
    }

}