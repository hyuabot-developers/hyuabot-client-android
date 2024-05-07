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
class BusTabOtherFragment @Inject constructor() : Fragment() {
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
        parentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val mainGate = it.firstOrNull { stop -> stop.id == 216000719 }?.routes
            val terminal = it.firstOrNull { stop -> stop.id == 216000759 }?.routes
            val firstBusList = mainGate?.firstOrNull { route -> route.info.id == 216000026 }
            val secondBusList = mainGate?.firstOrNull { route -> route.info.id == 216000043 }
            val thirdBusList = terminal?.firstOrNull { route -> route.info.id == 216000075 }
            busFirstAdapter.updateData(firstBusList?.realtime ?: listOf(), firstBusList?.timetable ?: listOf())
            busSecondAdapter.updateData(secondBusList?.realtime ?: listOf(), secondBusList?.timetable ?: listOf())
            busThirdAdapter.updateData(thirdBusList?.realtime ?: listOf(), thirdBusList?.timetable ?: listOf())
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
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "3100", getString(R.string.bus_stop_main_gate))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            headerSecond.text = getString(R.string.bus_header_format, "3101", getString(R.string.bus_stop_main_gate))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            headerThird.text = getString(R.string.bus_header_format, "50", getString(R.string.bus_stop_terminal))
            realtimeViewThird.apply {
                adapter = busThirdAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
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
