package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.databinding.ItemShuttleRouteBinding

class ShuttleRouteAdapter(
    private val items: List<ShuttleRouteItemView.Route>,
    private val names: List<Int>
): RecyclerView.Adapter<ShuttleRouteAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemShuttleRouteBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(route: ShuttleRouteItemView.Route, nameResId: Int) {
            binding.routeItemView.bind(route)
            binding.routeNameText.text = binding.root.context.getString(nameResId).replace(" ", "\n")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShuttleRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], names[position])
    }

    override fun getItemCount(): Int = items.size
}
