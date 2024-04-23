package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeTabBinding
import app.kobuggi.hyuabot.util.LinearLayoutManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class ShuttleTabStationFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val shuttleCampusAdapter = ShuttleRealtimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_station,
            R.string.shuttle_header_bound_for_dormitory,
            emptyList()
        )
        val shuttleTerminalAdapter = ShuttleRealtimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_station,
            R.string.shuttle_header_bound_for_terminal,
            emptyList()
        )
        val shuttleJungangStationAdapter = ShuttleRealtimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_station,
            R.string.shuttle_header_bound_for_jungang_station,
            emptyList()
        )

        binding.apply {
            realtimeViewBoundForDormitory.apply {
                adapter = shuttleCampusAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetableBoundForDormitory.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
                    R.string.shuttle_header_bound_for_dormitory
                ).also {
                    findNavController().navigate(it)
                }
            }
            realtimeViewBoundForTerminal.apply {
                adapter = shuttleTerminalAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetableBoundForTerminal.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
                    R.string.shuttle_header_bound_for_terminal
                ).also {
                    findNavController().navigate(it)
                }
            }
            realtimeViewBoundForJungangStation.apply {
                adapter = shuttleJungangStationAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetableBoundForJungangStation.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
                    R.string.shuttle_header_bound_for_jungang_station
                ).also {
                    findNavController().navigate(it)
                }
            }
            scrollView.viewTreeObserver.addOnScrollChangedListener {
                binding.swipeRefreshLayout.isEnabled = binding.scrollView.scrollY == 0
            }
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
            stopInfo.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_station
                ).also {
                    findNavController().navigate(it)
                }
            }
        }
        parentViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it) binding.swipeRefreshLayout.isRefreshing = false
        }
        parentViewModel.result.observe(viewLifecycleOwner) { result ->
            val shuttleForCampus = result.filter { it.stop == "station" }
            val shuttleForTerminal = shuttleForCampus.filter { it.tag == "C" }
            val shuttleForJungangStation = shuttleForCampus.filter { it.tag == "DJ" }

            if (shuttleForCampus.isEmpty()) {
                binding.noRealtimeDataBoundForDormitory.visibility = View.VISIBLE
                binding.realtimeViewBoundForDormitory.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForDormitory.visibility = View.GONE
                binding.realtimeViewBoundForDormitory.visibility = View.VISIBLE
                shuttleCampusAdapter.updateData(shuttleForCampus.subList(0, min(5, shuttleForCampus.size)))
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
            headerBoundForStation.visibility = View.GONE
            realtimeViewBoundForStation.visibility = View.GONE
            noRealtimeDataBoundForStation.visibility = View.GONE
            entireTimetableBoundForStation.visibility = View.GONE
        }
        return binding.root
    }
}
