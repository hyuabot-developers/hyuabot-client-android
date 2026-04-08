package app.kobuggi.hyuabot.ui.subway.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemSubwayTimetableBinding

class SubwayTimetableListAdapter(
    private val context: Context,
    private var timetableList: List<SubwayTimetableItem> = emptyList(),
) : RecyclerView.Adapter<SubwayTimetableListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayTimetableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timetable: SubwayTimetableItem) {
            binding.apply {
                subwayDestinationText.text = getTerminalString(timetable.terminal.stationID)
                subwayTimeText.text = context.getString(
                    R.string.subway_timetable_time_format,
                    timetable.time.substring(0, 2),
                    timetable.time.substring(3, 5)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_timetable, parent, false)
        return ViewHolder(ItemSubwayTimetableBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timetableList[position])
    }

    override fun getItemCount(): Int = timetableList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newTimetableList: List<SubwayTimetableItem>) {
        timetableList = newTimetableList
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
