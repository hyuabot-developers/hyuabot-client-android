package app.kobuggi.hyuabot.ui.bus.realtime
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsItem

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

    private fun logsFor(route: Int, stop: Int) =
        parentViewModel.logResult.value?.firstOrNull { it.route.seq == route && it.stop.seq == stop }?.log ?: emptyList()

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
            val stopSeq = FIRST_SECTION_STOP_BY_RES[stopRes] ?: return@observe
            val stopName = getString(stopRes)
            binding.apply {
                headerFirstTitle.text = getString(R.string.bus_header_format, "3102", stopName)
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
                if (firstBusList == null) {
                    busFirstAdapter.updateData(emptyList())
                    binding.noRealtimeDataFirst.visibility = View.VISIBLE
                    return@observe
                }
                val secondaryTargetSeq = if (stopRes in SEOUL_REMOTE_RES_IDS) SEOUL_FIRST_RETURN_STOP else currentSeoulTargetStopID()
                val secondaryLogs = logsFor(216000061, secondaryTargetSeq)
                busFirstAdapter.updateData(firstBusList.arrival.map { arrival ->
                    BusArrivalItem(
                        firstBusList.route.name,
                        arrival,
                        BusSecondaryEta.secondaryArrivalTime(arrival, logsFor(firstBusList.route.seq, firstBusList.stop.seq), secondaryLogs)
                    )
                })
                binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        parentViewModel.seoulSecondStopID.observe(viewLifecycleOwner) { stopRes ->
            if (stopRes == null) return@observe
            val stopSeq = SECOND_SECTION_STOP_BY_RES[stopRes] ?: return@observe
            val stopName = getString(stopRes)
            val isRemote = stopRes in SEOUL_REMOTE_RES_IDS
            binding.apply {
                headerSecondTitle.text = getString(R.string.bus_header_format, "3100/3101/3100N", stopName)
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
                val arrivalList = routes.flatMap { route ->
                    val secondaryTargetSeq = if (isRemote) SEOUL_SECOND_RETURN_STOP else currentSeoulTargetStopID()
                    val secondaryLogs = logsFor(route.route.seq, secondaryTargetSeq)
                    route.arrival.map { arrival ->
                        BusArrivalItem(
                            route.route.name,
                            arrival,
                            BusSecondaryEta.secondaryArrivalTime(arrival, logsFor(route.route.seq, route.stop.seq), secondaryLogs)
                        )
                    }
                }
                busSecondAdapter.updateData(
                    arrivalList
                        .sortedBy { it.remainingMinutes ?: Double.MAX_VALUE }
                        .take(4)
                )
                binding.noRealtimeDataSecond.visibility = if (arrivalList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        binding.apply {
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            headerThird.visibility = View.GONE
            realtimeViewThird.visibility = View.GONE
            entireTimetableThird.visibility = View.GONE
            noRealtimeDataThird.visibility = View.GONE
            buttonLayoutThird.visibility = View.GONE
            headerFourth.visibility = View.GONE
            realtimeViewFourth.visibility = View.GONE
            entireTimetableFourth.visibility = View.GONE
            noRealtimeDataFourth.visibility = View.GONE
            buttonLayoutFourth.visibility = View.GONE
            swipeRefreshLayout.setOnRefreshListener {
                parentViewModel.fetchData()
            }
        }
        parentViewModel.isLoading.observe(viewLifecycleOwner) {
            if (!it) binding.swipeRefreshLayout.isRefreshing = false
        }
        return binding.root.also { disableViewStateSaving(it) }
    }

    private fun currentSeoulTargetStopID(): Int {
        return (parentViewModel.seoulTarget.value ?: BusSeoulTargetStop.GANGNAM).stopID
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }
}
