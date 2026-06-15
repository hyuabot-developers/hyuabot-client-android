package app.kobuggi.hyuabot.ui.bus.realtime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BusStopDialogQuery
import app.kobuggi.hyuabot.R

class BusFirstLastAdapter(
    private val items: List<BusStopDialogQuery.Bus>
) : RecyclerView.Adapter<BusFirstLastAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routeName: TextView = itemView.findViewById(R.id.bus_first_last_route_name)
        val upTimes: TextView = itemView.findViewById(R.id.bus_first_last_up)
        val downTimes: TextView = itemView.findViewById(R.id.bus_first_last_down)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_first_last, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context
        holder.routeName.text = item.route.name
        val color = if (item.route.name == "10-1" || item.route.name == "50") {
            ContextCompat.getColor(ctx, R.color.green_bus)
        } else {
            ContextCompat.getColor(ctx, R.color.red_bus)
        }
        holder.routeName.setTextColor(color)
        val up = item.route.runningTime.up
        val down = item.route.runningTime.down
        holder.upTimes.text = ctx.getString(R.string.bus_first_last_up, up.first ?: "-", up.last ?: "-")
        holder.downTimes.text = ctx.getString(R.string.bus_first_last_down, down.first ?: "-", down.last ?: "-")
    }

    override fun getItemCount() = items.size
}
