package app.kobuggi.hyuabot.ui.cafeteria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaTabBinding
import app.kobuggi.hyuabot.util.DividerItemWithoutLastDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CafeteriaTabLunchFragment : Fragment() {
    private val binding by lazy { FragmentCafeteriaTabBinding.inflate(layoutInflater) }
    private val parentViewModel: CafeteriaViewModel by viewModels({ requireParentFragment() })
    private var currentList: List<CafeteriaPageQuery.Cafeterium> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val cafeteriaAdapter = CafeteriaListAdapter(requireContext(), "lunch", listOf())
        val decoration = DividerItemWithoutLastDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        parentViewModel.apply {
            lunch.observe(viewLifecycleOwner) {
                currentList = it
                cafeteriaAdapter.updateList(it)
                binding.emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                binding.shareCafeteriaButton.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            }
        }
        binding.apply {
            shareCafeteriaButton.setOnClickListener {
                shareCafeteriaMenus(
                    requireContext(),
                    parentViewModel.date.value,
                    "lunch",
                    currentList,
                )
            }
            cafeteriaView.apply {
                adapter = cafeteriaAdapter
                addItemDecoration(decoration)
            }
        }
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }
}
