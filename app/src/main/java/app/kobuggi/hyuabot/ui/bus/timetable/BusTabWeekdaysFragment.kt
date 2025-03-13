package app.kobuggi.hyuabot.ui.bus.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentBusTimetableTabBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class BusTabWeekdaysFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusTimetableViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val adapter = BusTimetableListAdapter(requireContext(), listOf())
        val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        parentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val localTime = LocalTime.now()
            val timetableItems = mutableListOf<BusTimetableItem>()
            it.forEach { route ->
                timetableItems.addAll(route.timetable.filter { it.weekdays == "weekdays" }.map {
                    BusTimetableItem(
                        routeName = route.info.name,
                        weekdays = it.weekdays,
                        time = it.time
                    )
                })
            }
            val afterNowItemIndex = timetableItems.indexOfFirst { item -> item.time > localTime.format(dateTimeFormatter) }
            adapter.apply {
                updateData(timetableItems.sortedBy { it.time })
                if (afterNowItemIndex != -1) {
                    binding.busTimetableRecyclerView.scrollToPosition(afterNowItemIndex)
                }
            }
        }
        binding.apply {
            busTimetableRecyclerView.apply {
                this.adapter = adapter
                this.layoutManager = LinearLayoutManager(requireContext())
                this.addItemDecoration(decoration)
            }
        }
        return binding.root
    }
}
