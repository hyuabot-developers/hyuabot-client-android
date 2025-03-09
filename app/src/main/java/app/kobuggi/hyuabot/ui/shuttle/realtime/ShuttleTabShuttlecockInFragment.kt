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
class ShuttleTabShuttlecockInFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })
    private val shuttleTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val shuttleCampusAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_in,
            R.string.shuttle_header_bound_for_dormitory,
            childFragmentManager,
            emptyList()
        )
        val shuttleAdapter = ShuttleRealtimeByTimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_in,
            childFragmentManager,
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
                    R.string.shuttle_tab_shuttlecock_in,
                    R.string.shuttle_header_bound_for_dormitory
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            realtimeView.apply {
                adapter = shuttleAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetable.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_in,
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            stopInfo.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_shuttlecock_in
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
            val shuttle = source.result.filter { it.stop == "shuttlecock_i" && it.time > now.format(shuttleTimeFormatter) }
            // Hide the layout by showing the destination conf
            binding.shuttleDestinationLayout.visibility = if (source.showByDestination) View.VISIBLE else View.GONE
            binding.shuttleTimeLayout.visibility = if (source.showByDestination) View.GONE else View.VISIBLE
            // Update the recycler view
            if (shuttle.isEmpty()) {
                binding.noRealtimeDataBoundForDormitory.visibility = View.VISIBLE
                binding.realtimeViewBoundForDormitory.visibility = View.GONE
                binding.noRealtimeData.visibility = View.VISIBLE
                binding.realtimeView.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForDormitory.visibility = View.GONE
                binding.realtimeViewBoundForDormitory.visibility = View.VISIBLE
                binding.noRealtimeData.visibility = View.GONE
                binding.realtimeView.visibility = View.VISIBLE
                shuttleCampusAdapter.updateData(shuttle.subList(0, min(8, shuttle.size)))
                shuttleAdapter.updateData(shuttle.subList(0, min(8, shuttle.size)))
            }
        }

        binding.apply {
            headerBoundForStation.visibility = View.GONE
            realtimeViewBoundForStation.visibility = View.GONE
            noRealtimeDataBoundForStation.visibility = View.GONE
            entireTimetableBoundForStation.visibility = View.GONE
            headerBoundForTerminal.visibility = View.GONE
            realtimeViewBoundForTerminal.visibility = View.GONE
            noRealtimeDataBoundForTerminal.visibility = View.GONE
            entireTimetableBoundForTerminal.visibility = View.GONE
            headerBoundForJungangStation.visibility = View.GONE
            realtimeViewBoundForJungangStation.visibility = View.GONE
            noRealtimeDataBoundForJungangStation.visibility = View.GONE
            entireTimetableBoundForJungangStation.visibility = View.GONE
        }
        return binding.root
    }
}
