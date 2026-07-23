package app.kobuggi.hyuabot.ui.bus.realtime

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

class BusRealtimeListAdapter(
    private var arrivalList: List<BusArrivalItem> = emptyList(),
    private val maxCount: Int = 5,
) : RecyclerView.Adapter<BusRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        private val hourFormatter = DateTimeFormatter.ofPattern("HH")
        private val minuteFormatter = DateTimeFormatter.ofPattern("mm")
        private val godoTypeface by lazy {
            ResourcesCompat.getFont(binding.root.context, R.font.godo)
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: BusArrivalItem) {
            val routeName = item.route
            val arrival = item.item
            binding.busLowFloorBadge.visibility = if (arrival.lowFloor == true) android.view.View.VISIBLE else android.view.View.GONE
            val item = arrival
            if (item.isRealtime) {
                binding.busRouteText.apply {
                    text = routeName
                    setTextColor(ResourcesCompat.getColor(resources, getRouteColor(routeName), null))
                }
                val realtimeText = if (item.seats!! >= 0) {
                    binding.root.context.resources.getQuantityString(
                        R.plurals.bus_realtime_format_seats,
                        item.minutes!!,
                        item.minutes,
                        item.stops,
                        item.seats
                    )
                } else {
                    binding.root.context.resources.getQuantityString(
                        R.plurals.bus_realtime_format_no_seats,
                        item.minutes!!,
                        item.minutes,
                        item.stops
                    )
                }
                binding.busTimeText.applyRealtimeColor(realtimeText)
            } else {
                binding.busRouteText.apply {
                    text = routeName
                    setTextColor(ResourcesCompat.getColor(resources, getRouteColor(routeName), null))
                }
                binding.busTimeText.apply {
                    val arrivalTime = item.arrivalTime
                    if (arrivalTime != null) {
                        val now = LocalTime.now()
                        val toServiceSec = { t: LocalTime ->
                            val s = t.toSecondOfDay()
                            if (s < 4 * 3600) s + 86400 else s
                        }
                        val remainingMinutes = (toServiceSec(arrivalTime) - toServiceSec(now)) / 60
                        text = binding.root.context.getString(R.string.bus_arrival_estimated_format, remainingMinutes)
                    }
                    setTextColor(binding.root.context.getColor(R.color.secondary_text))
                    setTypeface(godoTypeface, Typeface.NORMAL)
                }
            }
        }

        private fun android.widget.TextView.applyRealtimeColor(value: String) {
            val styled = SpannableString(value)
            val delimiter = value.indexOf('(')
            if (delimiter > 0) {
                styled.setSpan(
                    ForegroundColorSpan(context.getColor(R.color.calendar_sunday)),
                    0,
                    (delimiter - 1).coerceAtLeast(0),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            text = styled
            setTextColor(context.getColor(R.color.primary_text))
            setTypeface(godoTypeface, Typeface.NORMAL)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_realtime, parent, false)
        return ViewHolder(ItemBusRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrivalList[position])
    }

    override fun getItemCount(): Int = min(arrivalList.size, maxCount)

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
