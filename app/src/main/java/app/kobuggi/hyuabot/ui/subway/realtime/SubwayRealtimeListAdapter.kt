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

}
