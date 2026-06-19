package app.kobuggi.hyuabot.ui.contact
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.databinding.ItemContactBinding
import app.kobuggi.hyuabot.service.database.entity.Contact

class ContactListAdapter(
    private var contacts: List<Contact>,
    private val onClickItem: (Contact) -> Unit,
    private val onLongClickItem: (Contact) -> Unit = {},
): RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemContactBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.contactName.text = contact.name
            binding.contactPhone.text = contact.phone
            binding.root.setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.CONTACT_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM, name = contact.name); onClickItem(contact) }
            binding.root.setOnLongClickListener {
                onLongClickItem(contact)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}
