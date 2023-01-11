package app.kobuggi.hyuabot.component.card.bus

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import app.kobuggi.hyuabot.ui.bus.realtime.RealtimeItem

class RealtimeItemAdapter(
    private val context: Context,
    private val showRouteName: Boolean,
    private val realtimeList: List<RealtimeItem>,
    private val timetableList: List<String>,
    private val count: Int = 3
) : RecyclerView.Adapter<RealtimeItemAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(realtimeItem: RealtimeItem) {
            val resources = context.resources
            if (showRouteName) {
                binding.busRealtimeItem.text = resources.getString(R.string.bus_arrival_realtime_with_seat_route,
                    realtimeItem.routeName, realtimeItem.remainedTime, realtimeItem.remainedSeat)
            } else if (realtimeItem.remainedSeat >= 0) {
                binding.busRealtimeItem.text = resources.getString(R.string.bus_arrival_realtime_with_seat,
                    realtimeItem.remainedTime, realtimeItem.remainedSeat)
            } else {
                binding.busRealtimeItem.text = resources.getString(R.string.bus_arrival_realtime, realtimeItem.remainedTime)
            }
        }

        fun bind(timetableItem: String) {
            val resources = context.resources
            binding.busRealtimeItem.text = resources.getString(R.string.bus_arrival_timetable,
                timetableItem.split(":")[0], timetableItem.split(":")[1])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_realtime, parent, false)
        return ViewHolder(ItemBusRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < realtimeList.size) {
            holder.bind(realtimeList[position])
        } else {
            holder.bind(timetableList[position - realtimeList.size])
        }
    }

    override fun getItemCount(): Int = if (realtimeList.size + timetableList.size > count) count else realtimeList.size + timetableList.size

}