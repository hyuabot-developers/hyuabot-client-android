package app.kobuggi.hyuabot.ui.shuttle.realtime
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsItem

import app.kobuggi.hyuabot.ui.common.ListPresentation
import app.kobuggi.hyuabot.ui.common.RequestUiState
import app.kobuggi.hyuabot.ui.common.StateKind
import app.kobuggi.hyuabot.ui.common.StateView
import app.kobuggi.hyuabot.ui.common.resolveListPresentation
import app.kobuggi.hyuabot.util.AnalyticsScreen
import app.kobuggi.hyuabot.util.ServiceClock
import app.kobuggi.hyuabot.util.TransportMode
import java.time.ZonedDateTime

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
class ShuttleTabShuttlecockOutFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })
    private var nextStationShuttleTime: LocalTime? = null
    private var nextTerminalShuttleTime: LocalTime? = null
    private var nextJungangShuttleTime: LocalTime? = null

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
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("shuttlecock_o", R.string.shuttle_tab_shuttlecock_out, entry.seq, entry.time, entry.stops.map { it.stop to it.time })
            }
        )
        val shuttleStationRouteAdapter = ShuttleRouteAdapter(
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
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -5,
                        R.string.shuttle_tab_shuttlecock_out to 0,
                        R.string.shuttle_tab_station to 10,
                        R.string.shuttle_tab_shuttlecock_in to 20,
                        R.string.shuttle_type_dormitory to 25
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
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -5,
                        R.string.shuttle_tab_shuttlecock_out to 0,
                        R.string.shuttle_tab_station to 10,
                        R.string.shuttle_tab_terminal to 15,
                        R.string.shuttle_tab_shuttlecock_in to 25,
                        R.string.shuttle_type_dormitory to 30
                    )
                )
            ),
            listOf(R.string.shuttle_type_direct, R.string.shuttle_type_circular)
        )
        val shuttleTerminalAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_header_bound_for_terminal,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("shuttlecock_o", R.string.shuttle_tab_shuttlecock_out, entry.seq, entry.time, entry.stops.map { it.stop to it.time })
            }
        )
        val shuttleTerminalRouteAdapter = ShuttleRouteAdapter(
            listOf(
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(R.color.red_bus),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_out,
                        R.string.shuttle_tab_terminal,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -5,
                        R.string.shuttle_tab_shuttlecock_out to 0,
                        R.string.shuttle_tab_terminal to 10,
                        R.string.shuttle_tab_shuttlecock_in to 15,
                        R.string.shuttle_type_dormitory to 25
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
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -5,
                        R.string.shuttle_tab_shuttlecock_out to 0,
                        R.string.shuttle_tab_station to 10,
                        R.string.shuttle_tab_terminal to 15,
                        R.string.shuttle_tab_shuttlecock_in to 25,
                        R.string.shuttle_type_dormitory to 30
                    )
                )
            ),
            listOf(R.string.shuttle_type_direct, R.string.shuttle_type_circular)
        )
        val shuttleJungangStationAdapter = ShuttleRealtimeByDestinationListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_header_bound_for_jungang_station,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("shuttlecock_o", R.string.shuttle_tab_shuttlecock_out, entry.seq, entry.time, entry.stops.map { it.stop to it.time })
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
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -5,
                        R.string.shuttle_tab_shuttlecock_out to 0,
                        R.string.shuttle_tab_station to 10,
                        R.string.shuttle_type_jungang to 13,
                        R.string.shuttle_tab_shuttlecock_in to 23,
                        R.string.shuttle_type_dormitory to 28
                    )
                )
            ),
            listOf(R.string.shuttle_type_jungang)
        )
        val shuttleAdapter = ShuttleRealtimeByTimeListAdapter(
            requireContext(),
            parentViewModel,
            viewLifecycleOwner,
            R.string.shuttle_tab_shuttlecock_out,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { order ->
                showAlarmDialogForStop("shuttlecock_o", R.string.shuttle_tab_shuttlecock_out, order.seq, order.time, order.stops.map { it.stop to it.time })
            }
        )

        binding.apply {
            realtimeViewBoundForStation.apply {
                adapter = shuttleStationAdapter
                layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
                addItemDecoration(decoration)
            }
            entireTimetableBoundForStation.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableStation.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
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
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_terminal
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableTerminal.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
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
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleTimetableFragment(
                    R.string.shuttle_tab_shuttlecock_out,
                    R.string.shuttle_header_bound_for_jungang_station
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableJungangStation.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
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
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_shuttlecock_out
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            stopInfo2.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_shuttlecock_in
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            infoButtonBoundForStation.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_TRANSFER_INFO)
                helpBoundForStation.visibility = if (helpBoundForStation.isVisible) View.GONE else View.VISIBLE
            }
            helpBoundForStationRecycler.apply {
                adapter = shuttleStationRouteAdapter
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
            val now = ZonedDateTime.now(ServiceClock.ZONE).toLocalTime()
            val shuttle = source.result.firstOrNull { it.name == "shuttlecock_o" }
            if (shuttle == null) {
                renderListStates()
                return@observe
            }
            val shuttleByOrder = shuttle.timetable.order.filter { it.time > now }
            val shuttleForStation = shuttle.timetable.destination.firstOrNull { it.destination == "STATION" }?.entries?.filter { it.time > now } ?: emptyList()
            val shuttleForTerminal = shuttle.timetable.destination.firstOrNull { it.destination == "TERMINAL" }?.entries?.filter { it.time > now } ?: emptyList()
            val shuttleForJungangStation = shuttle.timetable.destination.firstOrNull { it.destination == "JUNGANG" }?.entries?.filter { it.time > now } ?: emptyList()
            nextStationShuttleTime = shuttleForStation.firstOrNull()?.time
            nextTerminalShuttleTime = shuttleForTerminal.firstOrNull()?.time
            nextJungangShuttleTime = shuttleForJungangStation.firstOrNull()?.time
            // Hide the layout by showing the destination conf
            binding.shuttleDestinationScroll.visibility = if (source.showByDestination) View.VISIBLE else View.GONE
            binding.shuttleTimeScroll.visibility = if (source.showByDestination) View.GONE else View.VISIBLE
            // Update the recycler view
            shuttleAdapter.updateData(shuttleByOrder.subList(0, min(8, shuttleByOrder.size)), setOfNotNull(shuttleByOrder.lastOrNull()?.seq))
            shuttleStationAdapter.updateData(shuttleForStation.subList(0, min(3, shuttleForStation.size)), setOfNotNull(shuttleForStation.lastOrNull()?.seq))
            shuttleTerminalAdapter.updateData(shuttleForTerminal.subList(0, min(3, shuttleForTerminal.size)), setOfNotNull(shuttleForTerminal.lastOrNull()?.seq))
            shuttleJungangStationAdapter.updateData(shuttleForJungangStation.subList(0, min(3, shuttleForJungangStation.size)), setOfNotNull(shuttleForJungangStation.lastOrNull()?.seq))
            renderListStates()
        }
        parentViewModel.busAlternativeShuttlecock.observe(viewLifecycleOwner) { busMinutes ->
            updateBusAlternativeStation(busMinutes, parentViewModel.forceShowBusAlternative.value ?: false)
        }
        parentViewModel.forceShowBusAlternative.observe(viewLifecycleOwner) { forceShow ->
            updateBusAlternativeStation(parentViewModel.busAlternativeShuttlecock.value, forceShow)
        }
        parentViewModel.busAlternativeShuttlecock62.observe(viewLifecycleOwner) { busMinutes ->
            updateBusAlternative62(busMinutes)
        }
        parentViewModel.alternativeDisplayMode.observe(viewLifecycleOwner) {
            updateBusAlternativeStation(
                parentViewModel.busAlternativeShuttlecock.value,
                parentViewModel.forceShowBusAlternative.value ?: false,
            )
            updateBusAlternative62(parentViewModel.busAlternativeShuttlecock62.value)
        }

        binding.apply {
            headerBoundForDormitory.visibility = View.GONE
            realtimeViewBoundForDormitory.visibility = View.GONE
            noRealtimeDataBoundForDormitory.visibility = View.GONE
            entireTimetableBoundForDormitory.visibility = View.GONE
            entireTimetableDormitory.visibility = View.GONE
        }
        bindShuttleHelpButtons(binding.helpButton, binding.helpButton2)
        binding.applySectionSpacing()
        binding.headerBoundForStationComponent.titleView.setText(R.string.shuttle_header_bound_for_station)
        binding.headerBoundForDormitoryComponent.titleView.setText(R.string.shuttle_header_bound_for_dormitory)
        binding.headerBoundForTerminalComponent.titleView.setText(R.string.shuttle_header_bound_for_terminal)
        binding.headerBoundForJungangStationComponent.titleView.setText(R.string.shuttle_header_bound_for_jungang_station)
        parentViewModel.requestState.observe(viewLifecycleOwner) { renderListStates() }
        parentViewModel.serviceNotices.observe(viewLifecycleOwner) { renderListStates() }
        return binding.root.also { disableViewStateSaving(it) }
    }

    private fun showAlarmDialogForStop(boardingStopId: String, boardingLabelRes: Int, timetableSeq: Int, time: java.time.LocalTime, routeStops: List<Pair<String, java.time.LocalTime>>) {
        val boardingStop = parentViewModel.locationStops.value?.firstOrNull { it.name == boardingStopId } ?: return
        val now = java.time.ZonedDateTime.now()
        var departureTime = now.toLocalDate().atTime(time).atZone(java.time.ZoneId.systemDefault())
        if (departureTime.isBefore(now)) departureTime = departureTime.plusDays(1)
        val departureTimeMillis = departureTime.toInstant().toEpochMilli()
        val minutes = kotlin.math.ceil((departureTimeMillis - System.currentTimeMillis()) / 60_000.0).toInt().coerceAtLeast(0)
        val allStops = parentViewModel.locationStops.value ?: return
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

    private fun updateBusAlternativeStation(data: BusAlternativeData?, forceShow: Boolean = false) {
        val shouldShow = parentViewModel.shouldShowBusAlternative(
            data,
            nextStationShuttleTime,
            forceShow,
        )
        binding.busAlternativeStation.visibility = if (shouldShow) View.VISIBLE else View.GONE
        if (shouldShow) {
            binding.busAlternativeStationTime.text = if (data?.minutes != null)
                getString(R.string.shuttle_bus_alternative_time, data.minutes)
            else getString(R.string.shuttle_bus_alternative_no_data)
            bindBusAlternativeInfo(
                binding.busAlternativeStationInfo,
                "shuttlecock_o",
                getString(R.string.shuttle_tab_shuttlecock_out),
                data
            )
        }
    }

    private fun updateBusAlternative62(data: BusAlternativeData?) {
        val color = requireContext().getColor(R.color.green_bus)
        val showForTerminal = parentViewModel.shouldShowBusAlternative(
            data,
            nextTerminalShuttleTime,
        )
        val showForJungang = parentViewModel.shouldShowBusAlternative(
            data,
            nextJungangShuttleTime,
        )
        binding.busAlternativeTerminal.visibility = if (showForTerminal) View.VISIBLE else View.GONE
        binding.busAlternativeJungangStation.visibility = if (showForJungang) View.VISIBLE else View.GONE
        if ((showForTerminal || showForJungang) && data != null) {
            val timeText = getString(R.string.shuttle_bus_alternative_time, data.minutes)
            val routeText = getString(data.routeDisplayName)
            binding.busAccentBarTerminal.setBackgroundColor(color)
            binding.busAlternativeTerminalRoute.setTextColor(color)
            binding.busAlternativeTerminalRoute.text = routeText
            binding.busAlternativeTerminalTime.text = timeText
            binding.busAccentBarJungangStation.setBackgroundColor(color)
            binding.busAlternativeJungangStationRoute.setTextColor(color)
            binding.busAlternativeJungangStationRoute.text = routeText
            binding.busAlternativeJungangStationTime.text = timeText
            bindBusAlternativeInfo(
                binding.busAlternativeTerminalInfo,
                "shuttlecock_o",
                getString(R.string.shuttle_tab_shuttlecock_out),
                data
            )
            bindBusAlternativeInfo(
                binding.busAlternativeJungangStationInfo,
                "shuttlecock_o",
                getString(R.string.shuttle_tab_shuttlecock_out),
                data
            )
        } else {
            binding.busAlternativeTerminalInfo.isEnabled = false
            binding.busAlternativeTerminalInfo.alpha = 0.38f
            binding.busAlternativeJungangStationInfo.isEnabled = false
            binding.busAlternativeJungangStationInfo.alpha = 0.38f
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
        val shuttleStop = parentViewModel.locationStops.value?.firstOrNull { it.name == shuttleStopId }
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

    override fun onResume() {
        super.onResume()
        renderListStates()
        binding.noRealtimeData.setScreenActive(true)
        binding.noRealtimeDataBoundForStation.setScreenActive(true)
        binding.noRealtimeDataBoundForTerminal.setScreenActive(true)
        binding.noRealtimeDataBoundForJungangStation.setScreenActive(true)
    }

    override fun onPause() {
        binding.noRealtimeData.setScreenActive(false)
        binding.noRealtimeDataBoundForStation.setScreenActive(false)
        binding.noRealtimeDataBoundForTerminal.setScreenActive(false)
        binding.noRealtimeDataBoundForJungangStation.setScreenActive(false)
        super.onPause()
    }

    private fun renderListStates() {
        val byDestination = parentViewModel.showByDestination.value ?: false
        binding.shuttleDestinationScroll.isVisible = byDestination
        binding.shuttleTimeScroll.isVisible = !byDestination
        val now = ZonedDateTime.now(ServiceClock.ZONE)
        val stop = parentViewModel.result.value.orEmpty().firstOrNull { it.name == "shuttlecock_o" }
        val count = stop?.timetable?.order?.count { it.time > now.toLocalTime() } ?: 0
        renderSection(binding.noRealtimeData, binding.realtimeView, binding.entireTimetableStation, stop != null, count, !byDestination)
        renderSection(
            binding.noRealtimeDataBoundForStation, binding.realtimeViewBoundForStation, binding.entireTimetableBoundForStation,
            stop != null, stop?.timetable?.destination?.firstOrNull { it.destination == "STATION" }?.entries?.count { it.time > now.toLocalTime() } ?: 0, byDestination,
        )
        renderSection(
            binding.noRealtimeDataBoundForTerminal, binding.realtimeViewBoundForTerminal, binding.entireTimetableBoundForTerminal,
            stop != null, stop?.timetable?.destination?.firstOrNull { it.destination == "TERMINAL" }?.entries?.count { it.time > now.toLocalTime() } ?: 0, byDestination,
        )
        renderSection(
            binding.noRealtimeDataBoundForJungangStation, binding.realtimeViewBoundForJungangStation, binding.entireTimetableBoundForJungangStation,
            stop != null, stop?.timetable?.destination?.firstOrNull { it.destination == "JUNGANG" }?.entries?.count { it.time > now.toLocalTime() } ?: 0, byDestination,
        )
        val showTimeActions = !byDestination && resolveListPresentation(
            parentViewModel.requestState.value ?: RequestUiState(), stop != null, count,
            TransportMode.SHUTTLE, now, parentViewModel.haltDate(now.toLocalDate()),
        ) == ListPresentation.Content
        binding.entireTimetableStation.isVisible = showTimeActions
        binding.entireTimetableTerminal.isVisible = showTimeActions
        binding.entireTimetableJungangStation.isVisible = showTimeActions
    }

    private fun renderSection(stateView: StateView, rows: View, timetable: View, hasSource: Boolean, count: Int, active: Boolean) {
        if (!active) {
            stateView.clear()
            return
        }
        val now = ZonedDateTime.now(ServiceClock.ZONE)
        val presentation = resolveListPresentation(
            parentViewModel.requestState.value ?: RequestUiState(), hasSource, count,
            TransportMode.SHUTTLE, now, parentViewModel.haltDate(now.toLocalDate()),
        )
        rows.isVisible = presentation == ListPresentation.Content
        timetable.isVisible = presentation == ListPresentation.Content
        when (presentation) {
            is ListPresentation.State -> {
                val action = when (presentation.model.kind) {
                    StateKind.ERROR -> ({ parentViewModel.fetchData() })
                    StateKind.ENDED, StateKind.SUSPENDED -> ({ timetable.performClick(); Unit })
                    else -> null
                }
                stateView.bind(presentation.model, AnalyticsScreen.SHUTTLE_REALTIME, action)
            }
            else -> stateView.clear()
        }
    }
}
