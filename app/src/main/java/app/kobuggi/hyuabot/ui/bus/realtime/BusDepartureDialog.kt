package app.kobuggi.hyuabot.ui.bus.realtime

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.BusDepartureLogDialogQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogBusDepartureLogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class BusDepartureDialog @Inject constructor() : BottomSheetDialogFragment() {
    private val binding by lazy { DialogBusDepartureLogBinding.inflate(layoutInflater) }
    private val viewModel: BusDepartureDialogViewModel by viewModels()
    private val args : BusDepartureDialogArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val stopID = args.stopID
        val firstRouteID: Int = args.firstRouteID
        val secondRouteID: Int = args.secondRouteID
        val thirdRouteID: Int = args.thirdRouteID
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val firstLogAdapter = BusDepartureLogAdapter(requireContext(), listOf())
        val secondLogAdapter = BusDepartureLogAdapter(requireContext(), listOf())
        val thirdLogAdapter = BusDepartureLogAdapter(requireContext(), listOf())
        val queryDates: List<LocalDate> = when (today.dayOfWeek.value) {
            1 -> listOf(today.minusDays(5), today.minusDays(4), today.minusDays(3))
            2 -> listOf(today.minusDays(5), today.minusDays(4), today.minusDays(1))
            3 -> listOf(today.minusDays(5), today.minusDays(2), today.minusDays(1))
            4 -> listOf(today.minusDays(3), today.minusDays(2), today.minusDays(1))
            5 -> listOf(today.minusDays(3), today.minusDays(2), today.minusDays(1))
            6 -> listOf(today.minusDays(21), today.minusDays(14), today.minusDays(7))
            7 -> listOf(today.minusDays(21), today.minusDays(14), today.minusDays(7))
            else -> listOf(today)
        }
        val routes = listOf(firstRouteID, secondRouteID, thirdRouteID).filter { it > 0 }
        viewModel.fetchData(stopID, routes, queryDates.map { it.format(dateFormatter) })
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.bus_departure_log), Toast.LENGTH_SHORT).show() }
        }
        viewModel.result.observe(viewLifecycleOwner) {
            val logItems = mutableListOf<BusDepartureLogDialogQuery.Log>()
            it.forEach { route ->
                logItems.addAll(route.log)
            }
            val timetable1 = logItems.filter { log -> log.departureDate == queryDates[0].format(dateFormatter) }
            val timetable2 = logItems.filter { log -> log.departureDate == queryDates[1].format(dateFormatter) }
            val timetable3 = logItems.filter { log -> log.departureDate == queryDates[2].format(dateFormatter) }
            firstLogAdapter.updateData(timetable1.sortedBy { it.departureTime.toString() })
            secondLogAdapter.updateData(timetable2.sortedBy { it.departureTime.toString() })
            thirdLogAdapter.updateData(timetable3.sortedBy { it.departureTime.toString() })

            val currentTime = LocalTime.now()
            binding.busDepartureLogRecyclerView1.scrollToPosition(timetable1.indexOfFirst {
                    log -> LocalTime.parse(log.departureTime.toString().substring(0, 5)) > currentTime
                })
            binding.busDepartureLogRecyclerView2.scrollToPosition(timetable2.indexOfFirst {
                    log -> LocalTime.parse(log.departureTime.toString().substring(0, 5)) > currentTime
                })
            binding.busDepartureLogRecyclerView3.scrollToPosition(timetable3.indexOfFirst {
                    log -> LocalTime.parse(log.departureTime.toString().substring(0, 5)) > currentTime
                })
            if (firstLogAdapter.itemCount == 0) {
                binding.busDepartureLogNoData1.visibility = View.VISIBLE
            } else {
                binding.busDepartureLogNoData1.visibility = View.GONE
            }
            if (secondLogAdapter.itemCount == 0) {
                binding.busDepartureLogNoData2.visibility = View.VISIBLE
            } else {
                binding.busDepartureLogNoData2.visibility = View.GONE
            }
            if (thirdLogAdapter.itemCount == 0) {
                binding.busDepartureLogNoData3.visibility = View.VISIBLE
            } else {
                binding.busDepartureLogNoData3.visibility = View.GONE
            }
        }
        binding.apply {
            toolbar.apply {
                setOnMenuItemClickListener {
                    dismiss()
                    true
                }
            }
            busDepartureLogTitle1.text = getString(R.string.bus_departure_log_date_format, queryDates[0].monthValue, queryDates[0].dayOfMonth, getWeekdaysString(queryDates[0]))
            busDepartureLogTitle2.text = getString(R.string.bus_departure_log_date_format, queryDates[1].monthValue, queryDates[1].dayOfMonth, getWeekdaysString(queryDates[1]))
            busDepartureLogTitle3.text = getString(R.string.bus_departure_log_date_format, queryDates[2].monthValue, queryDates[2].dayOfMonth, getWeekdaysString(queryDates[2]))
            busDepartureLogRecyclerView1.apply {
                adapter = firstLogAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            busDepartureLogRecyclerView2.apply {
                adapter = secondLogAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            busDepartureLogRecyclerView3.apply {
                adapter = thirdLogAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
             behavior.isDraggable = false
        }
        return dialog
    }

    private fun getWeekdaysString(date: LocalDate): String {
        return when(date.dayOfWeek.value) {
            1 -> getString(R.string.monday)
            2 -> getString(R.string.tuesday)
            3 -> getString(R.string.wednesday)
            4 -> getString(R.string.thursday)
            5 -> getString(R.string.friday)
            6 -> getString(R.string.saturday)
            7 -> getString(R.string.sunday)
            else -> ""
        }
    }
}
