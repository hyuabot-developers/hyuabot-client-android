package app.kobuggi.hyuabot.component.card.bus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBusRouteBinding
import app.kobuggi.hyuabot.ui.bus.realtime.RealtimeRouteItem

class RealtimeRouteItemAdapter (
    private val context: Context,
    private var routeList: List<RealtimeRouteItem>,
    private val onClickTimetableButton: (Int, String, Int) -> Unit
) : RecyclerView.Adapter<RealtimeRouteItemAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBusRouteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(routeItem: RealtimeRouteItem) {
            val resources = context.resources
            if (routeItem.stopID > 0) {
                binding.busRouteName.text = resources.getString(R.string.bus_route_name, routeItem.routeName, resources.getString(routeItem.stopID))
            } else if (routeItem.routeName.isNotEmpty()) {
                binding.busRouteName.text = resources.getString(R.string.bus_route_name_without_stop, routeItem.routeName)
                binding.busTimetableButton.visibility = View.INVISIBLE
            }
            val adapter = RealtimeItemAdapter(
                context,
                routeItem.stopID == 0,
                routeItem.realtime,
                routeItem.timetable,
                if (routeItem.routeName.startsWith("31")) 3 else 6
            )
            if (adapter.itemCount > 0) {
                binding.busArrivalList.adapter = adapter
                binding.busArrivalList.layoutManager = LinearLayoutManager(context)
                binding.busArrivalList.visibility = View.VISIBLE
                binding.busArrivalEmpty.visibility = View.GONE
            } else {
                binding.busArrivalEmpty.visibility = View.VISIBLE
                binding.busArrivalList.visibility = View.GONE
            }
            binding.busTimetableButton.setOnClickListener { onClickTimetableButton(routeItem.routeID, routeItem.routeName, routeItem.startStopID) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_route, parent, false)
        return ViewHolder(ItemBusRouteBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(routeList[position])
    }

    override fun getItemCount(): Int = routeList.size
}