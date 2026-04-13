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
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusTimetableTabBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class BusTabSaturdayFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: BusTimetableViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val decoration = DividerItemDecoration(requireContext(), VERTICAL)
        val adapter = BusTimetableListAdapter(requireContext(), listOf())

        parentViewModel.result.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val localTime = LocalTime.now()
            val timetableItems = mutableListOf<BusTimetableItem>()
            it.forEach { route ->
                timetableItems.addAll(route.timetable.filter { timetable ->  timetable.weekday == "saturday" }.map { timetable ->
                    BusTimetableItem(
                        routeName = route.route.name,
                        weekdays = timetable.weekday,
                        time = if (timetable.time.hour < 4) {
                            requireContext().getString(
                                R.string.bus_timetable_time_format,
                                (timetable.time.hour + 24).toString().padStart(2, '0'),
                                timetable.time.minute.toString().padStart(2, '0')
                            )
                        } else {
                            requireContext().getString(
                                R.string.bus_timetable_time_format,
                                timetable.time.hour.toString().padStart(2, '0'),
                                timetable.time.minute.toString().padStart(2, '0')
                            )
                        }
                    )
                })
            }
            timetableItems.sortBy { timetable -> timetable.time }
            val afterNowItemIndex = timetableItems.indexOfFirst { item ->  item.time > getString(
                R.string.bus_timetable_time_format,
                localTime.hour.toString().padStart(2, '0'),
                localTime.minute.toString().padStart(2, '0')
            )}
            if (timetableItems.isEmpty()) {
                binding.busTimetableRecyclerView.visibility = View.GONE
                binding.busTimetableEmptyText.visibility = View.VISIBLE
                return@observe
            } else {
                binding.busTimetableRecyclerView.visibility = View.VISIBLE
                binding.busTimetableEmptyText.visibility = View.GONE
            }
            adapter.apply {
                updateData(timetableItems)
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
