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
                subwayDestinationText.text = timetable.terminal.name
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

}
