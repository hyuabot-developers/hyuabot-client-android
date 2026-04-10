package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeTabBinding
import app.kobuggi.hyuabot.service.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubwayTabYellowFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val upAdapter = SubwayRealtimeListAdapter(requireContext())
        val downAdapter = SubwayRealtimeListAdapter(requireContext())
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        binding.apply {
            headerUp.text = getString(R.string.subway_yellow_up)
            realtimeViewUp.apply {
                adapter = upAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            entireTimetableUp.setOnClickListener {
                SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("K251", "up").also {
                    findNavController().safeNavigate(it)
                }
            }
            headerDown.text = getString(R.string.subway_yellow_down)
            realtimeViewDown.apply {
                adapter = downAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
            entireTimetableDown.setOnClickListener {
                SubwayRealtimeFragmentDirections.actionSubwayRealtimeFragmentToSubwayTimetableFragment("K251", "down").also {
                    findNavController().safeNavigate(it)
                }
            }
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
        }
        parentViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it) binding.swipeRefreshLayout.isRefreshing = false
        }
        parentViewModel.campusYellow.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val upEntries = it.arrival.firstOrNull { arrival -> arrival.direction == "up" }?.entries ?: emptyList()
            val downEntries = it.arrival.firstOrNull { arrival -> arrival.direction == "down" }?.entries ?: emptyList()
            upAdapter.updateData(upEntries)
            downAdapter.updateData(downEntries)
            if (upEntries.isEmpty()) {
                binding.noRealtimeDataUp.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataUp.visibility = View.GONE
            }
            if (downEntries.isEmpty()) {
                binding.noRealtimeDataDown.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataDown.visibility = View.GONE
            }
        }
        return binding.root
    }
}
