package app.kobuggi.hyuabot.ui.bus.realtime

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRealtimeBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

private const val DEFAULT_MINIMUM_DISPATCH_MINUTES = 10

class BusRealtimeListAdapter(
    private var arrivalList: List<BusArrivalItem> = emptyList(),
    private val maxCount: Int = 5,
) : RecyclerView.Adapter<BusRealtimeListAdapter.ViewHolder>() {
    private var showSecondaryEta: Boolean = true

    @SuppressLint("NotifyDataSetChanged")
    fun setShowSecondaryEta(show: Boolean) {
        if (showSecondaryEta == show) return
        showSecondaryEta = show
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemBusRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        private val hourFormatter = DateTimeFormatter.ofPattern("HH")
        private val minuteFormatter = DateTimeFormatter.ofPattern("mm")
        private val secondaryTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val godoTypeface by lazy {
            ResourcesCompat.getFont(binding.root.context, R.font.godo)
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: BusArrivalItem) {
            binding.busTimeText.textSize = 15f
            val routeName = item.route
            val arrival = item.item
            binding.busRouteIndicator.backgroundTintList = ColorStateList.valueOf(
                binding.root.context.getColor(getRouteColor(routeName)),
            )
            val secondarySuffix = if (showSecondaryEta) {
                val destinationStopName = busDestinationStopNameResource(item.destinationStopID)
                    ?.let(binding.root.context::getString)
                val destinationArrivalTime = item.destinationArrivalTime ?: item.secondaryArrivalTime
                if (destinationStopName != null && destinationArrivalTime != null) {
                    binding.root.context.getString(
                        R.string.bus_arrival_secondary_format_with_stop,
                        destinationStopName,
                        destinationArrivalTime.format(secondaryTimeFormatter),
                    )
                } else ""
            } else {
                ""
            }
            binding.busLowFloorBadge.visibility = if (arrival.lowFloor == true) android.view.View.VISIBLE else android.view.View.GONE
            val item = arrival
            if (item.isRealtime) {
                binding.busRouteText.apply {
                    text = routeName
                    setTextColor(binding.root.context.getColor(R.color.primary_text))
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
                binding.busTimeText.applyRealtimeColor(listOf(realtimeText, secondarySuffix).filter(String::isNotEmpty).joinToString("\n"))
            } else {
                binding.busRouteText.apply {
                    text = routeName
                    setTextColor(binding.root.context.getColor(R.color.primary_text))
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
                        text = listOf(
                            binding.root.context.getString(R.string.bus_arrival_estimated_format, remainingMinutes),
                            secondarySuffix,
                        ).filter(String::isNotEmpty).joinToString("\n")
                    }
                    setTextColor(binding.root.context.getColor(R.color.primary_text))
                    setTypeface(godoTypeface, Typeface.NORMAL)
                }
            }
        }

        private fun android.widget.TextView.applyRealtimeColor(value: String) {
            text = value
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
        val acceptedByRoute = mutableMapOf<String, Double>()
        val realtime = newArrivalList
            .filter { it.item.isRealtime && (it.remainingMinutes ?: Double.MAX_VALUE) >= 0.0 }
            .sortedBy { it.remainingMinutes ?: Double.MAX_VALUE }
            .onEach { item -> acceptedByRoute[item.route] = item.remainingMinutes ?: Double.MAX_VALUE }
        val logs = newArrivalList
            .filter { !it.item.isRealtime && (it.remainingMinutes ?: Double.MAX_VALUE) >= 0.0 }
            .sortedBy { it.remainingMinutes ?: Double.MAX_VALUE }
            .filter { item ->
                val remaining = item.remainingMinutes ?: return@filter false
                val previous = acceptedByRoute[item.route]
                val interval = item.minimumDispatchMinutes?.toDouble()
                    ?: DEFAULT_MINIMUM_DISPATCH_MINUTES.toDouble()
                val valid = previous == null || remaining - previous >= interval
                if (valid) acceptedByRoute[item.route] = remaining
                valid
            }
        arrivalList = realtime + logs
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
