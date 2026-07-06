package app.kobuggi.hyuabot.ui.subway.realtime

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemSubwayTransferBinding

class SubwayTransferListAdapter(
    private val context: Context,
    private val heading: String,
    private var arrivals: List<SubwayTransferItem> = emptyList(),
) : RecyclerView.Adapter<SubwayTransferListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemSubwayTransferBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transfer: SubwayTransferItem) {
            binding.firstDestinationText.text = context.getString(
                R.string.subway_transfer_destination_format,
                getTerminalString(transfer.take.terminal.stationID)
            )
            binding.firstTimeText.text = minutesAfter(transfer.take.minutes)
            binding.firstMetaText.text = if (transfer.transfer == null) {
                context.getString(R.string.subway_transfer_current_location_format, transfer.take.location ?: '-')
            } else {
                context.getString(R.string.subway_transfer_board_at_hanyang)
            }
            binding.firstLineIndicator.backgroundTintList = ColorStateList.valueOf(getLineColor(transfer.take.terminal.stationID))

            val secondLeg = transfer.transfer
            if (secondLeg == null) {
                binding.transferRow.visibility = View.GONE
                binding.secondLegRow.visibility = View.GONE
                return
            }

            binding.transferRow.visibility = View.VISIBLE
            binding.secondLegRow.visibility = View.VISIBLE
            binding.transferStationText.text = context.getString(
                R.string.subway_transfer_station_format,
                getTransferStationString()
            )
            binding.transferWaitText.text = context.resources.getQuantityString(
                R.plurals.subway_transfer_wait_time_format,
                secondLeg.minutes - transfer.take.minutes,
                secondLeg.minutes - transfer.take.minutes
            )
            binding.secondDestinationText.text = context.getString(
                R.string.subway_transfer_destination_format,
                getTerminalString(secondLeg.terminal.stationID)
            )
            binding.secondTimeText.text = minutesAfter(secondLeg.minutes)
            binding.secondLineIndicator.backgroundTintList = ColorStateList.valueOf(getLineColor(secondLeg.terminal.stationID))
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
            "S07" -> context.getString(R.string.subway_station_S07)
            "S11" -> context.getString(R.string.subway_station_S11)
            "S16" -> context.getString(R.string.subway_station_S16)
            else -> terminal
        }
    }

    private fun getTransferStationString(): String {
        return if (heading == "choji") {
            context.getString(R.string.subway_station_choji)
        } else {
            context.getString(R.string.subway_station_K258)
        }
    }

    private fun minutesAfter(minutes: Int): String {
        return context.resources.getQuantityString(
            R.plurals.subway_transfer_minutes_after_format,
            minutes,
            minutes
        )
    }

    private fun getLineColor(stationID: String): Int {
        val colorRes = when {
            stationID.startsWith("K4") -> R.color.subway_line4
            stationID.startsWith("S") -> R.color.subway_seohae
            stationID.startsWith("K2") -> R.color.home_subway_yellow
            else -> R.color.hanyang_blue
        }
        return ContextCompat.getColor(context, colorRes)
    }
}
