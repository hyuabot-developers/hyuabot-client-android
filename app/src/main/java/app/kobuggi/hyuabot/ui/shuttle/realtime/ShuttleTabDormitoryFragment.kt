package app.kobuggi.hyuabot.ui.shuttle.realtime

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
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeTabBinding
import app.kobuggi.hyuabot.util.LinearLayoutManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class ShuttleTabDormitoryFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val shuttleStationAdapter = ShuttleRealtimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_dormitory_out,
            R.string.shuttle_header_bound_for_station,
            emptyList()
        )
        val shuttleTerminalAdapter = ShuttleRealtimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_dormitory_out,
            R.string.shuttle_header_bound_for_terminal,
            emptyList()
        )
        val shuttleJungangStationAdapter = ShuttleRealtimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_dormitory_out,
            R.string.shuttle_header_bound_for_jungang_station,
            emptyList()
        )

        binding.apply {
            realtimeViewBoundForStation.apply {
                adapter = shuttleStationAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            realtimeViewBoundForTerminal.apply {
                adapter = shuttleTerminalAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            realtimeViewBoundForJungangStation.apply {
                adapter = shuttleJungangStationAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
        }
        parentViewModel.result.observe(viewLifecycleOwner) { result ->
            val shuttle = result.filter { it.stop == "dormitory_o" }
            val shuttleForStation = shuttle.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
            val shuttleForTerminal = shuttle.filter { it.tag == "DY" || it.tag == "C" }
            val shuttleForJungangStation = shuttle.filter { it.tag == "DJ" }

            if (shuttleForStation.isEmpty()) {
                binding.noRealtimeDataBoundForStation.visibility = View.VISIBLE
                binding.realtimeViewBoundForStation.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForStation.visibility = View.GONE
                binding.realtimeViewBoundForStation.visibility = View.VISIBLE
                shuttleStationAdapter.updateData(shuttleForStation.subList(0, min(5, shuttleForStation.size)))
            }

            if (shuttleForTerminal.isEmpty()) {
                binding.noRealtimeDataBoundForTerminal.visibility = View.VISIBLE
                binding.realtimeViewBoundForTerminal.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForTerminal.visibility = View.GONE
                binding.realtimeViewBoundForTerminal.visibility = View.VISIBLE
                shuttleTerminalAdapter.updateData(shuttleForTerminal.subList(0, min(5, shuttleForTerminal.size)))
            }

            if (shuttleForJungangStation.isEmpty()) {
                binding.noRealtimeDataBoundForJungangStation.visibility = View.VISIBLE
                binding.realtimeViewBoundForJungangStation.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForJungangStation.visibility = View.GONE
                binding.realtimeViewBoundForJungangStation.visibility = View.VISIBLE
                shuttleJungangStationAdapter.updateData(shuttleForJungangStation.subList(0, min(5, shuttleForJungangStation.size)))
            }
        }
        binding.apply {
            headerBoundForDormitory.visibility = View.GONE
            realtimeViewBoundForDormitory.visibility = View.GONE
            noRealtimeDataBoundForDormitory.visibility = View.GONE
            entireTimetableBoundForDormitory.visibility = View.GONE
        }
        return binding.root
    }
}
