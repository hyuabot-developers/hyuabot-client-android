package app.kobuggi.hyuabot.ui.bus.realtime

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BusDepartureLogDialogQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusLogBinding
import app.kobuggi.hyuabot.util.UIUtility
import java.time.LocalTime

class BusDepartureLogAdapter(
    private val context: Context,
    private var logList: List<BusDepartureLogDialogQuery.Log>,
    private var currentTime: LocalTime = LocalTime.now()
) : RecyclerView.Adapter<BusDepartureLogAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBusLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, item: BusDepartureLogDialogQuery.Log) {
            binding.apply {
                busTimeText.text = item.departureTime.toString().substring(0, 5)
                if (position % 2 == 0) {
                    busItem.setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.hanyang_blue, null))
                    if (currentTime.isAfter(LocalTime.parse(item.departureTime.toString().substring(0, 5)))) {
                        busTimeText.setTextColor(ResourcesCompat.getColor(context.resources, android.R.color.darker_gray, null))
                    } else {
                        busTimeText.setTextColor(ResourcesCompat.getColor(context.resources, android.R.color.white, null))
                    }
                } else {
                    busItem.setBackgroundColor(ResourcesCompat.getColor(context.resources, android.R.color.transparent, null))
                    if (currentTime.isAfter(LocalTime.parse(item.departureTime.toString().substring(0, 5)))) {
                        busTimeText.setTextColor(ResourcesCompat.getColor(context.resources, android.R.color.darker_gray, null))
                    } else {
                        if (UIUtility.isDarkModeOn(context.resources)) {
                            busTimeText.setTextColor(ResourcesCompat.getColor(context.resources, android.R.color.white, null))
                        } else {
                            busTimeText.setTextColor(ResourcesCompat.getColor(context.resources, android.R.color.black, null))
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_log, parent, false)
        return ViewHolder(ItemBusLogBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, logList[position])
    }

    override fun getItemCount(): Int = logList.size

    fun updateData(newData: List<BusDepartureLogDialogQuery.Log>) {
        currentTime = LocalTime.now()
        if (logList.size > newData.size) {
            logList = newData
            notifyItemRangeChanged(0, logList.size)
            notifyItemRangeInserted(logList.size, newData.size - logList.size)
        } else if (logList.size < newData.size) {
            logList = newData
            notifyItemRangeChanged(0, newData.size)
            notifyItemRangeRemoved(newData.size, logList.size - newData.size)
        } else {
            logList = newData
            notifyItemRangeChanged(0, logList.size)
        }
    }
}
