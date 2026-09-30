package app.kobuggi.hyuabot.ui.subway.realtime

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemSubwayRealtimeBinding

class SubwayRealtimeListAdapter(
    private val context: Context,
    @param:ColorRes private val destinationColor: Int,
    private var arrivals: List<SubwayRealtimePageQuery.Entry> = emptyList(),
) : RecyclerView.Adapter<SubwayRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(arrival: SubwayRealtimePageQuery.Entry) {
            binding.subwayLineIndicator.backgroundTintList = ColorStateList.valueOf(context.getColor(destinationColor))
            binding.subwayDestinationText.setTextColor(context.getColor(R.color.primary_text))
            if (arrival.isRealtime) {
                if (arrival.isLast!!) {
                    binding.subwayDestinationText.apply {
                        text = context.getString(
                            R.string.subway_realtime_destination_format_last,
                            arrival.terminal.name,
                        )
                    }
                } else {
                    binding.subwayDestinationText.text = context.getString(
                        R.string.subway_realtime_destination_format,
                        arrival.terminal.name,
                    )
                }
                val realtimeText = if (arrival.stops != null && arrival.stops > 0) {
                    context.resources.getQuantityString(
                        R.plurals.subway_realtime_format,
                        arrival.minutes,
                        arrival.minutes,
                        arrival.location ?: '-',
                        arrival.stops
                    )
                } else {
                    context.resources.getQuantityString(
                        R.plurals.subway_realtime_timetable_format,
                        arrival.minutes,
                        arrival.minutes,
                    )
                }
                binding.subwayTimeText.applyRealtimeColor(realtimeText)
            } else {
                binding.apply {
                    subwayDestinationText.text = context.getString(
                        R.string.subway_realtime_destination_format,
                        arrival.terminal.name,
                    )
                    val arrivalText = context.resources.getQuantityString(
                        R.plurals.subway_realtime_timetable_format,
                        arrival.minutes,
                        arrival.minutes,
                    )
                    subwayTimeText.text = "$arrivalText ${context.getString(R.string.transit_arrival_scheduled_suffix)}"
                    subwayTimeText.setTextColor(context.getColor(R.color.primary_text))
                }
            }
        }

        private fun android.widget.TextView.applyRealtimeColor(value: String) {
            text = value
            setTextColor(context.getColor(R.color.primary_text))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_realtime, parent, false)
        return ViewHolder(ItemSubwayRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrivals[position])
    }

    override fun getItemCount(): Int = arrivals.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newArrivals: List<SubwayRealtimePageQuery.Entry>) {
        arrivals = newArrivals
        notifyDataSetChanged()
    }

}
