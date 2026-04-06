package app.kobuggi.hyuabot.ui.bus.realtime

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import app.kobuggi.hyuabot.util.UIUtility
import java.time.format.DateTimeFormatter
import kotlin.math.min

class BusRealtimeListAdapter(
    private var arrivalList: List<BusArrivalItem> = emptyList()
) : RecyclerView.Adapter<BusRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        private val hourFormatter = DateTimeFormatter.ofPattern("HH")
        private val minuteFormatter = DateTimeFormatter.ofPattern("mm")

        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: BusArrivalItem, position: Int) {
            val routeName = item.route
            val item = item.item
            if (item.isRealtime) {
                binding.busRouteText.apply {
                    text = routeName
                    setTextColor(ResourcesCompat.getColor(resources, getRouteColor(routeName), null))
                }
                if (position == arrivalList.size - 1) {
                    if (item.seats!! >= 0) {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_seats_last, item.minutes, item.stops, item.seats)
                            setTextColor(context.getColor(R.color.red_bus))
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        }
                    } else {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_no_seats_last, item.minutes, item.stops)
                            setTextColor(context.getColor(R.color.red_bus))
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        }
                    }
                } else {
                    if (item.seats!! >= 0) {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_seats, item.minutes, item.stops, item.seats)
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                            setTypeface(typeface, android.graphics.Typeface.NORMAL)
                        }
                    } else {
                        binding.busTimeText.apply {
                            text = context.getString(R.string.bus_realtime_format_no_seats, item.minutes, item.stops)
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                            setTypeface(typeface, android.graphics.Typeface.NORMAL)
                        }
                    }
                }
            } else {
                binding.busRouteText.apply {
                    text = routeName
                    setTextColor(ResourcesCompat.getColor(resources, getRouteColor(routeName), null))
                }
                if (position == arrivalList.size - 1) {
                    binding.busTimeText.apply {
                        text = context.getString(
                            R.string.bus_timetable_format_last,
                            hourFormatter.format(item.time),
                            minuteFormatter.format(item.time)
                        )
                        setTextColor(context.getColor(R.color.red_bus))
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }
                } else {
                    binding.busTimeText.apply {
                        text = context.getString(
                            R.string.bus_timetable_format,
                            hourFormatter.format(item.time),
                            minuteFormatter.format(item.time)
                        )
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
        holder.bind(arrivalList[position], position)
    }

    override fun getItemCount(): Int = min(arrivalList.size, 5)

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newArrivalList: List<BusArrivalItem>) {
        arrivalList = newArrivalList
        notifyDataSetChanged()
    }

    fun getRouteColor(routeName: String): Int {
        val redBusList = listOf("3100", "3100N", "3101", "3102", "7070", "9090")
        return if (redBusList.contains(routeName)) {
            R.color.red_bus
        } else {
            R.color.green_bus
        }
    }
}
