package app.kobuggi.hyuabot.ui.bus.realtime

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
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class BusTabSuwonFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val busSecondAdapter = BusRealtimeListAdapter()
        parentViewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList == null) return@observe
            val routes = busList.filter { route -> route.stop.seq == 216000070 && (route.route.seq == 216000104 || route.route.seq == 200000015) }
            val arrivalList = routes.flatMap { route -> route.arrival.map { BusArrivalItem(route.route.name, it) } }
            busSecondAdapter.updateData(arrivalList.sortedWith(compareBy({ it.item.minutes ?: Int.MAX_VALUE }, { it.item.time ?: LocalTime.MAX })))
            binding.noRealtimeDataFirst.visibility = if (arrivalList.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "7070/9090", getString(R.string.bus_stop_entrance))
            realtimeViewFirst.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
            departureLogFirst.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(
                    216000070,
                    216000104,
                    200000015
                ).also { direction ->
                    findNavController().safeNavigate(direction)
                }
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
        return binding.root
    }
}
