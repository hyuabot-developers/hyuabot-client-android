package app.kobuggi.hyuabot.ui.contact

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentContactBinding
import app.kobuggi.hyuabot.service.database.entity.Contact
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContactFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentContactBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ContactViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listAdapter = ContactListAdapter(emptyList(), { onClickItem(it) }, { showContactActions(it) })
        val searchListAdapter = ContactListAdapter(emptyList(), { onClickItem(it) }, { showContactActions(it) })
        viewModel.apply {
            fetchContactVersion()
            queryError.observe(viewLifecycleOwner) {
                it?.let { Toast.makeText(requireContext(), getString(R.string.contact_error), Toast.LENGTH_SHORT).show() }
            }
            updating.observe(viewLifecycleOwner) {
                binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
            }
            contacts.observe(viewLifecycleOwner) { contactList ->
                val currentCampusID = campusID.value ?: return@observe
                listAdapter.updateData(contactList.filter { it.campusID == currentCampusID })
            }
            campusID.observe(viewLifecycleOwner) { id ->
                val currentContacts = contacts.value ?: return@observe
                listAdapter.updateData(currentContacts.filter { it.campusID == id })
            }
            searchResults.observe(viewLifecycleOwner) {
                searchListAdapter.updateData(it)
            }
        }
        binding.apply {
            contactListView.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
            }
            searchView
                .editText
                .apply {
                    addTextChangedListener { editable ->
                        if (editable.isNullOrBlank()) {
                            searchListAdapter.updateData(emptyList())
                        } else {
                            val currentCampusID = viewModel.campusID.value ?: return@addTextChangedListener
                            viewModel.searchContacts(editable.toString(), currentCampusID)
                        }
                    }
                }
            contactSearchView.apply {
                adapter = searchListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
            }
        }
        return binding.root
    }

    private fun onClickItem(contact: Contact) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = "tel:${contact.phone}".toUri()
        startActivity(intent)
    }

    private fun showContactActions(contact: Contact) {
        val labels = arrayOf(
            getString(R.string.contact_action_copy_phone),
            getString(R.string.contact_action_share)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(contact.name)
            .setItems(labels) { _, which ->
                when (which) {
                    0 -> copyContactPhone(contact)
                    1 -> shareContact(contact)
                }
            }
            .show()
    }

    private fun copyContactPhone(contact: Contact) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(contact.name, contact.phone))
        Toast.makeText(requireContext(), R.string.contact_phone_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareContact(contact: Contact) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_share_format, contact.name, contact.phone))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.contact_action_share)))
    }
}
