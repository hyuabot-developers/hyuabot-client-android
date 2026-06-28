package app.kobuggi.hyuabot.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemBuildingSearchBinding
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator

class BuildingSearchAdapter(
    private val context: Context,
    private val onClick: (RoomItem) -> Unit,
    private var rooms: List<RoomItem>
): RecyclerView.Adapter<BuildingSearchAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemBuildingSearchBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(room: RoomItem) {
            binding.apply {
                itemBuildingSearch.setOnClickListener { onClick(room) }
                itemBuildingSearch.tag = room
                DynamicTextTranslator.bind(roomName, room.name)
                roomDescription.text =
                    context.getString(R.string.room_description_format, room.name, room.number)
                DynamicTextTranslator.translateText(context.resources, room.name) { translatedName ->
                    if (itemBuildingSearch.tag == room) {
                        roomDescription.text = context.getString(R.string.room_description_format, translatedName, room.number)
                    }
                }
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
    fun updateData(newData: List<RoomItem>) {
        rooms = newData
        notifyDataSetChanged()
    }
}
