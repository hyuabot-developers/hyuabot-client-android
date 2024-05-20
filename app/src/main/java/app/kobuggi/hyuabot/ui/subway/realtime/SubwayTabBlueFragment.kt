package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubwayTabBlueFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val upAdapter = SubwayRealtimeListAdapter(requireContext(), listOf(), null, listOf(), null)
        val downAdapter = SubwayRealtimeListAdapter(requireContext(), null, listOf(), null, listOf())
        binding.apply {
            headerUp.text = getString(R.string.subway_blue_up)
            realtimeViewUp.apply {
                adapter = upAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            headerDown.text = getString(R.string.subway_blue_down)
            realtimeViewDown.apply {
                adapter = downAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
        }
        parentViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it) binding.swipeRefreshLayout.isRefreshing = false
        }
        return binding.root
    }
}
