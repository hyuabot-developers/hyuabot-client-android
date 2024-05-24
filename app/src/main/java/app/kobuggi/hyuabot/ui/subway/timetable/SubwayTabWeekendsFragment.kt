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
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class SubwayTabWeekendsFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: SubwayTimetableViewModel by viewModels({ requireParentFragment() })
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        if (parentViewModel.heading.value == "up") {
            val timetableAdapter = SubwayTimetableListAdapter(requireContext(), listOf(), null)
            parentViewModel.up.observe(viewLifecycleOwner) {
                timetableAdapter.updateData(
                    it.filter {
                            item -> item.weekdays
                    }.map {
                            item -> item.copy(
                        time = add24HoursAfterMidnight(item.time)
                    )
                    }.sortedBy { item -> item.time },
                    null
                )
            }
            binding.apply {
                timetableRecyclerView.apply {
                    adapter = timetableAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(decoration)
                }
            }
        } else {
            val timetableAdapter = SubwayTimetableListAdapter(requireContext(), null, listOf())
            parentViewModel.down.observe(viewLifecycleOwner) {
                timetableAdapter.updateData(
                    null,
                    it.filter {
                            item -> !item.weekdays
                    }.map {
                            item -> item.copy(
                        time = add24HoursAfterMidnight(item.time)
                    )
                    }.sortedBy {
                            item -> item.time
                    }
                )
            }
            binding.apply {
                timetableRecyclerView.apply {
                    adapter = timetableAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(decoration)
                }
            }
        }
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun add24HoursAfterMidnight(time: String): String {
        val (hour, minute, second) = time.split(":").map { it.toInt() }
        return if (hour < 5) {
            String.format("%02d:%02d:%02d", hour + 24, minute, second)
        } else {
            time
        }
    }
}
