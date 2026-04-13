package app.kobuggi.hyuabot.ui.subway.timetable

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentSubwayTimetableTabBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class SubwayTabWeekdaysFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayTimetableViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val timetableAdapter = SubwayTimetableListAdapter(requireContext())
        parentViewModel.timetable.observe(viewLifecycleOwner) {
            timetableAdapter.updateData(
                it.filter {
                    item -> item.weekday == "weekdays"
                }.map {
                    item -> SubwayTimetableItem(
                        weekday = item.weekday,
                        direction = item.direction,
                        time = add24HoursAfterMidnight(item.time),
                        terminal = item.terminal,
                    )
                }.sortedBy { item -> item.time },
            )
        }
        binding.apply {
            timetableRecyclerView.apply {
                adapter = timetableAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(decoration)
            }
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
