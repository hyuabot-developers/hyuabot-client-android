package app.kobuggi.hyuabot.ui.subway.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemSubwayTimetableBinding
import app.kobuggi.hyuabot.model.subway.SubwayTimetableItemResponse
import app.kobuggi.hyuabot.util.Subway
import java.time.LocalTime

class TimetableItemAdapter (private val context: Context, private var timetable: List<SubwayTimetableItemResponse>) : RecyclerView.Adapter<TimetableItemAdapter.ViewHolder>() {
    private var now = LocalTime.now()
    inner class ViewHolder(private val binding: ItemSubwayTimetableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timetableItem: SubwayTimetableItemResponse) {
            val resources = context.resources
            binding.subwayTimetableItemTerminal.text = resources.getString(R.string.subway_realtime_terminal, Subway.getSubwayStationName(context, timetableItem.terminalStation))
            binding.subwayTimetableItemTime.text = resources.getString(R.string.subway_timetable_item, timetableItem.departureTime.split(":")[0], timetableItem.departureTime.split(":")[1])
            if (timetableItem.departureTime.split(":")[0].toInt() < now.hour) {
                binding.subwayTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
            } else if (timetableItem.departureTime.split(":")[0].toInt() == now.hour) {
                if (timetableItem.departureTime.split(":")[1].toInt() < now.minute) {
                    binding.subwayTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
                } else {
                    binding.subwayTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
                }
            } else {
                binding.subwayTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_timetable, parent, false)
        return ViewHolder(ItemSubwayTimetableBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timetable[position])
    }

    override fun getItemCount(): Int = timetable.size

    @SuppressLint("NotifyDataSetChanged")
    fun setTimeTable(timetable: List<SubwayTimetableItemResponse>) {
        now = LocalTime.now()
        this.timetable = timetable.sortedBy { it.departureTime }
        notifyDataSetChanged()
    }
}