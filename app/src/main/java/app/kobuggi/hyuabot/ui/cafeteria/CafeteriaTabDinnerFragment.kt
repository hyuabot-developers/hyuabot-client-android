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
class CafeteriaTabDinnerFragment : Fragment() {
    private val binding by lazy { FragmentCafeteriaTabBinding.inflate(layoutInflater) }
    private val parentViewModel: CafeteriaViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val cafeteriaAdapter = CafeteriaListAdapter(requireContext(), "dinner", listOf())
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        parentViewModel.apply {
            dinner.observe(viewLifecycleOwner) {
                cafeteriaAdapter.updateList(it)
                binding.emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        binding.apply {
            cafeteriaView.apply {
                adapter = cafeteriaAdapter
                addItemDecoration(decoration)
            }
        }
        return binding.root
    }
}
