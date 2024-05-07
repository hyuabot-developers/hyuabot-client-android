package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BusTabCityFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusRealtimeViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val busFirstAdapter = BusRealtimeListAdapter(requireContext(), listOf(), listOf())
        val busSecondAdapter = BusRealtimeListAdapter(requireContext(), listOf(), listOf())
        parentViewModel.selectedStopID.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                R.string.bus_stop_convention -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_convention))
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { stop -> stop.id == 216000379 }?.routes?.firstOrNull { route -> route.info.id == 216000068 }
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
                        headerFirst.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_cluster))
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { stop -> stop.id == 216000381 }?.routes?.firstOrNull { route -> route.info.id == 216000068 }
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
                        headerFirst.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_dormitory))
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.firstOrNull { stop -> stop.id == 216000383 }?.routes?.firstOrNull { route -> route.info.id == 216000068 }
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
            val secondBusList = it.firstOrNull { stop -> stop.id == 216000138 }?.routes?.firstOrNull { route -> route.info.id == 216000068 }
            busSecondAdapter.updateData(secondBusList?.realtime ?: listOf(), secondBusList?.timetable ?: listOf())
            if (secondBusList?.realtime.isNullOrEmpty() && secondBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataSecond.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataSecond.visibility = View.GONE
            }
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_convention))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(decoration)
            }
            headerSecond.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_sangnoksu_station))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(decoration)
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
