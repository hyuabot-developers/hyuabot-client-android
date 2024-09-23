package app.kobuggi.hyuabot.ui.cafeteria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaTabBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CafeteriaTabBreakfastFragment : Fragment() {
    private val binding by lazy { FragmentCafeteriaTabBinding.inflate(layoutInflater) }
    private val parentViewModel: CafeteriaViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val cafeteriaAdapter = CafeteriaListAdapter(requireContext(), "breakfast", listOf())
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        parentViewModel.apply {
            breakfast.observe(viewLifecycleOwner) {
                cafeteriaAdapter.updateList(it)
                binding.emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
            isLoading.observe(viewLifecycleOwner) {
                if (!it) binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        binding.apply {
            cafeteriaView.apply {
                adapter = cafeteriaAdapter
                addItemDecoration(decoration)
            }
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.campusID.observe(viewLifecycleOwner) {
                    parentViewModel.fetchData(it)
                }
            }
        }
        return binding.root
    }
}
