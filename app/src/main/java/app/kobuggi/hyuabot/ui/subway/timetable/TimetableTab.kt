package app.kobuggi.hyuabot.ui.subway.timetable

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentSubwayTimetableTabBinding
import app.kobuggi.hyuabot.model.subway.SubwayTimetableItemResponse
import app.kobuggi.hyuabot.util.TimetableUtil
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

class TimetableTab : Fragment() {
    companion object {
        fun newInstance(index: Int): TimetableTab {
            val bundle = Bundle(1)
            val fragment = TimetableTab()
            bundle.putInt("index", index)
            fragment.arguments = bundle
            return fragment
        }
    }
    private val binding by lazy { FragmentSubwayTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: TimetableViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val index = arguments?.getInt("index") ?: 0
        val timetable = when (index) {
            0 -> {
                parentViewModel.weekdaysTimetable
            }
            else -> {
                parentViewModel.weekendsTimetable
            }
        }
        val adapter = TimetableItemAdapter(requireContext(), listOf())
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        binding.subwayTimetableList.addItemDecoration(decoration)
        binding.subwayTimetableList.adapter = adapter
        binding.subwayTimetableList.layoutManager = LinearLayoutManager(requireContext())
        timetable.observe(viewLifecycleOwner) {
            adapter.setTimeTable(it)
            if (it.isEmpty()) {
                binding.noSubwayTimetable.visibility = View.VISIBLE
            } else {
                val now = LocalTime.now()
                binding.noSubwayTimetable.visibility = View.GONE
                binding.subwayTimetableList.smoothScrollToPosition(
                    min(it
                        .sortedBy { item -> item.departureTime }
                        .indexOfFirst { item -> item.departureTime > now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) } + 5, it.size - 1)
                )
            }
        }
        return binding.root
    }
}