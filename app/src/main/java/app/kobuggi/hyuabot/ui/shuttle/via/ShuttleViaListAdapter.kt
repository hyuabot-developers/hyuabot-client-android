package app.kobuggi.hyuabot.ui.shuttle.via

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleTimetablePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleBinding

class ShuttleViaListAdapter(
    private val context: Context,
    private val stopsOfTimetableByOrder: List<ShuttleRealtimePageQuery.Stop1> = emptyList(),
    private val stopsOfTimetableByDestination: List<ShuttleRealtimePageQuery.Stop2> = emptyList(),
    private val stopsOfEntireTimetable: List<ShuttleTimetablePageQuery.Stop1> = emptyList(),
) : RecyclerView.Adapter<ShuttleViaListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemShuttleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShuttleRealtimePageQuery.Stop1) {
            binding.shuttleTypeText.text = setStopText(item.stop)
            // Departure time
            binding.shuttleTimeText.text = context.getString(
                R.string.shuttle_time_type_3,
                item.time.hour.toString().padStart(2, '0'),
                item.time.minute.toString().padStart(2, '0')
            )
        }

        fun bind(item: ShuttleRealtimePageQuery.Stop2) {
            binding.shuttleTypeText.text = setStopText(item.stop)
            // Departure time
            binding.shuttleTimeText.text = context.getString(
                R.string.shuttle_time_type_3,
                item.time.hour.toString().padStart(2, '0'),
                item.time.minute.toString().padStart(2, '0')
            )
        }

        fun bind(item: ShuttleTimetablePageQuery.Stop1) {
            binding.shuttleTypeText.text = setStopText(item.stop)
            // Departure time
            binding.shuttleTimeText.text = context.getString(
                R.string.shuttle_time_type_3,
                item.time.hour.toString().padStart(2, '0'),
                item.time.minute.toString().padStart(2, '0')
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle, parent, false)
        return ViewHolder(ItemShuttleBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (stopsOfTimetableByOrder.isNotEmpty()) {
            holder.bind(stopsOfTimetableByOrder[position])
        } else if (stopsOfTimetableByDestination.isNotEmpty()) {
            holder.bind(stopsOfTimetableByDestination[position])
        } else if (stopsOfEntireTimetable.isNotEmpty()) {
            holder.bind(stopsOfEntireTimetable[position])
        }
    }

    override fun getItemCount(): Int = when {
        stopsOfTimetableByOrder.isNotEmpty() -> stopsOfTimetableByOrder.size
        stopsOfTimetableByDestination.isNotEmpty() -> stopsOfTimetableByDestination.size
        stopsOfEntireTimetable.isNotEmpty() -> stopsOfEntireTimetable.size
        else -> 0
    }

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
