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
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeTabBinding
import app.kobuggi.hyuabot.util.NavControllerExtension.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving

@AndroidEntryPoint
class BusTabCityFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })
    private var configuredStop: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val busFirstAdapter = BusRealtimeListAdapter()
        val busSecondAdapter = BusRealtimeListAdapter()
        parentViewModel.showSecondaryEta.observe(viewLifecycleOwner) {
            busFirstAdapter.setShowSecondaryEta(it)
            busSecondAdapter.setShowSecondaryEta(it)
        }
        parentViewModel.selectedStopID.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            if (configuredStop == it) return@observe
            configuredStop = it
            when (it) {
                R.string.bus_stop_convention -> {
                    binding.apply {
                        headerFirst.titleView.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_convention))
                        headerFirstStopBtn.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(216000379, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        departureLogFirst.setOnClickListener {
                            AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000379, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000379, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { bus -> bus.stop.seq == 216000379 && bus.route.seq == 216000068 }
                        parentViewModel.reportSectionUpdatedAt(0, "first", busSectionUpdatedAt(firstBusList))
                        if (firstBusList == null) {
                            busFirstAdapter.updateData(emptyList())

                            return@observe
                        }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival ->
                            BusArrivalItem(
                                firstBusList.route.name,
                                arrival,
                                secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival,  destinationStopID = 216000138),
                                destinationStopID = 216000138,
                                minimumDispatchMinutes = BusDispatchInterval.forToday(firstBusList.minimumDispatchIntervals),
                            )
                        })

                    }
                }
                R.string.bus_stop_cluster -> {
                    binding.apply {
                        headerFirst.titleView.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_cluster))
                        headerFirstStopBtn.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(216000381, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        departureLogFirst.setOnClickListener {
                            AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000381, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000381, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { bus -> bus.stop.seq == 216000381 && bus.route.seq == 216000068 }
                        parentViewModel.reportSectionUpdatedAt(0, "first", busSectionUpdatedAt(firstBusList))
                        if (firstBusList == null) {
                            busFirstAdapter.updateData(emptyList())

                            return@observe
                        }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival ->
                            BusArrivalItem(
                                firstBusList.route.name,
                                arrival,
                                secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival, destinationStopID = 216000138),
                                destinationStopID = 216000138,
                                minimumDispatchMinutes = BusDispatchInterval.forToday(firstBusList.minimumDispatchIntervals),
                            )
                        })

                    }
                }
                R.string.bus_stop_dormitory -> {
                    binding.apply {
                        headerFirst.titleView.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_dormitory))
                        headerFirstStopBtn.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(216000383, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        departureLogFirst.setOnClickListener {
                            AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000383, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000383, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { bus -> bus.stop.seq == 216000383 && bus.route.seq == 216000068 }
                        parentViewModel.reportSectionUpdatedAt(0, "first", busSectionUpdatedAt(firstBusList))
                        if (firstBusList == null) {
                            busFirstAdapter.updateData(emptyList())

                            return@observe
                        }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival ->
                            BusArrivalItem(
                                firstBusList.route.name,
                                arrival,
                                secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival, destinationStopID = 216000138),
                                destinationStopID = 216000138,
                                minimumDispatchMinutes = BusDispatchInterval.forToday(firstBusList.minimumDispatchIntervals),
                            )
                        })

                    }
                }
            }
        }
        parentViewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList == null) return@observe
            val currentTime = LocalTime.now()
            val secondBusList = busList.firstOrNull { bus -> bus.stop.seq == 216000138 && bus.route.seq == 216000068 }
            parentViewModel.reportSectionUpdatedAt(0, "second", busSectionUpdatedAt(secondBusList))
            if (secondBusList == null) {
                busSecondAdapter.updateData(emptyList())

                return@observe
            }
            val secondBusRealtime = secondBusList.arrival.filter { arrival -> arrival.isRealtime }
            val secondBusTimetable = if (secondBusRealtime.isNotEmpty()) {
                secondBusList.arrival.filter { arrival ->
                    !arrival.isRealtime && arrival.arrivalTime!! > currentTime.plusMinutes(secondBusRealtime.last().minutes!!.toLong())
                }
            } else {
                secondBusList.arrival.filter { arrival -> !arrival.isRealtime }
            }
            busSecondAdapter.updateData(
                (secondBusRealtime + secondBusTimetable).map { arrival ->
                    BusArrivalItem(
                        secondBusList.route.name,
                        arrival,
                        secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival, destinationStopID = 216000378),
                        destinationStopID = 216000378,
                        minimumDispatchMinutes = BusDispatchInterval.forToday(secondBusList.minimumDispatchIntervals),
                    )
                }
            )

        }
        binding.apply {
            headerFirst.titleView.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_convention))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(decoration)
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                itemAnimator = null
            }
            headerSecond.titleView.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_sangnoksu_station))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(decoration)
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                itemAnimator = null
            }
            headerSecondStopBtn.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(216000138, 216000068).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            departureLogSecond.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000138, 216000068).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            entireTimetableSecond.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000138, 216000068).also { direction ->
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
        parentViewModel.reportSectionUpdatedAt(0, "third", null)
        parentViewModel.reportSectionUpdatedAt(0, "fourth", null)
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
            buses.any { bus -> bus.stop.seq == (busStopSequence(parentViewModel.selectedStopID.value) ?: 216000379) && bus.route.seq == 216000068 }, binding.realtimeViewFirst.adapter?.itemCount ?: 0,
        )
        renderSection(
            binding.noRealtimeDataSecond, binding.realtimeViewSecond, binding.entireTimetableSecond,
            buses.any { bus -> bus.stop.seq == 216000138 && bus.route.seq == 216000068 }, binding.realtimeViewSecond.adapter?.itemCount ?: 0,
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
