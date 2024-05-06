package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeTabBinding
import dagger.hilt.android.AndroidEntryPoint
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
        val busFirstAdapter = BusRealtimeListAdapter(requireContext(), listOf(), listOf())
        val busSecondAdapter = BusRealtimeListAdapter(requireContext(), listOf(), listOf())
        val busThirdAdapter = BusRealtimeListAdapter(requireContext(), listOf(), listOf())
        val busFourthAdapter = BusRealtimeListAdapter(requireContext(), listOf(), listOf(), true)
        parentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val mainGate = it.firstOrNull { stop -> stop.id == 216000719 }?.routes
            val entrance = it.firstOrNull { stop -> stop.id == 216000070 }?.routes
            val firstBusList = mainGate?.firstOrNull { route -> route.info.id == 216000070 }
            val secondBusList = entrance?.firstOrNull { route -> route.info.id == 217000014 }
            val thirdBusList = entrance?.firstOrNull { route -> route.info.id == 216000104 }
            val fourthBusList = entrance?.firstOrNull { route -> route.info.id == 200000015 }
            busFirstAdapter.updateData(firstBusList?.realtime ?: listOf(), firstBusList?.timetable ?: listOf())
            busSecondAdapter.updateData(secondBusList?.realtime ?: listOf(), secondBusList?.timetable ?: listOf())
            busThirdAdapter.updateData(thirdBusList?.realtime ?: listOf(), thirdBusList?.timetable ?: listOf())
            busFourthAdapter.updateData(fourthBusList?.realtime ?: listOf(), fourthBusList?.timetable ?: listOf())
            if (firstBusList?.realtime.isNullOrEmpty() && firstBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataFirst.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataFirst.visibility = View.GONE
            }
            if (secondBusList?.realtime.isNullOrEmpty() && secondBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataSecond.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataSecond.visibility = View.GONE
            }
            if (thirdBusList?.realtime.isNullOrEmpty() && thirdBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataThird.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataThird.visibility = View.GONE
            }
            if (fourthBusList?.realtime.isNullOrEmpty() && fourthBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataFourth.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataFourth.visibility = View.GONE
            }
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "707-1", getString(R.string.bus_stop_main_gate))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
            headerSecond.text = getString(R.string.bus_header_format, "110", getString(R.string.bus_stop_entrance))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
            headerThird.text = getString(R.string.bus_header_format, "7070", getString(R.string.bus_stop_entrance))
            realtimeViewThird.apply {
                adapter = busThirdAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
            headerFourth.text = getString(R.string.bus_header_format, "9090", getString(R.string.bus_stop_entrance))
            realtimeViewFourth.apply {
                adapter = busFourthAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
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
