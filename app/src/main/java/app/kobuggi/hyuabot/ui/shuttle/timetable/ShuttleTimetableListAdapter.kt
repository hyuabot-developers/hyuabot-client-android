package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleBinding

class ShuttleTimetableListAdapter(
    private val context: Context,
    private val shuttleTimetableViewModel: ShuttleTimetableViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val stopID: Int,
    private val headerID: Int,
    private var shuttleList: List<ShuttleRealtimePageQuery.Timetable>,
) : RecyclerView.Adapter<ShuttleTimetableListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemShuttleBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: ShuttleRealtimePageQuery.Timetable) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle, parent, false)
        return ViewHolder(ItemShuttleBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shuttleList[position])
    }

    override fun getItemCount(): Int = shuttleList.size

    fun updateData(newData: List<ShuttleRealtimePageQuery.Timetable>) {
        if (shuttleList.size > newData.size) {
            shuttleList = newData
            notifyItemRangeChanged(0, shuttleList.size)
            notifyItemRangeInserted(shuttleList.size, newData.size - shuttleList.size)
        } else if (shuttleList.size < newData.size) {
            shuttleList = newData
            notifyItemRangeChanged(0, newData.size)
            notifyItemRangeRemoved(newData.size, shuttleList.size - newData.size)
        } else {
            shuttleList = newData
            notifyItemRangeChanged(0, shuttleList.size)
        }
    }
}
