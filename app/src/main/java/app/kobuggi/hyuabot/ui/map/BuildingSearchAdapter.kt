package app.kobuggi.hyuabot.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.MapPageSearchQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBuildingSearchBinding

class BuildingSearchAdapter(private val context: Context, private var rooms: List<MapPageSearchQuery.Room>): RecyclerView.Adapter<BuildingSearchAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBuildingSearchBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(room: MapPageSearchQuery.Room) {
            binding.apply {
                roomName.text = room.name
                roomDescription.text =
                    context.getString(R.string.room_description_format, room.buildingName, room.number)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_building_search, parent, false)
        return ViewHolder(ItemBuildingSearchBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int = rooms.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<MapPageSearchQuery.Room>) {
        rooms = newData
        notifyDataSetChanged()
    }
}
