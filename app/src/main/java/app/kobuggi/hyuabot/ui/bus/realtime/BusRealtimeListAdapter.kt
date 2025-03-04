package app.kobuggi.hyuabot.ui.bus.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import app.kobuggi.hyuabot.util.UIUtility
import kotlin.math.min

class BusRealtimeListAdapter(
    private val context: Context,
    private var realtimeList: List<BusRealtimeItem>,
    private var timetableList: List<BusTimetableItem>,
    private val timetableNotExist: Boolean = false,
) : RecyclerView.Adapter<BusRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(realtimeItem: BusRealtimeItem?, timetableItem: BusTimetableItem?, position: Int) {
            if (realtimeItem != null) {
                binding.busRouteText.apply {
                    text = realtimeItem.routeName
                    setTextColor(ResourcesCompat.getColor(resources, getRouteColor(realtimeItem.routeName), null))
                }
                if ((timetableList.isEmpty() && !timetableNotExist) && position == realtimeList.size - 1) {
                    if (realtimeItem.seat >= 0) {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_seats_last, realtimeItem.time.toInt(), realtimeItem.stop, realtimeItem.seat)
                            setTextColor(context.getColor(R.color.red_bus))
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        }
                    } else {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_no_seats_last, realtimeItem.time.toInt(), realtimeItem.stop)
                            setTextColor(context.getColor(R.color.red_bus))
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        }
                    }
                } else {
                    if (realtimeItem.seat >= 0) {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_seats, realtimeItem.time.toInt(), realtimeItem.stop, realtimeItem.seat)
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                            setTypeface(typeface, android.graphics.Typeface.NORMAL)
                        }
                    } else {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_no_seats, realtimeItem.time.toInt(), realtimeItem.stop)
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                            setTypeface(typeface, android.graphics.Typeface.NORMAL)
                        }
                    }
                }
            } else if (timetableItem != null) {
                binding.busRouteText.apply {
                    text = timetableItem.routeName
                    setTextColor(ResourcesCompat.getColor(resources, getRouteColor(timetableItem.routeName), null))
                }
                if (position == realtimeList.size + timetableList.size - 1 && !timetableNotExist) {
                    binding.busTimeText.apply {
                        text = context.getString(R.string.bus_timetable_format_last, timetableItem.time.substring(0, 2), timetableItem.time.substring(3, 5))
                        setTextColor(context.getColor(R.color.red_bus))
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }
                } else {
                    binding.busTimeText.apply {
                        text = context.getString(R.string.bus_timetable_format, timetableItem.time.substring(0, 2), timetableItem.time.substring(3, 5))
                        setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                            context.getColor(android.R.color.white)
                        } else {
                            context.getColor(android.R.color.black)
                        })
                        setTypeface(typeface, android.graphics.Typeface.NORMAL)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_realtime, parent, false)
        return ViewHolder(ItemBusRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < realtimeList.size) {
            holder.bind(realtimeList[position], null, position)
        } else {
            holder.bind(null, timetableList[position - realtimeList.size], position)
        }
    }

    override fun getItemCount(): Int = min(realtimeList.size + timetableList.size, 5)

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRealtimeList: List<BusRealtimeItem>, newTimetableList: List<BusTimetableItem>) {
        realtimeList = newRealtimeList
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
