package app.kobuggi.hyuabot.ui.readingRoom
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import app.kobuggi.hyuabot.databinding.ItemReadingRoomBinding
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator

class ReadingRoomListAdapter(
    private val context: Context,
    private val onClick: (ReadingRoomPageQuery.ReadingRoom, Boolean) -> Unit,
    private var rooms: List<ReadingRoomPageQuery.ReadingRoom>,
    private var notifications: Set<Int>
): RecyclerView.Adapter<ReadingRoomListAdapter.ViewHolder>() {
    inner class ViewHolder (private val binding: ItemReadingRoomBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(room: ReadingRoomPageQuery.ReadingRoom) {
            binding.apply {
                DynamicTextTranslator.bind(readingRoomName, room.name)
                readingRoomSeatCount.text =
                    context.getString(R.string.reading_room_seat_format, room.seats.available, room.seats.active)
                readingRoomAlarmButton.apply {
                    isSelected = notifications.contains(room.seq)
                    setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.READING_ROOM_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM); onClick(room, !isSelected) }
                }
                val progress = if (room.seats.active > 0) (room.seats.occupied * 100) / room.seats.active else 0
                readingRoomProgress.progress = progress
                val indicatorColor = when {
                    progress >= 90 -> context.getColor(R.color.red_bus)
                    progress >= 70 -> context.getColor(android.R.color.holo_orange_light)
                    else -> context.getColor(R.color.green_bus)
                }
                readingRoomProgress.setIndicatorColor(indicatorColor)
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
