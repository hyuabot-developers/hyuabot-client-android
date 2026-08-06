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

private val SUWON_STOP_BY_RES = mapOf(
    R.string.bus_stop_entrance to 216000070,
    R.string.bus_stop_suwon_station to 202000106,
)

@AndroidEntryPoint
class BusTabSuwonFragment @Inject constructor() : Fragment() {
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
        val busSecondAdapter = BusRealtimeListAdapter(maxCount = 10)
        parentViewModel.showSecondaryEta.observe(viewLifecycleOwner) {
            busSecondAdapter.setShowSecondaryEta(it)
        }
        parentViewModel.suwonStopID.observe(viewLifecycleOwner) { stopRes ->
            if (stopRes == null) return@observe
            val stopSeq = SUWON_STOP_BY_RES[stopRes] ?: return@observe
            val secondaryTargetSeq = if (stopRes == R.string.bus_stop_suwon_station) 216000141 else 202000208
            val stopName = getString(stopRes)
            binding.apply {
                headerFirstTitle.text = getString(R.string.bus_header_format, "7070/9090", stopName)
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
                val arrivalList = routes.flatMap { route ->
                    val secondaryLogs = logsFor(route.route.seq, secondaryTargetSeq)
                    route.arrival.map { arrival ->
                        BusArrivalItem(
                            route.route.name,
                            arrival,
                            BusSecondaryEta.secondaryArrivalTime(arrival, logsFor(route.route.seq, route.stop.seq), secondaryLogs)
                        )
                    }
                }
                busSecondAdapter.updateData(arrivalList.sortedBy { it.remainingMinutes ?: Double.MAX_VALUE })
                binding.noRealtimeDataFirst.visibility = if (arrivalList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        binding.apply {
            realtimeViewFirst.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
            entireTimetableFirst.isEnabled = false
            headerSecond.visibility = View.GONE
            realtimeViewSecond.visibility = View.GONE
            entireTimetableSecond.visibility = View.GONE
            noRealtimeDataSecond.visibility = View.GONE
            buttonLayoutSecond.visibility = View.GONE
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

    override fun onSaveInstanceState(outState: Bundle) {
    }
}
