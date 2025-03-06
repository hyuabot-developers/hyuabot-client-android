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
        parentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val terminal = it.firstOrNull { stop -> stop.id == 216000759 }?.routes
            val firstBusList = terminal?.firstOrNull { route -> route.info.id == 216000075 }
            val gwangmyeongStation = it.firstOrNull { stop -> stop.id == 213000487 }?.routes
            val secondBusList = gwangmyeongStation?.firstOrNull { route -> route.info.id == 216000075 }
            busFirstAdapter.updateData(
                firstBusList?.realtime?.map { realtimeItem ->
                    BusRealtimeItem(firstBusList.info.name, realtimeItem.sequence, realtimeItem.stop, realtimeItem.time, realtimeItem.seat, realtimeItem.lowFloor, realtimeItem.updatedAt)
                } ?: listOf(),
                firstBusList?.timetable?.map { timetableItem ->
                    BusTimetableItem(firstBusList.info.name, timetableItem.weekdays, timetableItem.time)
                } ?: listOf()
            )
            busSecondAdapter.updateData(
                listOf(),
                secondBusList?.timetable?.map { timetableItem ->
                    BusTimetableItem(secondBusList.info.name, timetableItem.weekdays, timetableItem.time)
                } ?: listOf()
            )
            if (firstBusList?.realtime.isNullOrEmpty() && firstBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataFirst.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataFirst.visibility = View.GONE
            }
            if (secondBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataSecond.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataSecond.visibility = View.GONE
            }
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "50", getString(R.string.bus_stop_terminal))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            departureLogFirst.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000719, 216000026).also { direction ->
                    findNavController().navigate(direction)
                }
            }
            entireTimetableFirst.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000719, 216000026).also { direction ->
                    findNavController().navigate(direction)
                }
            }
            headerSecond.text = getString(R.string.bus_header_format, "50", getString(R.string.bus_stop_gwangmyeong_station))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            entireTimetableSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000719, 213000487).also { direction ->
                    findNavController().navigate(direction)
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
