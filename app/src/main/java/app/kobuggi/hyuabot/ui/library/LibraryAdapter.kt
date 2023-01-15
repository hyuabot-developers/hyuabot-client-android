package app.kobuggi.hyuabot.ui.library

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.CardReadingRoomBinding
import app.kobuggi.hyuabot.model.library.ReadingRoomItemResponse
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging


class LibraryAdapter(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private var items: List<ReadingRoomItemResponse>) : RecyclerView.Adapter<LibraryAdapter.ReadingRoomViewHolder>() {
    private val roomNameStringID = mapOf(
        "제1열람실 (2F)" to R.string.room_name_1_2f,
        "제2열람실 (4F)" to R.string.room_name_2_4f,
        "집중열람실 (4F)" to R.string.room_name_3_4f,
        "노상일 HOLMZ (4F)" to R.string.holmz_room,
        "법학 제1열람실[3층]" to R.string.law_room_1_3f,
        "법학 제2열람실A[4층]" to R.string.law_room_2a_4f,
        "법학 제2열람실B[4층]" to R.string.law_room_2b_4f,
        "법학 대학원열람실[2층]" to R.string.law_room_graduated_2f,
        "제1열람실[지하1층]" to R.string.room_name_1_underground_1f,
        "제2열람실[지하1층]" to R.string.room_name_2_underground_1f,
        "제3열람실[3층]" to R.string.room_name_3_3f,
        "제4열람실[3층]" to R.string.room_name_4_3f,
        "HOLMZ 열람석" to R.string.holmz_room_seoul,
    )

    inner class ReadingRoomViewHolder(private val binding: CardReadingRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReadingRoomItemResponse) {
            binding.readingRoomName.text = if(roomNameStringID.containsKey(item.roomName)) {
                binding.root.context.getString(roomNameStringID[item.roomName]!!)
            } else {
                item.roomName
            }
            binding.currentSeat.text = item.availableSeat.toString()
            binding.totalSeat.text = item.activeSeat.toString()
            binding.readingRoomNotificationButton.text = if(sharedPreferences.getBoolean("reading_room_${item.roomID}", false)) {
                binding.root.context.getString(R.string.reading_room_notification_cancel)
            } else {
                binding.root.context.getString(R.string.reading_room_notification)
            }
            binding.readingRoomNotificationButton.setOnClickListener {
                if (binding.readingRoomNotificationButton.text == context.getString(R.string.reading_room_notification)) {
                    Firebase.messaging.subscribeToTopic("reading_room_${item.roomID}")
                        .addOnCompleteListener { task ->
                            var msg = context.getString(R.string.reading_room_notification_message_fail, binding.readingRoomName.text)
                            if (task.isSuccessful) {
                                msg = context.getString(R.string.reading_room_notification_message, binding.readingRoomName.text)
                                binding.readingRoomNotificationButton.text = context.getString(R.string.reading_room_notification_cancel)
                                sharedPreferences.edit().putBoolean("reading_room_${item.roomID}", true).apply()
                            }
                            Log.d("Reading Room Page", msg)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Firebase.messaging.unsubscribeFromTopic("reading_room_${item.roomID}")
                        .addOnCompleteListener { task ->
                            var msg = context.getString(R.string.reading_room_notification_cancel_message_fail, binding.readingRoomName.text)
                            if (task.isSuccessful) {
                                msg = context.getString(R.string.reading_room_notification_cancel_message, binding.readingRoomName.text)
                                binding.readingRoomNotificationButton.text = context.getString(R.string.reading_room_notification)
                                sharedPreferences.edit().putBoolean("reading_room_${item.roomID}", false).apply()
                            }
                            Log.d("Reading Room Page", msg)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_reading_room, parent, false)
        return ReadingRoomViewHolder(CardReadingRoomBinding.bind(view))
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setReadingRooms(items: List<ReadingRoomItemResponse>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ReadingRoomViewHolder, position: Int) {
        holder.bind(items[position])
    }
}