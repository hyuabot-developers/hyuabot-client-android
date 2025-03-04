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
        val busSecondAdapter = BusRealtimeListAdapter(requireContext(), listOf(), listOf(), true)
        parentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val mainGate = it.firstOrNull { stop -> stop.id == 216000719 }?.routes
            val entrance = it.firstOrNull { stop -> stop.id == 216000070 }?.routes
            val firstBusList = mainGate?.firstOrNull { route -> route.info.id == 216000070 }
            val secondBusList = entrance?.filter { route -> route.info.id == 217000014 || route.info.id == 216000104 || route.info.id == 200000015 }
            val realtimeList = mutableListOf<BusRealtimeItem>()
            val timetableList = mutableListOf<BusTimetableItem>()
            secondBusList?.forEach { route ->
                realtimeList.addAll(route.realtime.map { realtimeItem ->
                    BusRealtimeItem(route.info.name, realtimeItem.sequence, realtimeItem.stop, realtimeItem.time, realtimeItem.seat, realtimeItem.lowFloor, realtimeItem.updatedAt)
                })
                timetableList.addAll(route.timetable.map { timetableItem ->
                    BusTimetableItem(route.info.name, timetableItem.weekdays, timetableItem.time)
                })
            }
            busFirstAdapter.updateData(
                firstBusList?.realtime?.map { realtimeItem ->
                    BusRealtimeItem(firstBusList.info.name, realtimeItem.sequence, realtimeItem.stop, realtimeItem.time, realtimeItem.seat, realtimeItem.lowFloor, realtimeItem.updatedAt)
                } ?: listOf(),
                firstBusList?.timetable?.map { timetableItem ->
                    BusTimetableItem(firstBusList.info.name, timetableItem.weekdays, timetableItem.time)
                } ?: listOf()
            )
            busSecondAdapter.updateData(
                realtimeList.sortedBy { it.time },
                timetableList.sortedBy { it.time }
            )
            if (firstBusList?.realtime.isNullOrEmpty() && firstBusList?.timetable.isNullOrEmpty()) {
                binding.noRealtimeDataFirst.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataFirst.visibility = View.GONE
            }
            if (realtimeList.isEmpty() && timetableList.isEmpty()) {
                binding.noRealtimeDataSecond.visibility = View.VISIBLE
            } else {
                binding.noRealtimeDataSecond.visibility = View.GONE
            }
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "707-1", getString(R.string.bus_stop_main_gate))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
            departureLogFirst.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000719, 216000070).also { direction ->
                    findNavController().navigate(direction)
                }
            }
            entireTimetableFirst.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000719, 216000070).also { direction ->
                    findNavController().navigate(direction)
                }
            }
            headerSecond.text = getString(R.string.bus_header_format, "110/7070/9090", getString(R.string.bus_stop_entrance))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(context)
            }
            departureLogSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000070, 217000014).also { direction ->
                    findNavController().navigate(direction)
                }
            }
            entireTimetableSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000070, 217000014).also { direction ->
                    findNavController().navigate(direction)
                }
            }
            entireTimetableSecond.isEnabled = false
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
