package app.kobuggi.hyuabot.ui.bus.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import app.kobuggi.hyuabot.util.UIUtility
import kotlin.math.min

class BusRealtimeListAdapter(
    private val context: Context,
    private var realtimeList: List<BusRealtimePageQuery.Realtime>,
    private var timetableList: List<BusRealtimePageQuery.Timetable>,
    private val timetableNotExist: Boolean = false,
) : RecyclerView.Adapter<BusRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(realtimeItem: BusRealtimePageQuery.Realtime?, timetableItem: BusRealtimePageQuery.Timetable?, position: Int) {
            if (realtimeItem != null) {
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
                if (position == realtimeList.size + timetableList.size - 1) {
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
    fun updateData(newRealtimeList: List<BusRealtimePageQuery.Realtime>, newTimetableList: List<BusRealtimePageQuery.Timetable>) {
        realtimeList = newRealtimeList
        timetableList = newTimetableList
        notifyDataSetChanged()
    }
}