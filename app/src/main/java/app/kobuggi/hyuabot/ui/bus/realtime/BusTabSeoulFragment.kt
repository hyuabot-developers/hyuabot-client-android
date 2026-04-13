package app.kobuggi.hyuabot.ui.bus.realtime

import android.annotation.SuppressLint
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
class BusTabSeoulFragment @Inject constructor() : Fragment() {
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
        parentViewModel.selectedStopID.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                R.string.bus_stop_convention -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_convention))
                        departureLogFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000379, 216000061).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(216000379, 216000061).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.first { bus -> bus.stop.seq == 216000379 && bus.route.seq == 216000061 }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival -> BusArrivalItem(firstBusList.route.name, arrival) })
                        binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                R.string.bus_stop_cluster -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_cluster))
                        departureLogFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000381, 216000061).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(
                                216000381,
                                216000061
                            ).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.first { bus -> bus.stop.seq == 216000381 && bus.route.seq == 216000061 }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival -> BusArrivalItem(firstBusList.route.name, arrival) })
                        binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                R.string.bus_stop_dormitory -> {
                    binding.apply {
                        headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_dormitory))
                        departureLogFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(216000383, 216000061).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                        entireTimetableFirst.setOnClickListener {
                            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(
                                216000383,
                                216000061
                            ).also { direction ->
                                findNavController().safeNavigate(direction)
                            }
                        }
                    }
                    parentViewModel.result.observe(viewLifecycleOwner) { busList ->
                        val firstBusList = busList.first { bus -> bus.stop.seq == 216000383 && bus.route.seq == 216000061 }
                        busFirstAdapter.updateData(firstBusList.arrival.map { arrival -> BusArrivalItem(firstBusList.route.name, arrival) })
                        binding.noRealtimeDataFirst.visibility = if (firstBusList.arrival.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
        parentViewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList == null) return@observe
            val routes = busList.filter { route -> route.stop.seq == 216000719 && (route.route.seq == 216000096 || route.route.seq == 216000026 || route.route.seq == 216000043) }
            val arrivalList = routes.flatMap { route -> route.arrival.map { BusArrivalItem(route.route.name, it) } }
            busSecondAdapter.updateData(arrivalList.sortedWith(compareBy(
                { it.item.minutes ?: Int.MAX_VALUE },
                { it.item.time?.let { time ->
                    add24HoursAfterMidnight(time)
                }}
            )))
            binding.noRealtimeDataSecond.visibility = if (arrivalList.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.apply {
            headerFirst.text = getString(R.string.bus_header_format, "3102", getString(R.string.bus_stop_convention))
            realtimeViewFirst.apply {
                adapter = busFirstAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            headerSecond.text = getString(R.string.bus_header_format, "3100/3101/3101N", getString(R.string.bus_stop_main_gate))
            realtimeViewSecond.apply {
                adapter = busSecondAdapter
                addItemDecoration(decoration)
                layoutManager = LinearLayoutManager(requireContext())
            }
            departureLogSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusDepartureLogDialogFragment(
                    216000719,
                    216000096,
                    216000026,
                    216000043
                ).also { direction ->
                    findNavController().safeNavigate(direction)
                }
            }
            entireTimetableSecond.setOnClickListener {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment(
                    216000719, 216000096, 216000026, 216000043
                ).also { direction ->
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

    @SuppressLint("DefaultLocale")
    private fun add24HoursAfterMidnight(time: LocalTime): String {
        val hour = time.hour
        val minute = time.minute
        val second = time.second
        return if (hour < 5) {
            String.format("%02d:%02d:%02d", hour + 24, minute, second)
        } else {
            String.format("%02d:%02d:%02d", hour, minute, second)
        }
    }
}
