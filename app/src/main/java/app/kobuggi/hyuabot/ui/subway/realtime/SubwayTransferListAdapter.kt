package app.kobuggi.hyuabot.ui.subway.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemSubwayTransferBinding

class SubwayTransferListAdapter(
    private val context: Context,
    private val heading: String,
    private var arrivals: List<SubwayTransferItem> = emptyList(),
) : RecyclerView.Adapter<SubwayTransferListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayTransferBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(transfer: SubwayTransferItem) {
            binding.transferTargetText.isSelected = true
            if (transfer.transfer == null) {
                if (heading == "up") {
                    binding.transferTargetText.text = context.getString(
                        R.string.subway_transfer_up_item_format_no_transfer,
                        getTerminalString(transfer.take.terminal.stationID),
                        transfer.take.location ?: '-'
                    )
                } else {
                    binding.transferTargetText.text = context.getString(
                        R.string.subway_transfer_down_item_format_no_transfer,
                        getTerminalString(transfer.take.terminal.stationID),
                        transfer.take.minutes,
                        transfer.take.location ?: '-'
                    )
                }
            } else {
                if (heading == "up") {
                    binding.transferTargetText.text = context.getString(
                        R.string.subway_transfer_up_item_format,
                        getTerminalString(transfer.take.terminal.stationID),
                        transfer.take.location ?: '-',
                        getTerminalString(transfer.transfer.terminal.stationID),
                        transfer.transfer.minutes
                    )
                } else {
                    binding.transferTargetText.text = context.getString(
                        R.string.subway_transfer_down_item_format,
                        getTerminalString(transfer.take.terminal.stationID),
                        transfer.take.minutes,
                        transfer.take.location ?: '-',
                        getTerminalString(transfer.transfer.terminal.stationID),
                        transfer.transfer.minutes
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subway_transfer, parent, false)
        return ViewHolder(ItemSubwayTransferBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrivals[position])
    }

    override fun getItemCount(): Int = arrivals.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newArrivals: List<SubwayTransferItem>) {
        arrivals = newArrivals
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
