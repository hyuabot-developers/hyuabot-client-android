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
class ShuttleTabJungangStationFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleRealtimeViewModel by viewModels({ requireParentFragment() })
    private var nextCampusShuttleTime: LocalTime? = null

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
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("jungang_stn", R.string.shuttle_tab_jungang_station, entry.seq, entry.time, entry.stops.map { it.stop to it.time })
            }
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
            emptyList(),
            onAlarmClick = { order ->
                showAlarmDialogForStop("jungang_stn", R.string.shuttle_tab_jungang_station, order.seq, order.time, order.stops.map { it.stop to it.time })
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
            val now = ZonedDateTime.now(ServiceClock.ZONE).toLocalTime()
            val shuttle = source.result.firstOrNull { it.name == "jungang_stn" }
            if (shuttle == null) {
                renderListStates()
                return@observe
            }
            val shuttleByOrder = shuttle.timetable.order.filter { it.time > now }
            val shuttleForCampus = shuttle.timetable.destination.firstOrNull { it.destination == "CAMPUS" }?.entries?.filter { it.time > now } ?: emptyList()
            nextCampusShuttleTime = shuttleForCampus.firstOrNull()?.time
            // Hide the layout by showing the destination conf
            binding.shuttleDestinationScroll.visibility = if (source.showByDestination) View.VISIBLE else View.GONE
            binding.shuttleTimeScroll.visibility = if (source.showByDestination) View.GONE else View.VISIBLE
            // Update the recycler view
            shuttleAdapter.updateData(shuttleByOrder.subList(0, min(8, shuttleByOrder.size)), setOfNotNull(shuttleByOrder.lastOrNull()?.seq))
            shuttleCampusAdapter.updateData(shuttleForCampus.subList(0, min(8, shuttleForCampus.size)), setOfNotNull(shuttleForCampus.lastOrNull()?.seq))
            renderListStates()
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
        parentViewModel.alternativeDisplayMode.observe(viewLifecycleOwner) {
            updateBusAlternativeDormitory(
                parentViewModel.busAlternativeJungang80.value,
                parentViewModel.busAlternativeJungang62.value,
            )
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

    private fun updateBusAlternativeDormitory(data80: BusAlternativeData?, data62: BusAlternativeData?) {
        val blueColor = requireContext().getColor(R.color.blue_bus)
        val greenColor = requireContext().getColor(R.color.green_bus)

        val shouldShow80 = parentViewModel.shouldShowBusAlternative(data80, nextCampusShuttleTime)
        binding.busAlternativeDormitory.visibility = if (shouldShow80) View.VISIBLE else View.GONE
        if (shouldShow80 && data80 != null) {
            binding.busAccentBarDormitory.setBackgroundColor(blueColor)
            binding.busAlternativeDormitoryRoute.setTextColor(blueColor)
            binding.busAlternativeDormitoryRoute.text = getString(data80.routeDisplayName)
            binding.busAlternativeDormitoryTime.text = getString(R.string.shuttle_bus_alternative_time, data80.minutes)

            bindBusAlternativeInfo(
                binding.busAlternativeDormitoryInfo,
                "jungang_stn",
                getString(R.string.shuttle_tab_jungang_station),
                data80
            )
        } else {
            binding.busAlternativeDormitoryInfo.isEnabled = false
            binding.busAlternativeDormitoryInfo.alpha = 0.38f
        }

        val shouldShow62 = parentViewModel.shouldShowBusAlternative(data62, nextCampusShuttleTime)
        binding.busAlternativeDormitory2.visibility = if (shouldShow62) View.VISIBLE else View.GONE
        if (shouldShow62 && data62 != null) {
            binding.busAccentBarDormitory2.setBackgroundColor(greenColor)
            binding.busAlternativeDormitory2Route.setTextColor(greenColor)
            binding.busAlternativeDormitory2Route.text = getString(data62.routeDisplayName)
            binding.busAlternativeDormitory2Time.text = getString(R.string.shuttle_bus_alternative_time, data62.minutes)
            bindBusAlternativeInfo(
                binding.busAlternativeDormitory2Info,
                "jungang_stn",
                getString(R.string.shuttle_tab_jungang_station),
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
        binding.noRealtimeDataBoundForDormitory.setScreenActive(true)
    }

    override fun onPause() {
        binding.noRealtimeData.setScreenActive(false)
        binding.noRealtimeDataBoundForDormitory.setScreenActive(false)
        super.onPause()
    }

    private fun renderListStates() {
        val byDestination = parentViewModel.showByDestination.value ?: false
        binding.shuttleDestinationScroll.isVisible = byDestination
        binding.shuttleTimeScroll.isVisible = !byDestination
        val now = ZonedDateTime.now(ServiceClock.ZONE)
        val stop = parentViewModel.result.value.orEmpty().firstOrNull { it.name == "jungang_stn" }
        val count = stop?.timetable?.order?.count { it.time > now.toLocalTime() } ?: 0
        renderSection(binding.noRealtimeData, binding.realtimeView, binding.entireTimetableDormitory, stop != null, count, !byDestination)
        renderSection(
            binding.noRealtimeDataBoundForDormitory, binding.realtimeViewBoundForDormitory, binding.entireTimetableBoundForDormitory,
            stop != null, stop?.timetable?.destination?.firstOrNull { it.destination == "CAMPUS" }?.entries?.count { it.time > now.toLocalTime() } ?: 0, byDestination,
        )
        val showTimeActions = !byDestination && resolveListPresentation(
            parentViewModel.requestState.value ?: RequestUiState(), stop != null, count,
            TransportMode.SHUTTLE, now, parentViewModel.haltDate(now.toLocalDate()),
        ) == ListPresentation.Content
        binding.entireTimetableDormitory.isVisible = showTimeActions
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
