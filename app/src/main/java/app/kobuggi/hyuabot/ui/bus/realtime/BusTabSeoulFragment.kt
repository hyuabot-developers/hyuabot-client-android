package app.kobuggi.hyuabot.ui.bus.realtime
import app.kobuggi.hyuabot.util.AnalyticsManager
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

private val FIRST_SECTION_STOP_BY_RES = mapOf(
    R.string.bus_stop_convention to 216000379,
    R.string.bus_stop_cluster to 216000381,
    R.string.bus_stop_dormitory to 216000383,
    R.string.bus_stop_seocho to 121000060,
    R.string.bus_stop_gyodae to 121000929,
    R.string.bus_stop_gangnam to 121000974,
    R.string.bus_stop_yangjae to 121000970,
    R.string.bus_stop_yangjae_forest to 121000220,
)
private val SECOND_SECTION_STOP_BY_RES = mapOf(
    R.string.bus_stop_main_gate to 216000719,
    R.string.bus_stop_seocho to 121000060,
    R.string.bus_stop_gyodae to 121000929,
    R.string.bus_stop_gangnam to 121000974,
    R.string.bus_stop_yangjae to 121000970,
    R.string.bus_stop_yangjae_forest to 121000220,
)
private val SEOUL_REMOTE_RES_IDS = setOf(
    R.string.bus_stop_seocho,
    R.string.bus_stop_gyodae,
    R.string.bus_stop_gangnam,
    R.string.bus_stop_yangjae,
    R.string.bus_stop_yangjae_forest,
)
// 3102's own return leg (Seoul-bound stop -> campus) uses a distinct stop from its outbound one.
private const val SEOUL_FIRST_RETURN_STOP = 216000378
// 3100/3101/3100N never stop at 216000379 (that's 3102-only); their return leg uses 216000048.
private const val SEOUL_SECOND_RETURN_STOP = 216000048

@AndroidEntryPoint
class BusTabSeoulFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })
    private var configuredFirstStop: Int? = null
    private var configuredSecondStop: Int? = null

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
        parentViewModel.seoulFirstStopID.observe(viewLifecycleOwner) { stopRes ->
            if (stopRes == null) return@observe
            if (configuredFirstStop == stopRes) return@observe
            configuredFirstStop = stopRes
            val stopSeq = FIRST_SECTION_STOP_BY_RES[stopRes] ?: return@observe
            val stopName = getString(stopRes)
            binding.apply {
                headerFirst.titleView.text = getString(R.string.bus_header_format, "3102", stopName)
                headerFirstStopBtn.setOnClickListener {
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(stopSeq, 216000061).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
                departureLogFirst.setOnClickListener {
                    AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(stopSeq, 216000061).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
                entireTimetableFirst.setOnClickListener {
                    AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(stopSeq, 216000061).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
            }
            parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                val firstBusList = busList.firstOrNull { bus -> bus.stop.seq == stopSeq && bus.route.seq == 216000061 }
                parentViewModel.reportSectionUpdatedAt(1, "first", busSectionUpdatedAt(firstBusList))
                if (firstBusList == null) {
                    busFirstAdapter.updateData(emptyList())

                    return@observe
                }
                val secondaryTargetSeq = if (stopRes in SEOUL_REMOTE_RES_IDS) SEOUL_FIRST_RETURN_STOP else currentSeoulTargetStopID()
                busFirstAdapter.updateData(firstBusList.arrival.map { arrival ->
                    BusArrivalItem(
                        firstBusList.route.name,
                        arrival,
                        secondaryArrivalTime = BusSecondaryEta.secondaryArrivalTime(arrival, destinationStopID = secondaryTargetSeq),
                        destinationStopID = secondaryTargetSeq,
                        minimumDispatchMinutes = BusDispatchInterval.forToday(firstBusList.minimumDispatchIntervals),
                    )
                })

            }
        }
        parentViewModel.seoulSecondStopID.observe(viewLifecycleOwner) { stopRes ->
            if (stopRes == null) return@observe
            if (configuredSecondStop == stopRes) return@observe
            configuredSecondStop = stopRes
            val stopSeq = SECOND_SECTION_STOP_BY_RES[stopRes] ?: return@observe
            val stopName = getString(stopRes)
            val isRemote = stopRes in SEOUL_REMOTE_RES_IDS
            binding.apply {
                headerSecond.titleView.text = getString(R.string.bus_header_format, "3100/3101/3100N", stopName)
                headerSecondStopBtn.setOnClickListener {
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopInfoFragment(
                        stopSeq, 216000096, 216000026, 216000043
                    ).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
                departureLogSecond.setOnClickListener {
                    AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_DEPARTURE_LOG)
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(
                        stopSeq, 216000096, 216000026, 216000043
                    ).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
                entireTimetableSecond.setOnClickListener {
                    AnalyticsManager.logSelect(AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE)
                    BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(
                        stopSeq, 216000096, 216000026, 216000043
                    ).also { direction ->
                        findNavController().safeNavigate(direction)
                    }
                }
            }
            parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                val routes = busList.filter { route ->
                    route.stop.seq == stopSeq && (route.route.seq == 216000096 || route.route.seq == 216000026 || route.route.seq == 216000043)
                }
                parentViewModel.reportSectionUpdatedAt(1, "second", maxRealtimeUpdatedAt(routes.flatMap { route -> route.realtime.map { it.updatedAt.toInstant() } }))
                val arrivalList = routes.flatMap { route ->
                    val secondaryTargetSeq = if (isRemote) SEOUL_SECOND_RETURN_STOP else currentSeoulTargetStopID()
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
                busSecondAdapter.updateData(
                    arrivalList
                        .sortedBy { it.remainingMinutes ?: Double.MAX_VALUE }
                        .take(4)
                )

            }
        }
        binding.apply {
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                itemAnimator = null
            }
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                itemAnimator = null
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
                parentViewModel.fetchData()
            }
        }
        parentViewModel.requestState.observe(viewLifecycleOwner) { request ->
            if (!request.loading) binding.swipeRefreshLayout.isRefreshing = false
            renderListStates()
        }
        parentViewModel.reportSectionUpdatedAt(1, "third", null)
        parentViewModel.reportSectionUpdatedAt(1, "fourth", null)
        parentViewModel.result.observe(viewLifecycleOwner) { renderListStates() }
        binding.swipeRefreshLayout.setColorSchemeColors(
            com.google.android.material.color.MaterialColors.getColor(binding.root, androidx.appcompat.R.attr.colorPrimary),
        )
        return binding.root.also { disableViewStateSaving(it) }
    }

    private fun currentSeoulTargetStopID(): Int {
        return (parentViewModel.seoulTarget.value ?: BusSeoulTargetStop.GANGNAM).stopID
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
            buses.any { bus -> bus.stop.seq == (busStopSequence(parentViewModel.seoulFirstStopID.value) ?: 216000379) && bus.route.seq == 216000061 }, binding.realtimeViewFirst.adapter?.itemCount ?: 0,
        )
        renderSection(
            binding.noRealtimeDataSecond, binding.realtimeViewSecond, binding.entireTimetableSecond,
            buses.any { bus -> bus.stop.seq == (busStopSequence(parentViewModel.seoulSecondStopID.value) ?: 216000719) && bus.route.seq in setOf(216000043, 216000026, 216000096) }, binding.realtimeViewSecond.adapter?.itemCount ?: 0,
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
