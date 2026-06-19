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
import app.kobuggi.hyuabot.widget.ShuttleWidgetSupport

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
        val shuttleCampusAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_station,
            R.string.shuttle_header_bound_for_dormitory,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("station", R.string.shuttle_tab_station, entry.seq, entry.time, entry.stops.map { it.stop })
            }
        )
        val shuttleCampusRouteAdapter = ShuttleRouteAdapter(
            listOf(
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(R.color.red_bus),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_tab_shuttlecock_in to 10,
                        R.string.shuttle_type_dormitory to 15
                    )
                ),
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(R.color.red_bus),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_tab_shuttlecock_in,
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_tab_shuttlecock_in to 10,
                    )
                ),
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_tab_terminal,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_tab_terminal to 5,
                        R.string.shuttle_tab_shuttlecock_in to 15,
                        R.string.shuttle_type_dormitory to 20
                    )
                ),
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_tab_terminal,
                        R.string.shuttle_tab_shuttlecock_in,
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_tab_terminal to 5,
                        R.string.shuttle_tab_shuttlecock_in to 15,
                    )
                ),
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(R.color.hanyang_green),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_type_jungang,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_type_jungang to 3,
                        R.string.shuttle_tab_shuttlecock_in to 13,
                        R.string.shuttle_type_dormitory to 18
                    )
                ),
            ),
            listOf(
                R.string.shuttle_type_dormitory_direct,
                R.string.shuttle_type_dormitory_circular,
                R.string.shuttle_type_shuttlecock_direct,
                R.string.shuttle_type_shuttlecock_circular,
                R.string.shuttle_type_jungang,
            )
        )
        val shuttleTerminalAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_station,
            R.string.shuttle_header_bound_for_terminal,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("station", R.string.shuttle_tab_station, entry.seq, entry.time, entry.stops.map { it.stop })
            }
        )
        val shuttleTerminalRouteAdapter = ShuttleRouteAdapter(
            listOf(
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_tab_terminal,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_tab_terminal to 5,
                        R.string.shuttle_tab_shuttlecock_in to 15,
                        R.string.shuttle_type_dormitory to 20
                    )
                ),
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_tab_terminal,
                        R.string.shuttle_tab_shuttlecock_in,
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_tab_terminal to 5,
                        R.string.shuttle_tab_shuttlecock_in to 15,
                    )
                ),
            ),
            listOf(
                R.string.shuttle_type_dormitory_circular,
                R.string.shuttle_type_shuttlecock_circular,
                R.string.shuttle_type_jungang,
            )
        )
        val shuttleJungangStationAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_station,
            R.string.shuttle_header_bound_for_jungang_station,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("station", R.string.shuttle_tab_station, entry.seq, entry.time, entry.stops.map { it.stop })
            }
        )
        val shuttleJungangStationRouteAdapter = ShuttleRouteAdapter(
            listOf(
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(R.color.hanyang_green),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_station,
                        R.string.shuttle_type_jungang,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 2,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -15,
                        R.string.shuttle_tab_shuttlecock_out to -10,
                        R.string.shuttle_tab_station to 0,
                        R.string.shuttle_type_jungang to 3,
                        R.string.shuttle_tab_shuttlecock_in to 13,
                        R.string.shuttle_type_dormitory to 18
                    )
                ),
            ),
            listOf(
                R.string.shuttle_type_jungang,
            )
        )
        val shuttleAdapter = ShuttleRealtimeByTimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_station,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { order ->
                showAlarmDialogForStop("station", R.string.shuttle_tab_station, order.seq, order.time, order.stops.map { it.stop })
            }
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
                    R.string.shuttle_tab_station,
                    R.string.shuttle_header_bound_for_dormitory
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableDormitory.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
                    R.string.shuttle_header_bound_for_dormitory
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
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
                    R.string.shuttle_header_bound_for_terminal
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableTerminal.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
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
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
                    R.string.shuttle_header_bound_for_jungang_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableJungangStation.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_station,
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
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            stopInfo2.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_station
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
            infoButtonBoundForTerminal.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_TRANSFER_INFO)
                helpBoundForTerminal.visibility = if (helpBoundForTerminal.isVisible) View.GONE else View.VISIBLE
            }
            helpBoundForTerminalRecycler.apply {
                adapter = shuttleTerminalRouteAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
            infoButtonBoundForJungangStation.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_TRANSFER_INFO)
                helpBoundForJungangStation.visibility = if (helpBoundForJungangStation.isVisible) View.GONE else View.VISIBLE
            }
            helpBoundForJungangStationRecycler.apply {
                adapter = shuttleJungangStationRouteAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
        }
        parentViewModel.latestShuttleResult.observe(viewLifecycleOwner) { source ->
            val now = LocalTime.now()
            val shuttle = source.result.firstOrNull { it.name == "station" } ?: return@observe
            val shuttleByOrder = shuttle.timetable.order.filter { it.time > now }
            val shuttleForCampus = shuttle.timetable.destination.firstOrNull { it.destination == "CAMPUS" }?.entries?.filter { it.time > now } ?: emptyList()
            val shuttleForTerminal = shuttle.timetable.destination.firstOrNull { it.destination == "TERMINAL" }?.entries?.filter { it.time > now } ?: emptyList()
            val shuttleForJungangStation = shuttle.timetable.destination.firstOrNull { it.destination == "JUNGANG" }?.entries?.filter { it.time > now } ?: emptyList()
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
                shuttleAdapter.updateData(shuttleByOrder.subList(0, min(3, shuttleByOrder.size)))
            }

            if (shuttleForCampus.isEmpty()) {
                binding.noRealtimeDataBoundForDormitory.visibility = View.VISIBLE
                binding.realtimeViewBoundForDormitory.visibility = View.GONE
            } else {
                binding.noRealtimeDataBoundForDormitory.visibility = View.GONE
                binding.realtimeViewBoundForDormitory.visibility = View.VISIBLE
                shuttleCampusAdapter.updateData(shuttleForCampus.subList(0, min(3, shuttleForCampus.size)))
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
        parentViewModel.busAlternativeStation.observe(viewLifecycleOwner) { busMinutes ->
            updateBusAlternativeDormitory(busMinutes)
        }

        binding.apply {
            headerBoundForStation.visibility = View.GONE
            realtimeViewBoundForStation.visibility = View.GONE
            noRealtimeDataBoundForStation.visibility = View.GONE
            entireTimetableBoundForStation.visibility = View.GONE
            entireTimetableStation.visibility = View.GONE
        }
        bindShuttleHelpButtons(binding.helpButton, binding.helpButton2)
        return binding.root
    }

    private fun showAlarmDialogForStop(boardingStopId: String, boardingLabelRes: Int, timetableSeq: Int, time: java.time.LocalTime, stopNames: List<String>) {
        val boardingStop = parentViewModel.result.value?.firstOrNull { it.name == boardingStopId } ?: return
        val now = java.time.ZonedDateTime.now()
        var departureTime = now.toLocalDate().atTime(time).atZone(java.time.ZoneId.systemDefault())
        if (departureTime.isBefore(now)) departureTime = departureTime.plusDays(1)
        val departureTimeMillis = departureTime.toInstant().toEpochMilli()
        val minutes = kotlin.math.ceil((departureTimeMillis - System.currentTimeMillis()) / 60_000.0).toInt().coerceAtLeast(0)
        val allStops = parentViewModel.result.value ?: return
        val destStops = stopNames.mapNotNull { name ->
            allStops.firstOrNull { it.name == name }?.let {
                Triple(ShuttleWidgetSupport.stopDisplayName(requireContext(), it.name), it.latitude, it.longitude)
            }
        }
        val alarmKey = app.kobuggi.hyuabot.service.alarm.ShuttleAlarmService.buildAlarmKey(boardingStopId, timetableSeq)
        ShuttleAlarmDialogFragment.newInstance(
            getString(boardingLabelRes), boardingStop.latitude, boardingStop.longitude,
            minutes, departureTimeMillis, alarmKey, destStops
        ).show(childFragmentManager, "shuttle_alarm")
    }

    private fun updateBusAlternativeDormitory(data: BusAlternativeData?) {
        val shouldShow = data?.minutes != null
        binding.busAlternativeDormitory.visibility = if (shouldShow) View.VISIBLE else View.GONE
        if (shouldShow) {
            binding.busAlternativeDormitoryTime.text = getString(R.string.shuttle_bus_alternative_time, data.minutes)
        }
        binding.busAlternativeDormitory2.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }
}
