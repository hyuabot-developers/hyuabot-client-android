package app.kobuggi.hyuabot.ui.shuttle.realtime
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.LastRunJudge

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
class ShuttleTabShuttlecockInFragment @Inject constructor() : Fragment() {
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
            R.string.shuttle_tab_shuttlecock_in,
            R.string.shuttle_header_bound_for_dormitory,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { entry ->
                showAlarmDialogForStop("shuttlecock_i", R.string.shuttle_tab_shuttlecock_in, entry.seq, entry.time, entry.stops.map { it.stop to it.time })
            }
        )
        val shuttleCampusRouteAdapter = ShuttleRouteAdapter(
            listOf(
                ShuttleRouteItemView.Route(
                    color = requireContext().getColor(android.R.color.white),
                    stops = listOf(
                        R.string.shuttle_tab_dormitory_out,
                        R.string.shuttle_tab_shuttlecock_in,
                        R.string.shuttle_type_dormitory
                    ),
                    currentStopIndex = 1,
                    labels = mapOf(
                        R.string.shuttle_tab_dormitory_out to -30,
                        R.string.shuttle_tab_shuttlecock_in to 0,
                        R.string.shuttle_type_dormitory to 5,
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
            R.string.shuttle_tab_shuttlecock_in,
            childFragmentManager,
            emptyList(),
            onAlarmClick = { order ->
                showAlarmDialogForStop("shuttlecock_i", R.string.shuttle_tab_shuttlecock_in, order.seq, order.time, order.stops.map { it.stop to it.time })
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
                    R.string.shuttle_tab_shuttlecock_in,
                    R.string.shuttle_header_bound_for_dormitory
                ).also {
                    findNavController().safeNavigate(it)
                }
            }
            entireTimetableDormitory.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE)
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
            stopInfo.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL)
                ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleStopDialogFragment(
                    R.string.shuttle_tab_shuttlecock_in
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
            val shuttle = source.result.firstOrNull { it.name == "shuttlecock_i" }
            if (shuttle == null) {
                renderListStates()
                return@observe
            }
            val shuttleByOrder = shuttle.timetable.order.filter { it.time > now }
            val shuttleForCampus = shuttle.timetable.destination.firstOrNull { it.destination == "CAMPUS" }?.entries?.filter { it.time > now } ?: emptyList()
            // Hide the layout by showing the destination conf
            binding.shuttleDestinationScroll.visibility = if (source.showByDestination) View.VISIBLE else View.GONE
            binding.shuttleTimeScroll.visibility = if (source.showByDestination) View.GONE else View.VISIBLE
            // Update the recycler view
            shuttleAdapter.updateData(shuttleByOrder.subList(0, min(8, shuttleByOrder.size)), LastRunJudge.shuttleLastRunSeqs(shuttleByOrder) { it.seq })
            shuttleCampusAdapter.updateData(shuttleForCampus.subList(0, min(8, shuttleForCampus.size)), LastRunJudge.shuttleLastRunSeqs(shuttleForCampus) { it.seq })
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
            entireTimetableTerminal.visibility = View.GONE
            headerBoundForJungangStation.visibility = View.GONE
            realtimeViewBoundForJungangStation.visibility = View.GONE
            noRealtimeDataBoundForJungangStation.visibility = View.GONE
            entireTimetableBoundForJungangStation.visibility = View.GONE
            entireTimetableJungangStation.visibility = View.GONE
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
        val stop = parentViewModel.result.value.orEmpty().firstOrNull { it.name == "shuttlecock_i" }
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
