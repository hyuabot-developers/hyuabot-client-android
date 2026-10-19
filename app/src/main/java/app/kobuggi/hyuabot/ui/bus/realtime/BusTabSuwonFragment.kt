package app.kobuggi.hyuabot.ui.bus.realtime
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.ManualRefreshDomain
import app.kobuggi.hyuabot.util.AnalyticsItem

import androidx.core.view.isVisible
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeTabBinding
import app.kobuggi.hyuabot.util.NavControllerExtension.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving

private val SUWON_STOP_BY_RES = mapOf(
    R.string.bus_stop_entrance to 216000070,
    R.string.bus_stop_suwon_station to 202000106,
)

@AndroidEntryPoint
class BusTabSuwonFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })
    private var configuredStop: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val busSecondAdapter = BusRealtimeListAdapter(maxCount = 10)
        parentViewModel.showSecondaryEta.observe(viewLifecycleOwner) {
            busSecondAdapter.setShowSecondaryEta(it)
        }
        parentViewModel.suwonStopID.observe(viewLifecycleOwner) { stopRes ->
            if (stopRes == null) return@observe
            if (configuredStop == stopRes) return@observe
            configuredStop = stopRes
            val stopSeq = SUWON_STOP_BY_RES[stopRes] ?: return@observe
            val secondaryTargetSeq = if (stopRes == R.string.bus_stop_suwon_station) 216000141 else 202000208
            val stopName = getString(stopRes)
            binding.apply {
                headerFirst.titleView.text = getString(R.string.bus_header_format, "7070/9090", stopName)
                headerFirstStopBtn.setOnClickListener {
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(stopSeq, 216000104, 200000015).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
                departureLogFirst.setOnClickListener {
                    AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(
                        stopSeq,
                        216000104,
                        200000015
                    ).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
            }
            parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                val routes = busList.filter { route -> route.stop.seq == stopSeq && (route.route.seq == 216000104 || route.route.seq == 200000015) }
                parentViewModel.reportSectionUpdatedAt(2, "first", maxRealtimeUpdatedAt(routes.flatMap { route -> route.realtime.map { it.updatedAt.toInstant() } }))
                val arrivalList = routes.flatMap { route ->
                    route.arrival.map { arrival ->
                        BusArrivalItem(
                            route.route.name,
                            arrival,
                            secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival, destinationStopID = secondaryTargetSeq),
                            destinationStopID = secondaryTargetSeq,
                            minimumDispatchMinutes = BusDispatchInterval.forToday(route.minimumDispatchIntervals),
                        )
                    }
                }
                busSecondAdapter.updateData(arrivalList.sortedBy { it.remainingMinutes ?: Double.MAX_VALUE })

            }
        }
        binding.apply {
            realtimeViewFirst.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                itemAnimator = null
            }
            entireTimetableFirst.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                val stop = busStopSequence(parentViewModel.suwonStopID.value) ?: 216000070
                findNavController().safeNavigate(
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(stop, 216000104, 200000015),
                )
            }
            headerSecondContainer.visibility = View.GONE
            realtimeViewSecond.visibility = View.GONE
            entireTimetableSecond.visibility = View.GONE
            noRealtimeDataSecond.visibility = View.GONE
            buttonLayoutSecond.visibility = View.GONE
            headerThirdContainer.visibility = View.GONE
            realtimeViewThird.visibility = View.GONE
            entireTimetableThird.visibility = View.GONE
            noRealtimeDataThird.visibility = View.GONE
            buttonLayoutThird.visibility = View.GONE
            headerFourthContainer.visibility = View.GONE
            realtimeViewFourth.visibility = View.GONE
            entireTimetableFourth.visibility = View.GONE
            noRealtimeDataFourth.visibility = View.GONE
            buttonLayoutFourth.visibility = View.GONE
            swipeRefreshLayout.setOnRefreshListener {
                AnalyticsManager.logManualRefresh(ManualRefreshDomain.BUS)
                parentViewModel.fetchData()
            }
        }
        parentViewModel.requestState.observe(viewLifecycleOwner) { request ->
            if (!request.loading) binding.swipeRefreshLayout.isRefreshing = false
            renderListStates()
        }
        parentViewModel.reportSectionUpdatedAt(2, "second", null)
        parentViewModel.reportSectionUpdatedAt(2, "third", null)
        parentViewModel.reportSectionUpdatedAt(2, "fourth", null)
        parentViewModel.result.observe(viewLifecycleOwner) { renderListStates() }
        binding.swipeRefreshLayout.setColorSchemeColors(
            com.google.android.material.color.MaterialColors.getColor(binding.root, androidx.appcompat.R.attr.colorPrimary),
        )
        return binding.root.also { disableViewStateSaving(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }

    override fun onResume() {
        super.onResume()
        renderListStates()
        binding.noRealtimeDataFirst.setScreenActive(true)
    }

    override fun onPause() {
        binding.noRealtimeDataFirst.setScreenActive(false)
        super.onPause()
    }

    private fun renderListStates() {
        val buses = parentViewModel.result.value.orEmpty()
        renderSection(
            binding.noRealtimeDataFirst, binding.realtimeViewFirst, binding.entireTimetableFirst,
            buses.any { bus -> bus.stop.seq == (busStopSequence(parentViewModel.suwonStopID.value) ?: 216000070) && bus.route.seq in setOf(216000104, 200000015) }, binding.realtimeViewFirst.adapter?.itemCount ?: 0,
        )
    }

    private fun renderSection(stateView: StateView, rows: View, timetable: View, hasSource: Boolean, count: Int) {
        val presentation = resolveListPresentation(
            parentViewModel.requestState.value ?: RequestUiState(), hasSource, count,
            TransportMode.BUS, ZonedDateTime.now(ServiceClock.ZONE),
        )
        rows.isVisible = presentation == ListPresentation.Content
        timetable.isVisible = presentation == ListPresentation.Content
        when (presentation) {
            is ListPresentation.State -> {
                val action = when (presentation.model.kind) {
                    StateKind.ERROR -> ({ parentViewModel.fetchData() })
                    StateKind.ENDED -> ({ timetable.performClick(); Unit })
                    else -> null
                }
                stateView.bind(presentation.model, AnalyticsScreen.BUS_REALTIME, action)
            }
            else -> stateView.clear()
        }
    }
}
