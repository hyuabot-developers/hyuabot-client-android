package app.kobuggi.hyuabot.ui.bus.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import app.kobuggi.hyuabot.util.UIUtility
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BusTimetableListAdapter(private val context: Context, private var timetableList: List<BusTimetableItem>) : RecyclerView.Adapter<BusTimetableListAdapter.ViewHolder>() {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val currentTime = LocalTime.now()

    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(timetableItem: BusTimetableItem) {
            binding.busRouteText.apply {
                text = timetableItem.routeName
                setTextColor(context.getColor(getRouteColor(timetableItem.routeName)))
            }
            if (timetableItem.time.startsWith("24:")) {
                binding.apply {
                    busTimeText.text = context.getString(
                        R.string.bus_timetable_time_format,
                        timetableItem.time.substring(0, 2),
                        timetableItem.time.substring(3, 5)
                    )
                    busTimeText.setTextColor(
                        if (UIUtility.isDarkModeOn(context.resources)) {
                            context.getColor(android.R.color.white)
                        } else {
                            context.getColor(android.R.color.black)
                        }
                    )
                }
            } else {
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
    fun updateData(newTimetableList: List<BusTimetableItem>) {
        timetableList = newTimetableList
        notifyDataSetChanged()
    }

    fun getRouteColor(routeName: String): Int {
        val redBusList = listOf("110", "707-1", "3100", "3100N", "3101", "3102", "7070", "9090")
        return if (redBusList.contains(routeName)) {
            R.color.red_bus
        } else {
            R.color.green_bus
        }
    }
}
