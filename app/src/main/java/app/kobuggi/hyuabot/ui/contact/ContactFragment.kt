package app.kobuggi.hyuabot.ui.contact

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
        val listAdapter = ContactListAdapter(emptyList()) { onClickItem(it) }
        val searchListAdapter = ContactListAdapter(emptyList()) { onClickItem(it) }
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
}
