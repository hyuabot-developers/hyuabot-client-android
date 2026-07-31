package app.kobuggi.hyuabot.ui.inquiry

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.databinding.ItemInquiryMessageAdminBinding
import app.kobuggi.hyuabot.databinding.ItemInquiryMessageDateHeaderBinding
import app.kobuggi.hyuabot.databinding.ItemInquiryMessageSystemBinding
import app.kobuggi.hyuabot.databinding.ItemInquiryMessageUserBinding
import app.kobuggi.hyuabot.service.InquiryMessage
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class InquiryMessageAdapter(
    messages: List<InquiryMessage>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = buildItems(messages)

    private sealed interface Item {
        data class DateHeader(val label: String) : Item
        data class Message(val value: InquiryMessage) : Item
    }

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

    private inner class DateHeaderViewHolder(private val binding: ItemInquiryMessageDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Item.DateHeader) {
            binding.inquiryDateHeader.text = header.label
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is Item.DateHeader -> TYPE_DATE_HEADER
        is Item.Message -> when (item.value.senderType) {
            "USER" -> TYPE_USER
            "ADMIN" -> TYPE_ADMIN
            else -> TYPE_SYSTEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> UserViewHolder(ItemInquiryMessageUserBinding.inflate(inflater, parent, false))
            TYPE_ADMIN -> AdminViewHolder(ItemInquiryMessageAdminBinding.inflate(inflater, parent, false))
            TYPE_SYSTEM -> SystemViewHolder(ItemInquiryMessageSystemBinding.inflate(inflater, parent, false))
            else -> DateHeaderViewHolder(ItemInquiryMessageDateHeaderBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is Item.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is Item.Message -> when (holder) {
                is UserViewHolder -> holder.bind(item.value)
                is AdminViewHolder -> holder.bind(item.value)
                is SystemViewHolder -> holder.bind(item.value)
                else -> Unit
            }
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newMessages: List<InquiryMessage>) {
        items = buildItems(newMessages)
        notifyDataSetChanged()
    }

    private fun buildItems(messages: List<InquiryMessage>): List<Item> {
        val result = mutableListOf<Item>()
        var previousDateLabel: String? = null
        messages.forEach { message ->
            val dateLabel = formatDate(message.createdAt)
            if (dateLabel != null && dateLabel != previousDateLabel) {
                result += Item.DateHeader(dateLabel)
                previousDateLabel = dateLabel
            }
            result += Item.Message(message)
        }
        return result
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

    private fun formatDate(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.substringBefore('[')
        return runCatching {
            OffsetDateTime.parse(cleaned).format(DATE_FORMATTER)
        }.getOrNull()
    }

    private companion object {
        const val TYPE_USER = 0
        const val TYPE_ADMIN = 1
        const val TYPE_SYSTEM = 2
        const val TYPE_DATE_HEADER = 3
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
    }
}
