package app.kobuggi.hyuabot.component.card.shuttle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemShuttleRealtimeBinding
import app.kobuggi.hyuabot.model.shuttle.ArrivalItem
import app.kobuggi.hyuabot.model.shuttle.Destination

class ShuttleRealtimeItemAdapter (private val destination: Destination, private var arrivalItemList: List<ArrivalItem>, private val count: Int = 3) : RecyclerView.Adapter<ShuttleRealtimeItemAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemShuttleRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(arrivalItem: ArrivalItem) {
            val resources = GlobalApplication.getAppResources()
            if (destination == Destination.DORMITORY) {
                binding.shuttleRealtimeItemRoute.text = resources.getString(R.string.D)
                binding.shuttleRealtimeItemRoute.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
            } else {
                when (arrivalItem.routeTag) {
                    "DH", "DY", "DJ" -> {
                        binding.shuttleRealtimeItemRoute.text = resources.getString(R.string.D)
                        binding.shuttleRealtimeItemRoute.setTextColor(ResourcesCompat.getColor(resources, android.R.color.holo_red_dark, null))
                    }
                    "C" -> {
                        binding.shuttleRealtimeItemRoute.text = resources.getString(R.string.C)
                        binding.shuttleRealtimeItemRoute.setTextColor(ResourcesCompat.getColor(resources, android.R.color.holo_blue_dark, null))
                    }
                }
            }
            binding.shuttleRealtimeItemTime.text = GlobalApplication.getAppResources().getString(R.string.shuttle_arrival_time, arrivalItem.time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle_realtime, parent, false)
        return ViewHolder(ItemShuttleRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrivalItemList[position])
    }

    override fun getItemCount(): Int = if (arrivalItemList.size > count) count else arrivalItemList.size

}