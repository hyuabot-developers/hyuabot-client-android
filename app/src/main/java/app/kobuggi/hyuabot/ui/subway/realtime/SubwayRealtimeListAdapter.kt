package app.kobuggi.hyuabot.ui.subway.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemSubwayRealtimeBinding

class SubwayRealtimeListAdapter(
    private val context: Context,
    @ColorRes private val destinationColor: Int,
    private var arrivals: List<SubwayRealtimePageQuery.Entry> = emptyList(),
) : RecyclerView.Adapter<SubwayRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(arrival: SubwayRealtimePageQuery.Entry) {
            binding.subwayDestinationText.setTextColor(context.getColor(destinationColor))
            if (arrival.isRealtime) {
                if (arrival.isLast!!) {
                    binding.subwayDestinationText.apply {
                        text = context.getString(
                            R.string.subway_realtime_destination_format_last,
                            getTerminalString(arrival.terminal.stationID),
                        )
                    }
                } else {
                    binding.subwayDestinationText.text = context.getString(
                        R.string.subway_realtime_destination_format,
                        getTerminalString(arrival.terminal.stationID),
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
                        getTerminalString(arrival.terminal.stationID),
                    )
                    subwayTimeText.text = context.resources.getQuantityString(
                        R.plurals.subway_realtime_timetable_format,
                        arrival.minutes,
                        arrival.minutes,
                    )
                    subwayTimeText.setTextColor(context.getColor(R.color.primary_text))
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

    private fun getTerminalString(terminal: String): String {
        return when (terminal) {
            "K209" -> context.getString(R.string.subway_station_K209)
            "K210" -> context.getString(R.string.subway_station_K210)
            "K233" -> context.getString(R.string.subway_station_K233)
            "K246" -> context.getString(R.string.subway_station_K246)
            "K258" -> context.getString(R.string.subway_station_K258)
            "K272" -> context.getString(R.string.subway_station_K272)
            "K409" -> context.getString(R.string.subway_station_K409)
            "K411" -> context.getString(R.string.subway_station_K411)
            "K419" -> context.getString(R.string.subway_station_K419)
            "K433" -> context.getString(R.string.subway_station_K433)
            "K443" -> context.getString(R.string.subway_station_K443)
            "K444" -> context.getString(R.string.subway_station_K444)
            "K453" -> context.getString(R.string.subway_station_K453)
            "K456" -> context.getString(R.string.subway_station_K456)
            else -> terminal
        }
    }
}
