package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableTabBinding
import app.kobuggi.hyuabot.util.LinearLayoutManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleTabWeekendsFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleTimetableTabBinding.inflate(layoutInflater) }
    private val parentViewModel: ShuttleTimetableViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val adapter = ShuttleTimetableListAdapter(
            parentViewModel.stopResID.value ?: 0,
            parentViewModel.headerResID.value ?: 0,
            emptyList()
        )
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.shuttleTimetableRecyclerView.apply {
            setAdapter(adapter)
            layoutManager = LinearLayoutManagerWrapper(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(decoration)
        }
        parentViewModel.result.observe(viewLifecycleOwner) {
            val timetableItems = it.filter { item -> !item.weekdays }
            if (timetableItems.isNotEmpty()) {
                adapter.updateData(timetableItems)
                binding.apply {
                    shuttleTimetableRecyclerView.visibility = View.VISIBLE
                    shuttleTimetableEmptyText.visibility = View.GONE
                }
            } else {
                binding.apply {
                    shuttleTimetableRecyclerView.visibility = View.GONE
                    shuttleTimetableEmptyText.visibility = View.VISIBLE
                }
            }
        }
        return binding.root
    }
}
