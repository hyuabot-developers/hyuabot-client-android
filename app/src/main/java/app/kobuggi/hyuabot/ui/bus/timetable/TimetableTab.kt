package app.kobuggi.hyuabot.ui.bus.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentBusTimetableTabBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

class TimetableTab(private val index: Int) : Fragment() {
    private val binding by lazy { FragmentBusTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: TimetableViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val timetable = when (index) {
            0 -> {
                parentViewModel.weekdaysTimetable
            }
            1 -> {
                parentViewModel.saturdaysTimetable
            }
            else -> {
                parentViewModel.sundaysTimetable
            }
        }
        val adapter = TimetableItemAdapter(requireContext(), listOf())
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        binding.busTimetableList.addItemDecoration(decoration)
        binding.busTimetableList.adapter = adapter
        binding.busTimetableList.layoutManager = LinearLayoutManager(requireContext())
        timetable.observe(viewLifecycleOwner) {
            adapter.setTimeTable(it)
            if (it.isEmpty()) {
                binding.noBusTimetable.visibility = View.VISIBLE
            } else {
                val now = LocalTime.now()
                binding.noBusTimetable.visibility = View.GONE
                binding.busTimetableList.smoothScrollToPosition(
                    min(it.indexOfFirst { item -> item > now.format(DateTimeFormatter.ofPattern("HH:mm")) } + 5, it.size - 1)
                )
            }
        }
        return binding.root
    }
}