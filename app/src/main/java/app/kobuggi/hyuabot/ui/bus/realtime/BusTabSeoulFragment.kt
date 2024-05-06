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
class BusTabSeoulFragment @Inject constructor() : Fragment() {
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
        parentViewModel.selectedStopID.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                R.string.bus_stop_convention -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_convention))
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { stop -> stop.id == 216000379 }?.routes?.firstOrNull { route -> route.info.id == 216000061 }
                        busFirstAdapter.updateData(firstBusList?.realtime ?: listOf(), firstBusList?.timetable ?: listOf())
                        if (firstBusList?.realtime.isNullOrEmpty() && firstBusList?.timetable.isNullOrEmpty()) {
                            binding.noRealtimeDataFirst.visibility = View.VISIBLE
                        } else {
                            binding.noRealtimeDataFirst.visibility = View.GONE
                        }
                    }
                }
                R.string.bus_stop_cluster -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_cluster))
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { stop -> stop.id == 216000381 }?.routes?.firstOrNull { route -> route.info.id == 216000061 }
                        busFirstAdapter.updateData(firstBusList?.realtime ?: listOf(), firstBusList?.timetable ?: listOf())
                        if (firstBusList?.realtime.isNullOrEmpty() && firstBusList?.timetable.isNullOrEmpty()) {
                            binding.noRealtimeDataFirst.visibility = View.VISIBLE
                        } else {
                            binding.noRealtimeDataFirst.visibility = View.GONE
                        }
                    }
                }
                R.string.bus_stop_dormitory -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_dormitory))
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { stop -> stop.id == 216000383 }?.routes?.firstOrNull { route -> route.info.id == 216000061 }
                        busFirstAdapter.updateData(firstBusList?.realtime ?: listOf(), firstBusList?.timetable ?: listOf())
                        if (firstBusList?.realtime.isNullOrEmpty() && firstBusList?.timetable.isNullOrEmpty()) {
                            binding.noRealtimeDataFirst.visibility = View.VISIBLE
                        } else {
                            binding.noRealtimeDataFirst.visibility = View.GONE
                        }
                    }
                }
            }
        }
        parentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val mainGate = it.firstOrNull { stop -> stop.id == 216000719 }?.routes
            val secondBusList = mainGate?.firstOrNull { route -> route.info.id == 216000096 }
            busSecondAdapter.updateData(secondBusList?.realtime ?: listOf(), secondBusList?.timetable ?: listOf())
            if (secondBusList?.realtime.isNullOrEmpty() && secondBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataSecond.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataSecond.visibility = View.GONE
            }
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_convention))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            headerSecond.text = getString(R.string.bus_header_format, "3100N", getString(R.string.bus_stop_main_gate))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            headerThird.visibility = View.GONE
            realtimeViewThird.visibility = View.GONE
            entireTimetableThird.visibility = View.GONE
            noRealtimeDataThird.visibility = View.GONE
            headerFourth.visibility = View.GONE
            realtimeViewFourth.visibility = View.GONE
            entireTimetableFourth.visibility = View.GONE
            noRealtimeDataFourth.visibility = View.GONE
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
