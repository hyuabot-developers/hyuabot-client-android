package app.kobuggi.hyuabot.ui.subway.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.SubwayTimetablePageDownQuery
import app.kobuggi.hyuabot.SubwayTimetablePageUpQuery
import app.kobuggi.hyuabot.databinding.ItemSubwayTimetableBinding

class SubwayTimetableListAdapter(
    private val context: Context,
    private var upTimetableList: List<SubwayTimetablePageUpQuery.Up>?,
    private var downTimetableList: List<SubwayTimetablePageDownQuery.Down>?
) : RecyclerView.Adapter<SubwayTimetableListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayTimetableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(upTimetableItem: SubwayTimetablePageUpQuery.Up?, downTimetableItem: SubwayTimetablePageDownQuery.Down?) {
            if (upTimetableItem != null) {
                binding.apply {
                    subwayDestinationText.text = getTerminalString(upTimetableItem.terminal.id)
                    subwayTimeText.text = upTimetableItem.time
                }
            } else if (downTimetableItem != null) {
                binding.apply {
                    subwayDestinationText.text = getTerminalString(downTimetableItem.terminal.id)
                    subwayTimeText.text = downTimetableItem.time
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_timetable, parent, false)
        return ViewHolder(ItemSubwayTimetableBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (upTimetableList != null) {
            holder.bind(upTimetableList!![position], null)
        } else {
            holder.bind(null, downTimetableList!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (upTimetableList != null) {
            upTimetableList!!.size
        } else {
            downTimetableList!!.size
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(
        newUpTimetableList: List<SubwayTimetablePageUpQuery.Up>?,
        newDownTimetableList: List<SubwayTimetablePageDownQuery.Down>?
    ) {
        upTimetableList = newUpTimetableList
        downTimetableList = newDownTimetableList
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
