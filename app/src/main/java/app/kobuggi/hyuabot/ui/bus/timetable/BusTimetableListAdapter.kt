package app.kobuggi.hyuabot.ui.bus.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BusTimetablePageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import app.kobuggi.hyuabot.util.UIUtility
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BusTimetableListAdapter(private val context: Context, private var timetableList: List<BusTimetablePageQuery.Timetable>) : RecyclerView.Adapter<BusTimetableListAdapter.ViewHolder>() {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val currentTime = LocalTime.now()

    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(timetableItem: BusTimetablePageQuery.Timetable) {
            val time = LocalTime.parse(timetableItem.time, dateTimeFormatter)
            binding.apply {
                busTimeText.text = context.getString(
                    R.string.bus_timetable_time_format,
                    time.hour.toString().padStart(2, '0'),
                    time.minute.toString().padStart(2, '0')
                )
                busTimeText.setTextColor(
                    if (currentTime.isAfter(time)) {
                        context.getColor(android.R.color.darker_gray)
                    } else {
                        if (UIUtility.isDarkModeOn(context.resources)) {
                            context.getColor(android.R.color.white)
                        } else {
                            context.getColor(android.R.color.black)
                        }
                    }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_realtime, parent, false)
        return ViewHolder(ItemBusRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timetableList[position])
    }

    override fun getItemCount(): Int = timetableList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newTimetableList: List<BusTimetablePageQuery.Timetable>) {
        timetableList = newTimetableList
        notifyDataSetChanged()
    }
}
