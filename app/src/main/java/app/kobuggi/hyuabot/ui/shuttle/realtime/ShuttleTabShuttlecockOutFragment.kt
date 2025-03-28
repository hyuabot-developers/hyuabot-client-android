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
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.util.LinearLayoutManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class ShuttleTabShuttlecockOutFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })
    private val shuttleTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val shuttleStationAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_header_bound_for_station,
            childFragmentManager,
            emptyList()
        )
        val shuttleTerminalAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_header_bound_for_terminal,
            childFragmentManager,
            emptyList()
        )
        val shuttleJungangStationAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_header_bound_for_jungang_station,
            childFragmentManager,
            emptyList()
        )
        val shuttleAdapter = ShuttleRealtimeByTimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_out,
            childFragmentManager,
            emptyList()
        )

        binding.apply {
            realtimeViewBoundForStation.apply {
                adapter = shuttleStationAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetableBoundForStation.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableStation.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            realtimeViewBoundForTerminal.apply {
                adapter = shuttleTerminalAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetableBoundForTerminal.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_terminal
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableTerminal.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_terminal
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            realtimeViewBoundForJungangStation.apply {
                adapter = shuttleJungangStationAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetableBoundForJungangStation.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_jungang_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableJungangStation.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_jungang_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            realtimeView.apply {
                adapter = shuttleAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            stopInfo.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_shuttlecock_out
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            stopInfo2.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_shuttlecock_in
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
        }
        parentViewModel.latestShuttleResult.observe(viewLifecycleOwner) { source ->
            val now = LocalTime.now()
            val shuttle = source.result.filter { it.stop == "shuttlecock_o" && it.time >= now.format(shuttleTimeFormatter) }
            val shuttleForStation = shuttle.filter { it.tag == "DH" || it.tag == "DJ" || it.tag == "C" }
            val shuttleForTerminal = shuttle.filter { it.tag == "DY" || it.tag == "C" }
            val shuttleForJungangStation = shuttle.filter { it.tag == "DJ" }
            // Hide the layout by showing the destination conf
            binding.shuttleDestinationLayout.visibility = if (source.showByDestination) View.VISIBLE else View.GONE
            binding.shuttleTimeLayout.visibility = if (source.showByDestination) View.GONE else View.VISIBLE
            // Update the recycler view
            if (shuttle.isEmpty()) {
                binding.noRealtimeData.visibility = View.VISIBLE
                binding.realtimeView.visibility = View.GONE
            } else {
                binding.noRealtimeData.visibility = View.GONE
                binding.realtimeView.visibility = View.VISIBLE
                shuttleAdapter.updateData(shuttle.subList(0, min(8, shuttle.size)))
            }
            if (shuttleForStation.isEmpty()) {
                binding.noRealtimeDataBoundForStation.visibility = View.VISIBLE
                binding.realtimeViewBoundForStation.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForStation.visibility = View.GONE
                binding.realtimeViewBoundForStation.visibility = View.VISIBLE
                shuttleStationAdapter.updateData(shuttleForStation.subList(0, min(3, shuttleForStation.size)))
            }
            if (shuttleForTerminal.isEmpty()) {
                binding.noRealtimeDataBoundForTerminal.visibility = View.VISIBLE
                binding.realtimeViewBoundForTerminal.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForTerminal.visibility = View.GONE
                binding.realtimeViewBoundForTerminal.visibility = View.VISIBLE
                shuttleTerminalAdapter.updateData(shuttleForTerminal.subList(0, min(3, shuttleForTerminal.size)))
            }
            if (shuttleForJungangStation.isEmpty()) {
                binding.noRealtimeDataBoundForJungangStation.visibility = View.VISIBLE
                binding.realtimeViewBoundForJungangStation.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForJungangStation.visibility = View.GONE
                binding.realtimeViewBoundForJungangStation.visibility = View.VISIBLE
                shuttleJungangStationAdapter.updateData(shuttleForJungangStation.subList(0, min(3, shuttleForJungangStation.size)))
            }
        }

        binding.apply {
            headerBoundForDormitory.visibility = View.GONE
            realtimeViewBoundForDormitory.visibility = View.GONE
            noRealtimeDataBoundForDormitory.visibility = View.GONE
            entireTimetableBoundForDormitory.visibility = View.GONE
            entireTimetableDormitory.visibility = View.GONE
        }
        return binding.root
    }
}
