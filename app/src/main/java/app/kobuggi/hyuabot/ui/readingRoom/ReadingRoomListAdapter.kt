package app.kobuggi.hyuabot.ui.readingRoom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import app.kobuggi.hyuabot.databinding.ItemReadingRoomBinding

class ReadingRoomListAdapter(
    private val context: Context,
    private val onClick: (ReadingRoomPageQuery.ReadingRoom) -> Unit,
    private var rooms: List<ReadingRoomPageQuery.ReadingRoom>,
    private var notifications: Set<Int>
): RecyclerView.Adapter<ReadingRoomListAdapter.ViewHolder>() {
    inner class ViewHolder (private val binding: ItemReadingRoomBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(room: ReadingRoomPageQuery.ReadingRoom) {
            binding.apply {
                readingRoomName.text = room.name
                readingRoomSeatCount.text =
                    context.getString(R.string.reading_room_seat_format, room.available, room.active)
                readingRoomAlarmButton.apply {
                    isSelected = notifications.contains(room.id)
                    setOnClickListener { onClick(room) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reading_room, parent, false)
        return ViewHolder(ItemReadingRoomBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int = rooms.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<ReadingRoomPageQuery.ReadingRoom>) {
        rooms = newData
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNotifications(newNotifications: Set<Int>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
