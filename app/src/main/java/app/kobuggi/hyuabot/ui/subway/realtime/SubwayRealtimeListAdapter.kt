package app.kobuggi.hyuabot.ui.subway.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemSubwayRealtimeBinding
import kotlin.math.min

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
            if (upRealtimeItem != null) {
                binding.apply {
                    if (upRealtimeItem.last) {
                        subwayDestinationText.apply {
                            text = context.getString(
                                R.string.subway_realtime_destination_format_last,
                                getTerminalString(upRealtimeItem.terminal.id),
                            )
                            setTextColor(context.getColor(android.R.color.holo_red_light))
                        }
                    } else {
                        subwayDestinationText.text = context.getString(
                            R.string.subway_realtime_destination_format,
                            getTerminalString(upRealtimeItem.terminal.id),
                        )
                    }
                    subwayTimeText.text = context.getString(
                        R.string.subway_realtime_format,
                        upRealtimeItem.time.toInt(),
                        upRealtimeItem.stop
                    )
                }
            }
            else if (downRealtimeItem != null) {
                binding.apply {
                    if (downRealtimeItem.last) {
                        subwayDestinationText.apply {
                            text = context.getString(
                                R.string.subway_realtime_destination_format_last,
                                getTerminalString(downRealtimeItem.terminal.id),
                            )
                            setTextColor(context.getColor(android.R.color.holo_red_light))
                            }
                    } else {
                        subwayDestinationText.text = context.getString(
                            R.string.subway_realtime_destination_format,
                            getTerminalString(downRealtimeItem.terminal.id),
                        )
                    }
                    subwayTimeText.text = context.getString(
                        R.string.subway_realtime_format,
                        downRealtimeItem.time.toInt(),
                        downRealtimeItem.stop
                    )
                }
            }
            else if (upTimetableItem != null) {
                binding.apply {
                    subwayDestinationText.text = context.getString(
                        R.string.subway_realtime_destination_format,
                        getTerminalString(upTimetableItem.terminal.id),
                    )
                    subwayTimeText.text = context.getString(
                        R.string.subway_realtime_timetable_format,
                        upTimetableItem.time.substring(0, 2),
                        upTimetableItem.time.substring(3, 5),
                    )
                }
            }
            else if (downTimetableItem != null) {
                binding.apply {
                    subwayDestinationText.text = context.getString(
                        R.string.subway_realtime_destination_format,
                        getTerminalString(downTimetableItem.terminal.id),
                    )
                    subwayTimeText.text = context.getString(
                        R.string.subway_realtime_timetable_format,
                        downTimetableItem.time.substring(0, 2),
                        downTimetableItem.time.substring(3, 5),
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
            min(upRealtimeList!!.size + upTimetableList!!.size, 5)
        } else if (downRealtimeList != null && downTimetableList != null) {
            min(downRealtimeList!!.size + downTimetableList!!.size, 5)
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
