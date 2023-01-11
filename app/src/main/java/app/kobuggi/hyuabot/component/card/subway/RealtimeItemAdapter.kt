package app.kobuggi.hyuabot.component.card.subway

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemSubwayRealtimeBinding
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeItemResponse
import app.kobuggi.hyuabot.model.subway.SubwayTimetableItemResponse

class RealtimeItemAdapter(
    private val context: Context,
    private val realtimeList: List<SubwayRealtimeItemResponse>,
    private val timetableList: List<SubwayTimetableItemResponse>,
    private val transferList: List<TransferItem>? = null,
    private val count: Int = 8
) : RecyclerView.Adapter<RealtimeItemAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(realtimeItem: SubwayRealtimeItemResponse) {
            val resources = context.resources
            binding.subwayRealtimeItemTerminal.text = resources.getString(R.string.subway_realtime_terminal, realtimeItem.terminalStation)
            binding.subwayRealtimeItemTime.text = resources.getString(R.string.subway_realtime_arrival, realtimeItem.time, realtimeItem.location)
        }

        fun bind(timetableItem: SubwayTimetableItemResponse) {
            val resources = context.resources
            binding.subwayRealtimeItemTerminal.text = resources.getString(R.string.subway_realtime_terminal, timetableItem.terminalStation)
            binding.subwayRealtimeItemTime.text = resources.getString(R.string.subway_timetable_arrival, timetableItem.departureTime.split(":")[0], timetableItem.departureTime.split(":")[1])
            binding.subwayRealtimeItemTerminal.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
            binding.subwayRealtimeItemTime.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
        }

        fun bind(transferItem: TransferItem) {
            val resources = context.resources
            binding.subwayRealtimeItemTerminal.text = resources.getString(R.string.transfer_from, transferItem.from.location, resources.getString(transferItem.fromID))
            if (transferItem.to != null) {
                binding.subwayRealtimeItemTime.text = resources.getString(R.string.transfer_to, transferItem.to.departureTime.substring(0, 5), resources.getString(transferItem.toID!!))
                binding.subwayRealtimeItemTime.layoutParams =
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                binding.subwayRealtimeItemTerminal.layoutParams =
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            } else {
                binding.subwayRealtimeItemTime.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_realtime, parent, false)
        return ViewHolder(ItemSubwayRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (transferList != null) {
            holder.bind(transferList[position])
        } else {
            if (position < realtimeList.size) {
                holder.bind(realtimeList[position])
            } else {
                holder.bind(timetableList[position - realtimeList.size])
            }
        }
    }

    override fun getItemCount(): Int {
        return if (transferList != null) {
            if (transferList.size > count) count else transferList.size
        } else {
            if (realtimeList.size + timetableList.size > count) count else realtimeList.size + timetableList.size
        }
    }

}