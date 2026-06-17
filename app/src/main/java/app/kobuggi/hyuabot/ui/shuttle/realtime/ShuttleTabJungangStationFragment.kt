package app.kobuggi.hyuabot.ui.shuttle.realtime
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class ShuttleTabJungangStationFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })

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
            R.string.shuttle_tab_jungang_station,
            R.string.shuttle_header_bound_for_dormitory,
            childFragmentManager,
            emptyList()
        )
        val shuttleCampusRouteAdapter = ShuttleRouteAdapter(
            listOf(
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_type_jungang,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 3,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -18,
                        R.string.shuttle_tab_shuttlecock_out to -13,
                        R.string.shuttle_tab_station to -3,
                        R.string.shuttle_type_jungang to 0,
                        R.string.shuttle_tab_shuttlecock_in to 10,
                        R.string.shuttle_type_dormitory to 15,
                    )
                ),
            ),
            listOf(
                R.string.shuttle_type_dormitory
            )
        )
        val shuttleAdapter = ShuttleRealtimeByTimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_jungang_station,
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
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_jungang_station,
                    R.string.shuttle_header_bound_for_dormitory
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableDormitory.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_jungang_station,
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
            stopInfo.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_jungang_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            stopInfo2.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_jungang_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            infoButtonBoundForDormitory.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_TRANSFER_INFO)
                helpBoundForDormitory.visibility = if (helpBoundForDormitory.isVisible) View.GONE else View.VISIBLE
            }
            helpBoundForDormitoryRecycler.apply {
                adapter = shuttleCampusRouteAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
        }
        parentViewModel.latestShuttleResult.observe(viewLifecycleOwner) { source ->
            val now = LocalTime.now()
            val shuttle = source.result.firstOrNull { it.name == "jungang_stn" } ?: return@observe
            val shuttleByOrder = shuttle.timetable.order.filter { it.time > now }
            val shuttleForCampus = shuttle.timetable.destination.firstOrNull { it.destination == "CAMPUS" }?.entries?.filter { it.time > now } ?: emptyList()
            // Hide the layout by showing the destination conf
            binding.shuttleDestinationScroll.visibility = if (source.showByDestination) View.VISIBLE else View.GONE
            binding.shuttleTimeScroll.visibility = if (source.showByDestination) View.GONE else View.VISIBLE
            // Update the recycler view
            if (shuttleByOrder.isEmpty()) {
                binding.noRealtimeData.visibility = View.VISIBLE
                binding.realtimeView.visibility = View.GONE
            } else {
                binding.noRealtimeData.visibility = View.GONE
                binding.realtimeView.visibility = View.VISIBLE
                shuttleAdapter.updateData(shuttleByOrder.subList(0, min(8, shuttleByOrder.size)))
            }
            if (shuttleForCampus.isEmpty()) {
                binding.noRealtimeDataBoundForDormitory.visibility = View.VISIBLE
                binding.realtimeViewBoundForDormitory.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForDormitory.visibility = View.GONE
                binding.realtimeViewBoundForDormitory.visibility = View.VISIBLE
                shuttleCampusAdapter.updateData(shuttleForCampus.subList(0, min(8, shuttleForCampus.size)))
            }
        }

        binding.apply {
            headerBoundForStation.visibility = View.GONE
            realtimeViewBoundForStation.visibility = View.GONE
            noRealtimeDataBoundForStation.visibility = View.GONE
            entireTimetableBoundForStation.visibility = View.GONE
            entireTimetableStation.visibility = View.GONE
            headerBoundForTerminal.visibility = View.GONE
            realtimeViewBoundForTerminal.visibility = View.GONE
            noRealtimeDataBoundForTerminal.visibility = View.GONE
            entireTimetableBoundForTerminal.visibility = View.GONE
            entireTimetableDormitory.visibility = View.GONE
            headerBoundForJungangStation.visibility = View.GONE
            realtimeViewBoundForJungangStation.visibility = View.GONE
            noRealtimeDataBoundForJungangStation.visibility = View.GONE
            entireTimetableBoundForJungangStation.visibility = View.GONE
            entireTimetableJungangStation.visibility = View.GONE
        }
        parentViewModel.busAlternativeJungang80.observe(viewLifecycleOwner) { data ->
            updateBusAlternativeDormitory(data, parentViewModel.busAlternativeJungang62.value)
        }
        parentViewModel.busAlternativeJungang62.observe(viewLifecycleOwner) { busMinutes ->
            updateBusAlternativeDormitory(parentViewModel.busAlternativeJungang80.value, busMinutes)
        }
        bindShuttleHelpButtons(binding.helpButton, binding.helpButton2)
        return binding.root
    }

    private fun updateBusAlternativeDormitory(data80: BusAlternativeData?, minutes62: Int?) {
        val blueColor = requireContext().getColor(R.color.blue_bus)
        val greenColor = requireContext().getColor(R.color.green_bus)

        binding.busAlternativeDormitory.visibility = if (data80 != null) View.VISIBLE else View.GONE
        if (data80 != null) {
            binding.busAccentBarDormitory.setBackgroundColor(blueColor)
            binding.busAlternativeDormitoryRoute.setTextColor(blueColor)
            binding.busAlternativeDormitoryRoute.text = getString(data80.routeDisplayName)
            binding.busAlternativeDormitoryTime.text = if (data80.minutes != null)
                getString(R.string.shuttle_bus_alternative_time, data80.minutes)
            else getString(R.string.shuttle_bus_alternative_no_data)
        }

        binding.busAlternativeDormitory2.visibility = if (minutes62 != null) View.VISIBLE else View.GONE
        if (minutes62 != null) {
            binding.busAccentBarDormitory2.setBackgroundColor(greenColor)
            binding.busAlternativeDormitory2Route.setTextColor(greenColor)
            binding.busAlternativeDormitory2Route.text = getString(R.string.shuttle_bus_alternative_route_62_dormitory)
            binding.busAlternativeDormitory2Time.text = getString(R.string.shuttle_bus_alternative_time, minutes62)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }
}
