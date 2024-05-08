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
import javax.inject.Inject

@AndroidEntryPoint
class BusTabSundayFragment @Inject constructor() : Fragment() {
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
            adapter.updateData(it.filter { timetable -> timetable.weekdays == "sunday" })
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
