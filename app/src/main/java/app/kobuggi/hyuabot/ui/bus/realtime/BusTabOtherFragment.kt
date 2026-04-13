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
import javax.inject.Inject

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
        parentViewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList == null) return@observe
            val firstBusList = busList.first { bus -> bus.stop.seq == 216000759 && bus.route.seq == 216000075 }
            val secondBusList = busList.first { bus -> bus.stop.seq == 213000487 && bus.route.seq == 216000075 }
            busFirstAdapter.updateData(firstBusList.arrival.map { arrival ->
                BusArrivalItem(firstBusList.route.name, arrival)
            })
            busSecondAdapter.updateData(secondBusList.arrival.filter {
                !it.isRealtime
            }.map { arrival ->
                BusArrivalItem(secondBusList.route.name, arrival)
            })
            binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
            binding.noRealtimeDataSecond.visibility = if (
                secondBusList.arrival.none { arrival -> arrival.isRealtime }
            ) View.VISIBLE else View.GONE
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "50", getString(R.string.bus_stop_terminal))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            departureLogFirst.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000759, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            entireTimetableFirst.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000759, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            headerSecond.text = getString(R.string.bus_header_format, "50", getString(R.string.bus_stop_gwangmyeong_station))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            departureLogSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(213000487, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            entireTimetableSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(213000487, 216000075).also { direction ->
                    findNavController().safeNavigate(direction)
                }
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
        return binding.root
    }
}
