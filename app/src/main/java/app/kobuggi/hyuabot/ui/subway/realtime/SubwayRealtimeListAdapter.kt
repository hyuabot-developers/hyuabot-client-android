package app.kobuggi.hyuabot.ui.subway.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemSubwayRealtimeBinding

class SubwayRealtimeListAdapter(
    private val context: Context,
    private var arrivals: List<SubwayRealtimePageQuery.Entry> = emptyList(),
) : RecyclerView.Adapter<SubwayRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(arrival: SubwayRealtimePageQuery.Entry) {
            if (arrival.isRealtime) {
                if (arrival.isLast!!) {
                    binding.subwayDestinationText.apply {
                        text = context.getString(
                            R.string.subway_realtime_destination_format_last,
                            getTerminalString(arrival.terminal.stationID),
                        )
                        setTextColor(context.getColor(android.R.color.holo_red_light))
                    }
                } else {
                    binding.subwayDestinationText.text = context.getString(
                        R.string.subway_realtime_destination_format,
                        getTerminalString(arrival.terminal.stationID),
                    )
                }
                binding.subwayTimeText.text = context.getString(
                    R.string.subway_realtime_format,
                    arrival.minutes,
                    arrival.location ?: '-'
                )
            } else {
                binding.apply {
                    subwayDestinationText.text = context.getString(
                        R.string.subway_realtime_destination_format,
                        getTerminalString(arrival.terminal.stationID),
                    )
                    subwayTimeText.text = context.getString(
                        R.string.subway_realtime_timetable_format,
                        arrival.minutes,
                        arrival.origin?.let { origin -> getTerminalString(origin.stationID) } ?: '-'
                    )
                }
            }
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
