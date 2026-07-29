package app.kobuggi.hyuabot.ui.inquiry

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.databinding.ItemInquiryMessageAdminBinding
import app.kobuggi.hyuabot.databinding.ItemInquiryMessageSystemBinding
import app.kobuggi.hyuabot.databinding.ItemInquiryMessageUserBinding
import app.kobuggi.hyuabot.service.InquiryMessage
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class InquiryMessageAdapter(
    private var messages: List<InquiryMessage>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class UserViewHolder(private val binding: ItemInquiryMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: InquiryMessage) {
            binding.inquiryMessageBody.text = message.body
            bindTime(binding.inquiryMessageTime, message.createdAt)
        }
    }

    inner class AdminViewHolder(private val binding: ItemInquiryMessageAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: InquiryMessage) {
            binding.inquiryMessageBody.text = message.body
            bindTime(binding.inquiryMessageTime, message.createdAt)
            binding.inquiryMessageRead.visibility =
                if (message.readAt.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    inner class SystemViewHolder(private val binding: ItemInquiryMessageSystemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: InquiryMessage) {
            binding.inquiryMessageBody.text = message.body
        }
    }

    override fun getItemViewType(position: Int): Int = when (messages[position].senderType) {
        "USER" -> TYPE_USER
        "ADMIN" -> TYPE_ADMIN
        else -> TYPE_SYSTEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> UserViewHolder(ItemInquiryMessageUserBinding.inflate(inflater, parent, false))
            TYPE_ADMIN -> AdminViewHolder(ItemInquiryMessageAdminBinding.inflate(inflater, parent, false))
            else -> SystemViewHolder(ItemInquiryMessageSystemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AdminViewHolder -> holder.bind(message)
            is SystemViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newMessages: List<InquiryMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    private fun bindTime(view: android.widget.TextView, raw: String?) {
        val formatted = formatTime(raw)
        if (formatted == null) {
            view.visibility = android.view.View.GONE
        } else {
            view.visibility = android.view.View.VISIBLE
            view.text = formatted
        }
    }

    private fun formatTime(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.substringBefore('[')
        return runCatching {
            OffsetDateTime.parse(cleaned).format(TIME_FORMATTER)
        }.getOrNull()
    }

    private companion object {
        const val TYPE_USER = 0
        const val TYPE_ADMIN = 1
        const val TYPE_SYSTEM = 2
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
