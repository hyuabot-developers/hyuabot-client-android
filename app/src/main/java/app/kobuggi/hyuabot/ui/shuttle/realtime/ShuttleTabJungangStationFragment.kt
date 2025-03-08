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
class ShuttleTabJungangStationFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })
    private val shuttleTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

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
            R.string.shuttle_tab_jungang_station,
            R.string.shuttle_header_bound_for_dormitory,
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
                    R.string.shuttle_tab_jungang_station,
                    R.string.shuttle_header_bound_for_dormitory
                ).also {
                    findNavController().safeNavigate(it)
                }
            }

            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
            stopInfo.setOnClickListener {
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_jungang_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
        }
        parentViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it) binding.swipeRefreshLayout.isRefreshing = false
        }

        parentViewModel.result.observe(viewLifecycleOwner) { result ->
            val now = LocalTime.now()
            val shuttleForCampus = result.filter { it.stop == "jungang_stn" && it.time > now.format(shuttleTimeFormatter) }
            if (shuttleForCampus.isEmpty()) {
                binding.noRealtimeDataBoundForDormitory.visibility = View.VISIBLE
                binding.realtimeViewBoundForDormitory.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForDormitory.visibility = View.GONE
                binding.realtimeViewBoundForDormitory.visibility = View.VISIBLE
                shuttleCampusAdapter.updateData(shuttleForCampus.subList(0, min(8, shuttleForCampus.size)))
            }
        }

        parentViewModel.showByDestination.observe(viewLifecycleOwner) {
            binding.shuttleRealtimeDestinationLayout.visibility = if (it) View.VISIBLE else View.GONE
            binding.shuttleRealtimeTimeLayout.visibility = if (it) View.GONE else View.VISIBLE
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
