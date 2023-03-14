package app.kobuggi.hyuabot.component.card.subway

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemSubwayHeadingBinding
import java.time.LocalTime

class RealtimeHeadingItemAdapter (
    private val context: Context,
    private val cardItem: CardItem,
    private val onClickTimetableButton: (String, String) -> Unit
) : RecyclerView.Adapter<RealtimeHeadingItemAdapter.ViewHolder>() {
    val now: LocalTime = LocalTime.now()
    inner class ViewHolder(private val binding: ItemSubwayHeadingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(headingItem: Int) {
            val resources = context.resources
            binding.subwayHeadingName.text = resources.getString(headingItem)
            if (headingItem == R.string.heading_up || headingItem == R.string.heading_down) {
                binding.subwayTimetableButton.setOnClickListener {
                    onClickTimetableButton(cardItem.stationID, if (headingItem == R.string.heading_up) "up" else "down")
                }
                val realtime = if (headingItem == R.string.heading_up) cardItem.realtimeList.up else cardItem.realtimeList.down
                val timetable = if (headingItem == R.string.heading_up) cardItem.timetableList.up else cardItem.timetableList.down
                val adapter = RealtimeItemAdapter(
                    context,
                    realtime,
                    timetable.filter {
                        val hour = it.departureTime.split(":")[0].toInt()
                        val minute = it.departureTime.split(":")[1].toInt()
                        val remainingTime = (hour - now.hour) * 60 + (minute - now.minute)
                        remainingTime > realtime.maxBy { realtimeItem -> realtimeItem.time }.time
                    }
                )
                if (adapter.itemCount > 0) {
                    binding.subwayArrivalList.adapter = adapter
                    binding.subwayArrivalList.layoutManager = LinearLayoutManager(context)
                    binding.subwayArrivalEmpty.visibility = View.GONE
                    binding.subwayArrivalList.visibility = View.VISIBLE
                } else {
                    binding.subwayArrivalEmpty.visibility = View.VISIBLE
                    binding.subwayArrivalList.visibility = View.GONE
                }
            } else if (headingItem == R.string.heading_incheon) {
                binding.subwayTimetableButton.visibility = View.INVISIBLE
                val transferList = arrayListOf<TransferItem>()
                cardItem.realtimeList.down.forEach {
                    val transfer = cardItem.transferTimetableList.down.find { transferItem -> run {
                        val hour = transferItem.departureTime.split(":")[0].toInt()
                        val minute = transferItem.departureTime.split(":")[1].toInt()
                        val remainingTime = (hour - now.hour) * 60 + (minute - now.minute)
                        remainingTime > it.time
                    }}
                    if (transfer != null && transfer.startStation == "오이도") {
                        transferList.add(TransferItem(it, R.string.line_no_4, transfer, R.string.line_suinbundang))
                    }
                }
                cardItem.transferRealtimeList.down.forEach {
                    if (it.terminalStation == "인천") {
                        transferList.add(TransferItem(it, R.string.line_suinbundang))
                    }
                }
                val adapter = RealtimeItemAdapter(context, listOf(), listOf(), transferList.sortedBy { it.from.time })
                if (adapter.itemCount > 0) {
                    binding.subwayArrivalList.adapter = adapter
                    binding.subwayArrivalList.layoutManager = LinearLayoutManager(context)
                    binding.subwayArrivalEmpty.visibility = View.GONE
                    binding.subwayArrivalList.visibility = View.VISIBLE
                } else {
                    binding.subwayArrivalEmpty.visibility = View.VISIBLE
                    binding.subwayArrivalList.visibility = View.GONE
                }
            } else if (headingItem == R.string.heading_ansan) {
                binding.subwayTimetableButton.visibility = View.INVISIBLE
                val transferList = arrayListOf<TransferItem>()
                cardItem.transferRealtimeList.up.forEach {
                    if (it.terminalStation == "오이도") {
                        val transfer = cardItem.timetableList.up.find { transferItem -> run {
                            val hour = transferItem.departureTime.split(":")[0].toInt()
                            val minute = transferItem.departureTime.split(":")[1].toInt()
                            val remainingTime = (hour - now.hour) * 60 + (minute - now.minute)
                            remainingTime > it.time
                        }}
                        if (transfer != null) {
                            transferList.add(TransferItem(it, R.string.line_suinbundang, transfer, R.string.line_no_4))
                        }
                    } else {
                        transferList.add(TransferItem(it, R.string.line_suinbundang))
                    }
                }
                val adapter = RealtimeItemAdapter(context, listOf(), listOf(), transferList.sortedBy { it.from.time })
                if (adapter.itemCount > 0) {
                    binding.subwayArrivalList.adapter = adapter
                    binding.subwayArrivalList.layoutManager = LinearLayoutManager(context)
                    binding.subwayArrivalEmpty.visibility = View.GONE
                    binding.subwayArrivalList.visibility = View.VISIBLE
                } else {
                    binding.subwayArrivalEmpty.visibility = View.VISIBLE
                    binding.subwayArrivalList.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_heading, parent, false)
        return ViewHolder(ItemSubwayHeadingBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cardItem.headingList.filter {
            it == R.string.heading_up ||
                    it == R.string.heading_down ||
                    it == R.string.heading_incheon ||
                    it == R.string.heading_ansan
        }[position])
    }

    override fun getItemCount(): Int = cardItem.headingList.filter {
        it == R.string.heading_up ||
        it == R.string.heading_down ||
        it == R.string.heading_incheon ||
        it == R.string.heading_ansan
    }.size
}