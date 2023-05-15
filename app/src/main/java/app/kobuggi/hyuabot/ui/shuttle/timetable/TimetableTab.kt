package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableTabBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
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
    private val binding by lazy { FragmentShuttleTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: TimetableViewModel by viewModels({requireParentFragment()})
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val index = arguments?.getInt("index") ?: 0
        val timetable = if (index == 0) parentViewModel.weekdaysTimetable else parentViewModel.weekendsTimetable
        val stopID = parentViewModel.stopID.value?: 0
        val adapter = TimetableItemAdapter(requireContext(), stopID, listOf())
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        binding.shuttleTimetableList.addItemDecoration(decoration)
        binding.shuttleTimetableList.adapter = adapter
        binding.shuttleTimetableList.layoutManager = LinearLayoutManager(requireContext())
        timetable.observe(viewLifecycleOwner) {
            adapter.setTimeTable(it)
            if (it.isEmpty()) {
                binding.noShuttleTimetable.visibility = View.VISIBLE
            } else {
                val now = LocalTime.now()
                binding.noShuttleTimetable.visibility = View.GONE
                binding.shuttleTimetableList.smoothScrollToPosition(
                    min(it.indexOfFirst { item -> item.departureTime > now.format(DateTimeFormatter.ofPattern("HH:mm")) } + 5, it.size - 1)
                )
            }
        }
        return binding.root
    }
}