package app.kobuggi.hyuabot

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.databinding.ItemBusArrivalBinding

class ShuttleArrivalAdapter(private val context: Context, private val stopID: Int, private var arrivalList: List<Int>) :
    RecyclerView.Adapter<ShuttleArrivalAdapter.ViewHolder>() {
    inner class ViewHolder(private val context: Context, private val binding: ItemBusArrivalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int) {
            binding.shuttleHeadingType.text = context.getString(ShuttleData.getStopHeading(stopID)[index])
            if (arrivalList[index] == -1) {
                binding.shuttleArrivalTime.text = context.getString(R.string.out_of_service)
            } else {
                binding.shuttleArrivalTime.text = context.getString(R.string.remaining_time, arrivalList[index])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_arrival, parent, false)
        return ViewHolder(context, ItemBusArrivalBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = ShuttleData.getStopHeading(stopID).size

    fun updateList(newList: List<Int>) {
        arrivalList = newList
        notifyItemRangeChanged(0, arrivalList.size)
    }
}