package app.kobuggi.hyuabot.ui.bus.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusTimetableBinding
import java.time.LocalTime

class TimetableItemAdapter (private val context: Context, private var timetable: List<String>) : RecyclerView.Adapter<TimetableItemAdapter.ViewHolder>() {
    private var now = LocalTime.now()
    inner class ViewHolder(private val binding: ItemBusTimetableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timetableItem: String) {
            val resources = context.resources
            binding.busTimetableItemTime.text = resources.getString(R.string.bus_timetable_item,
                timetableItem.split(":")[0],
                timetableItem.split(":")[1],
            )
            if (timetableItem.split(":")[0].toInt() < now.hour) {
                binding.busTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
            } else if (timetableItem.split(":")[0].toInt() == now.hour) {
                if (timetableItem.split(":")[1].toInt() < now.minute) {
                    binding.busTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
                } else {
                    binding.busTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
                }
            } else {
                binding.busTimetableItemTime.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_timetable, parent, false)
        return ViewHolder(ItemBusTimetableBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timetable[position])
    }

    override fun getItemCount(): Int = timetable.size

    @SuppressLint("NotifyDataSetChanged")
    fun setTimeTable(timetable: List<String>) {
        now = LocalTime.now()
        this.timetable = timetable
        notifyDataSetChanged()
    }
}