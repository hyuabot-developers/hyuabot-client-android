package app.kobuggi.hyuabot.ui.contact

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            contacts.observe(viewLifecycleOwner) {
                campusID.observe(viewLifecycleOwner) { campusID ->
                    listAdapter.updateData(it.filter { contact -> contact.campusID == campusID })
                }
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
                            viewModel.campusID.observe(viewLifecycleOwner) { campusID ->
                                viewModel.searchContacts(editable.toString(), campusID)
                            }
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

    fun onClickItem(contact: Contact) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${contact.phone}")
        startActivity(intent)
    }
}
