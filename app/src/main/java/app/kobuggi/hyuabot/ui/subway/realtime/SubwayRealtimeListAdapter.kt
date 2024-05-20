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
    private var upRealtimeList: List<SubwayRealtimePageQuery.Up>?,
    private var downRealtimeList: List<SubwayRealtimePageQuery.Down>?,
    private var upTimetableList: List<SubwayRealtimePageQuery.Up1>?,
    private var downTimetableList: List<SubwayRealtimePageQuery.Down1>?,
) : RecyclerView.Adapter<SubwayRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            upRealtimeItem: SubwayRealtimePageQuery.Up?,
            downRealtimeItem: SubwayRealtimePageQuery.Down?,
            upTimetableItem: SubwayRealtimePageQuery.Up1?,
            downTimetableItem: SubwayRealtimePageQuery.Down1?,
        ) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_realtime, parent, false)
        return ViewHolder(ItemSubwayRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (upRealtimeList != null && upTimetableList != null){
            if (position < upRealtimeList!!.size) {
                holder.bind(upRealtimeList!![position], null, null, null)
            } else {
                holder.bind(null, null, upTimetableList!![position - upRealtimeList!!.size], null)
            }
        } else if (downRealtimeList != null && downTimetableList != null) {
            if (position < downRealtimeList!!.size) {
                holder.bind(null, downRealtimeList!![position], null, null)
            } else {
                holder.bind(null, null, null, downTimetableList!![position - downRealtimeList!!.size])
            }
        }
    }

    override fun getItemCount(): Int {
        return if (upRealtimeList != null && upTimetableList != null) {
            upRealtimeList!!.size + upTimetableList!!.size
        } else if (downRealtimeList != null && downTimetableList != null) {
            downRealtimeList!!.size + downTimetableList!!.size
        } else {
            0
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(
        newUpRealtimeList: List<SubwayRealtimePageQuery.Up>?,
        newDownRealtimeList: List<SubwayRealtimePageQuery.Down>?,
        newUpTimetableList: List<SubwayRealtimePageQuery.Up1>?,
        newDownTimetableList: List<SubwayRealtimePageQuery.Down1>?,
    ) {
        upRealtimeList = newUpRealtimeList
        downRealtimeList = newDownRealtimeList
        upTimetableList = newUpTimetableList
        downTimetableList = newDownTimetableList
        notifyDataSetChanged()
    }
}
