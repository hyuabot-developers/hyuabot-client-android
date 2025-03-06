package app.kobuggi.hyuabot.ui.shuttle.via

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleTimetablePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ShuttleViaListAdapter(
    private val context: Context,
    private val realtimeViaStops: List<ShuttleRealtimePageQuery.Vium> = listOf(),
    private val timetableViaStops: List<ShuttleTimetablePageQuery.Vium> = listOf()
) : RecyclerView.Adapter<ShuttleViaListAdapter.ViewHolder>() {
    private val datetimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    inner class ViewHolder(private val binding: ItemShuttleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShuttleRealtimePageQuery.Vium) {
            binding.shuttleTypeText.text = setStopText(item.stop)
            // Departure time
            val departureTime = LocalTime.parse(item.time, datetimeFormatter)
            binding.shuttleTimeText.text = context.getString(
                R.string.shuttle_time_type_3,
                departureTime.hour.toString().padStart(2, '0'),
                departureTime.minute.toString().padStart(2, '0')
            )
        }
        fun bind(item: ShuttleTimetablePageQuery.Vium) {
            binding.shuttleTypeText.text = setStopText(item.stop)
            // Departure time
            val departureTime = LocalTime.parse(item.time, datetimeFormatter)
            binding.shuttleTimeText.text = context.getString(
                R.string.shuttle_time_type_3,
                departureTime.hour.toString().padStart(2, '0'),
                departureTime.minute.toString().padStart(2, '0')
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle, parent, false)
        return ViewHolder(ItemShuttleBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (realtimeViaStops.isNotEmpty()) {
            holder.bind(realtimeViaStops[position])
        } else if (timetableViaStops.isNotEmpty()) {
            holder.bind(timetableViaStops[position])
        }
    }

    override fun getItemCount(): Int = if (realtimeViaStops.isNotEmpty()) realtimeViaStops.size else timetableViaStops.size

    private fun setStopText(stopID: String) : String {
        return when(stopID) {
            "dormitory_o", "dormitory_i" -> context.getString(R.string.shuttle_tab_dormitory_out)
            "shuttlecock_o", "shuttlecock_i" -> context.getString(R.string.shuttle_tab_shuttlecock_out)
            "station" -> context.getString(R.string.shuttle_tab_station)
            "terminal" -> context.getString(R.string.shuttle_tab_terminal)
            "jungang_stn" -> context.getString(R.string.shuttle_tab_jungang_station)
            else -> ""
        }
    }
}
