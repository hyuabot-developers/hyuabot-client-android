package app.kobuggi.hyuabot.component.card.shuttle

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemShuttleListBinding
import app.kobuggi.hyuabot.model.shuttle.ArrivalItem
import app.kobuggi.hyuabot.model.shuttle.Destination

class RealtimeStopListAdapter(
    private val context: Context,
    private val stopID: Int,
    private val destinationList: HashMap<Destination, List<ArrivalItem>>,
    private val onClickTimetableButton: (Int, Int) -> Unit
) : RecyclerView.Adapter<RealtimeStopListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemShuttleListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(destination: Destination, arrivalList: List<ArrivalItem>) {
            val resources = context.resources
            val destinationID = when (destination) {
                Destination.STATION -> R.string.shuttle_bound_for_station
                Destination.CAMPUS -> R.string.shuttle_bound_for_campus
                Destination.DORMITORY -> R.string.shuttle_bound_for_dormitory
                Destination.JUNGANG_STN -> R.string.shuttle_bound_for_jungang_stn
                Destination.TERMINAL -> R.string.shuttle_bound_for_terminal
            }
            binding.shuttleHeading.text = resources.getString(destinationID)
            binding.shuttleTimetableButton.setOnClickListener {
                onClickTimetableButton(stopID, destinationID)
            }
            if (arrivalList.isNotEmpty()) {
                val adapter = RealtimeItemAdapter(context, destination, arrivalList.sortedBy { it.time }, if (itemCount == 1) 11 else 3)
                binding.shuttleRealtimeListView.adapter = adapter
                binding.shuttleRealtimeListView.layoutManager = LinearLayoutManager(context)
                binding.shuttleRealtimeListView.visibility = RecyclerView.VISIBLE
                binding.shuttleRealtimeNoData.visibility = RecyclerView.GONE
            } else {
                binding.shuttleRealtimeListView.visibility = RecyclerView.GONE
                binding.shuttleRealtimeNoData.visibility = RecyclerView.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle_list, parent, false)
        return ViewHolder(ItemShuttleListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(destinationList.keys.toList()[position], destinationList.values.toList()[position])
    }

    override fun getItemCount(): Int = destinationList.size
}