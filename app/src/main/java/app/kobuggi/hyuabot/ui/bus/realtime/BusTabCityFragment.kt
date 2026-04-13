package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeTabBinding
import app.kobuggi.hyuabot.util.NavControllerExtension.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
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
        val busFirstAdapter = BusRealtimeListAdapter()
        val busSecondAdapter = BusRealtimeListAdapter()
        parentViewModel.selectedStopID.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                R.string.bus_stop_convention -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_convention))
                        departureLogFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000379, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000379, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.first { bus -> bus.stop.seq == 216000379 && bus.route.seq == 216000068 }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival -> BusArrivalItem(firstBusList.route.name, arrival) })
                        binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                R.string.bus_stop_cluster -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_cluster))
                        departureLogFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000381, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000381, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.first { bus -> bus.stop.seq == 216000381 && bus.route.seq == 216000068 }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival -> BusArrivalItem(firstBusList.route.name, arrival) })
                        binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                R.string.bus_stop_dormitory -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "10-1", getString(R.string.bus_stop_dormitory))
                        departureLogFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000383, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000383, 216000068).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.first { bus -> bus.stop.seq == 216000383 && bus.route.seq == 216000068 }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival -> BusArrivalItem(firstBusList.route.name, arrival) })
                        binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
        parentViewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList == null) return@observe
            val currentTime = LocalTime.now()
            val secondBusList = busList.first { bus -> bus.stop.seq == 216000138 && bus.route.seq == 216000068 }
            val secondBusRealtime = secondBusList.arrival.filter { arrival -> arrival.isRealtime }
            val secondBusTimetable = if (secondBusRealtime.isNotEmpty()) {
                secondBusList.arrival.filter { arrival ->
                    !arrival.isRealtime && arrival.time!! > currentTime.plusMinutes(secondBusRealtime.last().minutes!!.toLong())
                }
            } else {
                secondBusList.arrival.filter { arrival -> !arrival.isRealtime }
            }
            busSecondAdapter.updateData(
                (secondBusRealtime + secondBusTimetable).map {
                    arrival -> BusArrivalItem(secondBusList.route.name, arrival)
                }
            )
            binding.noRealtimeDataSecond.visibility = if (secondBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
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
            departureLogSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000138, 216000068).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            entireTimetableSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000138, 216000068).also { direction ->
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
