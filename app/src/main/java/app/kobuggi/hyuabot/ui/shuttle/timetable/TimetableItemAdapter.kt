package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemShuttleTimetableBinding
import java.time.LocalTime

class TimetableItemAdapter (
    private val context: Context,
    private val stopID: Int,
    private var timetable: List<TimetableItem>) : RecyclerView.Adapter<TimetableItemAdapter.ViewHolder>() {
    private var now = LocalTime.now()
    inner class ViewHolder(private val binding: ItemShuttleTimetableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timetableItem: TimetableItem) {
            val resources = context.resources
            if (stopID == R.string.shuttlecock_i) {
                binding.shuttleTimetableItemRoute.text = resources.getString(R.string.D)
                binding.shuttleTimetableItemRoute.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
            } else {
                when (timetableItem.routeTag) {
                    "DH", "DY", "DJ" -> {
                        binding.shuttleTimetableItemRoute.text = resources.getString(R.string.D)
                        binding.shuttleTimetableItemRoute.setTextColor(ResourcesCompat.getColor(resources, android.R.color.holo_red_dark, null))
                    }
                    "C" -> {
                        binding.shuttleTimetableItemRoute.text = resources.getString(R.string.C)
                        binding.shuttleTimetableItemRoute.setTextColor(ResourcesCompat.getColor(resources, android.R.color.holo_blue_dark, null))
                    }
                }
            }
            binding.shuttleTimetableItemTime.text = resources.getString(R.string.shuttle_timetable_item,
                timetableItem.departureTime.split(":")[0],
                timetableItem.departureTime.split(":")[1],
            )
            if (timetableItem.departureTime.split(":")[0].toInt() < now.hour) {
                binding.shuttleTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
            } else if (timetableItem.departureTime.split(":")[0].toInt() == now.hour) {
                if (timetableItem.departureTime.split(":")[1].toInt() < now.minute) {
                    binding.shuttleTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
                } else {
                    binding.shuttleTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
                }
            } else {
                binding.shuttleTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle_timetable, parent, false)
        return ViewHolder(ItemShuttleTimetableBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timetable[position])
    }

    override fun getItemCount(): Int = timetable.size

    @SuppressLint("NotifyDataSetChanged")
    fun setTimeTable(timetable: List<TimetableItem>) {
        now = LocalTime.now()
        this.timetable = timetable
        notifyDataSetChanged()
    }
}