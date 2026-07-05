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
import app.kobuggi.hyuabot.util.disableViewStateSaving
import kotlin.math.min
import app.kobuggi.hyuabot.widget.ShuttleWidgetSupport

@AndroidEntryPoint
class ShuttleTabTerminalFragment @Inject constructor() : Fragment() {
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
            R.string.shuttle_tab_terminal,
            R.string.shuttle_header_bound_for_dormitory,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("terminal", R.string.shuttle_tab_terminal, entry.seq, entry.time, entry.stops.map { it.stop to it.time })
            }
        )
        val shuttleCampusRouteAdapter = ShuttleRouteAdapter(
            listOf(
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_terminal,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -20,
                        R.string.shuttle_tab_terminal to 0,
                        R.string.shuttle_tab_shuttlecock_in to 10,
                        R.string.shuttle_type_dormitory to 15,
                    )
                ),
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_terminal,
                        R.string.shuttle_tab_shuttlecock_in,
                    ),
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -20,
                        R.string.shuttle_tab_terminal to 0,
                        R.string.shuttle_tab_shuttlecock_in to 10,
                    )
                ),
            ),
            listOf(
                R.string.shuttle_type_dormitory,
                R.string.shuttle_type_shuttlecock,
            )
        )
        val shuttleAdapter = ShuttleRealtimeByTimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_terminal,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { order ->
                showAlarmDialogForStop("terminal", R.string.shuttle_tab_terminal, order.seq, order.time, order.stops.map { it.stop to it.time })
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
                    R.string.shuttle_tab_terminal,
                    R.string.shuttle_header_bound_for_dormitory
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableDormitory.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_terminal,
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
                    R.string.shuttle_tab_terminal
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            stopInfo2.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_terminal
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
            val shuttle = source.result.firstOrNull { it.name == "terminal" } ?: return@observe
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
            entireTimetableTerminal.visibility = View.GONE
            headerBoundForJungangStation.visibility = View.GONE
            realtimeViewBoundForJungangStation.visibility = View.GONE
            noRealtimeDataBoundForJungangStation.visibility = View.GONE
            entireTimetableBoundForJungangStation.visibility = View.GONE
            entireTimetableJungangStation.visibility = View.GONE
        }
        parentViewModel.busAlternativeTerminal80.observe(viewLifecycleOwner) { data ->
            updateBusAlternativeDormitory(data, parentViewModel.busAlternativeTerminal62.value)
        }
        parentViewModel.busAlternativeTerminal62.observe(viewLifecycleOwner) { busMinutes ->
            updateBusAlternativeDormitory(parentViewModel.busAlternativeTerminal80.value, busMinutes)
        }
        parentViewModel.transfer.observe(viewLifecycleOwner) { data ->
            ShuttleTransferBinder.bind(binding.transferSection, binding.transferContainer, "terminal", data)
        }
        bindShuttleHelpButtons(binding.helpButton, binding.helpButton2)
        return binding.root.also { disableViewStateSaving(it) }
    }

    private fun showAlarmDialogForStop(boardingStopId: String, boardingLabelRes: Int, timetableSeq: Int, time: java.time.LocalTime, routeStops: List<Pair<String, java.time.LocalTime>>) {
        val boardingStop = parentViewModel.result.value?.firstOrNull { it.name == boardingStopId } ?: return
        val now = java.time.ZonedDateTime.now()
        var departureTime = now.toLocalDate().atTime(time).atZone(java.time.ZoneId.systemDefault())
        if (departureTime.isBefore(now)) departureTime = departureTime.plusDays(1)
        val departureTimeMillis = departureTime.toInstant().toEpochMilli()
        val minutes = kotlin.math.ceil((departureTimeMillis - System.currentTimeMillis()) / 60_000.0).toInt().coerceAtLeast(0)
        val allStops = parentViewModel.result.value ?: return
        val destStops = buildShuttleAlarmDestinationStopIds(routeStops, boardingStopId).mapNotNull { name ->
            allStops.firstOrNull { it.name == shuttleAlarmLocationStopId(name) }?.let {
                ShuttleAlarmDestinationStop(name, ShuttleWidgetSupport.stopDisplayName(requireContext(), it.name), it.latitude, it.longitude)
            }
        }
        val alarmKey = app.kobuggi.hyuabot.service.alarm.ShuttleAlarmService.buildAlarmKey(boardingStopId, timetableSeq)
        val checkpointTimes = buildShuttleAlarmCheckpointTimes(routeStops, boardingStopId, departureTimeMillis)
        val checkpointNames = buildShuttleAlarmCheckpointStopIds(routeStops, boardingStopId).map { ShuttleWidgetSupport.stopDisplayName(requireContext(), it) }.toTypedArray()
        val destTimes = buildShuttleAlarmDestinationTimes(routeStops, boardingStopId, departureTimeMillis)
        ShuttleAlarmDialogFragment.newInstance(
            boardingStopId, getString(boardingLabelRes), boardingStop.latitude, boardingStop.longitude,
            minutes, departureTimeMillis, alarmKey, checkpointNames, checkpointTimes, destTimes, destStops
        ).show(childFragmentManager, "shuttle_alarm")
    }

    private fun updateBusAlternativeDormitory(data80: BusAlternativeData?, data62: BusAlternativeData?) {
        val blueColor = requireContext().getColor(R.color.blue_bus)
        val greenColor = requireContext().getColor(R.color.green_bus)

        val shouldShow80 = data80?.minutes != null
        binding.busAlternativeDormitory.visibility = if (shouldShow80) View.VISIBLE else View.GONE
        if (shouldShow80) {
            binding.busAccentBarDormitory.setBackgroundColor(blueColor)
            binding.busAlternativeDormitoryRoute.setTextColor(blueColor)
            binding.busAlternativeDormitoryRoute.text = getString(data80.routeDisplayName)
            binding.busAlternativeDormitoryTime.text = getString(R.string.shuttle_bus_alternative_time, data80.minutes)

            bindBusAlternativeInfo(
                binding.busAlternativeDormitoryInfo,
                "terminal",
                getString(R.string.shuttle_tab_terminal),
                data80
            )
        } else {
            binding.busAlternativeDormitoryInfo.isEnabled = false
            binding.busAlternativeDormitoryInfo.alpha = 0.38f
        }

        val shouldShow62 = data62?.minutes != null
        binding.busAlternativeDormitory2.visibility = if (shouldShow62) View.VISIBLE else View.GONE
        if (shouldShow62) {
            binding.busAccentBarDormitory2.setBackgroundColor(greenColor)
            binding.busAlternativeDormitory2Route.setTextColor(greenColor)
            binding.busAlternativeDormitory2Route.text = getString(data62.routeDisplayName)
            binding.busAlternativeDormitory2Time.text = getString(R.string.shuttle_bus_alternative_time, data62.minutes)
            bindBusAlternativeInfo(
                binding.busAlternativeDormitory2Info,
                "terminal",
                getString(R.string.shuttle_tab_terminal),
                data62
            )
        } else {
            binding.busAlternativeDormitory2Info.isEnabled = false
            binding.busAlternativeDormitory2Info.alpha = 0.38f
        }
    }

    private fun bindBusAlternativeInfo(
        button: View,
        shuttleStopId: String,
        shuttleStopName: String,
        data: BusAlternativeData?
    ) {
        val hasStopInfo = data != null && data.stopLat != 0.0
        button.isEnabled = hasStopInfo
        button.alpha = if (hasStopInfo) 1f else 0.38f
        if (!hasStopInfo) {
            button.setOnClickListener(null)
            return
        }
        val shuttleStop = parentViewModel.result.value?.firstOrNull { it.name == shuttleStopId }
        button.setOnClickListener {
            BusAlternativeStopSheet.newInstance(
                shuttleStopName,
                shuttleStop?.latitude ?: 0.0,
                shuttleStop?.longitude ?: 0.0,
                data.stopName,
                data.stopLat,
                data.stopLng
            ).show(childFragmentManager, "bus_stop_info")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }
}
