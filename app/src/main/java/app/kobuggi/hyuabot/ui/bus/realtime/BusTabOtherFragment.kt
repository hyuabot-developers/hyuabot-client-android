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
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving

@AndroidEntryPoint
class BusTabOtherFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val busFirstAdapter = BusRealtimeListAdapter()
        val busSecondAdapter = BusRealtimeListAdapter()
        parentViewModel.showSecondaryEta.observe(viewLifecycleOwner) {
            busFirstAdapter.setShowSecondaryEta(it)
            busSecondAdapter.setShowSecondaryEta(it)
        }
        parentViewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList == null) return@observe
            val firstBusList = busList.firstOrNull { bus -> bus.stop.seq == 216000759 && bus.route.seq == 216000075 }
            val secondBusList = busList.firstOrNull { bus -> bus.stop.seq == 213000487 && bus.route.seq == 216000075 }
            parentViewModel.reportSectionUpdatedAt(3, "first", busSectionUpdatedAt(firstBusList))
            parentViewModel.reportSectionUpdatedAt(3, "second", busSectionUpdatedAt(secondBusList))
            if (firstBusList == null) {
                busFirstAdapter.updateData(emptyList())

            } else {
                busFirstAdapter.updateData(firstBusList.arrival.map { arrival ->
                    BusArrivalItem(
                        firstBusList.route.name,
                        arrival,
                        secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival, destinationStopID = 213000487),
                        destinationStopID = 213000487,
                        minimumDispatchMinutes = BusDispatchInterval.forToday(firstBusList.minimumDispatchIntervals),
                    )
                })

            }
            if (secondBusList == null) {
                busSecondAdapter.updateData(emptyList())

            } else {
                busSecondAdapter.updateData(secondBusList.arrival.map { arrival ->
                    BusArrivalItem(
                        secondBusList.route.name,
                        arrival,
                        secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival, destinationStopID = 216000117),
                        destinationStopID = 216000117,
                        minimumDispatchMinutes = BusDispatchInterval.forToday(secondBusList.minimumDispatchIntervals),
                    )
                })

            }
        }
        binding.apply {
            headerFirst.titleView.text = getString(R.string.bus_header_format, "50", getString(R.string.bus_stop_terminal))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                itemAnimator = null
            }
            headerFirstStopBtn.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(216000759, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            departureLogFirst.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000759, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            entireTimetableFirst.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000759, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            headerSecond.titleView.text = getString(R.string.bus_header_format, "50", getString(R.string.bus_stop_gwangmyeong_station))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                itemAnimator = null
            }
            headerSecondStopBtn.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(213000487, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            departureLogSecond.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(213000487, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            entireTimetableSecond.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(213000487, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
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
        parentViewModel.reportSectionUpdatedAt(3, "third", null)
        parentViewModel.reportSectionUpdatedAt(3, "fourth", null)
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
        binding.noRealtimeDataSecond.setScreenActive(true)
    }

    override fun onPause() {
        binding.noRealtimeDataFirst.setScreenActive(false)
        binding.noRealtimeDataSecond.setScreenActive(false)
        super.onPause()
    }

    private fun renderListStates() {
        val buses = parentViewModel.result.value.orEmpty()
        renderSection(
            binding.noRealtimeDataFirst, binding.realtimeViewFirst, binding.entireTimetableFirst,
            buses.any { bus -> bus.stop.seq == 216000759 && bus.route.seq == 216000075 }, binding.realtimeViewFirst.adapter?.itemCount ?: 0,
        )
        renderSection(
            binding.noRealtimeDataSecond, binding.realtimeViewSecond, binding.entireTimetableSecond,
            buses.any { bus -> bus.stop.seq == 213000487 && bus.route.seq == 216000075 }, binding.realtimeViewSecond.adapter?.itemCount ?: 0,
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
