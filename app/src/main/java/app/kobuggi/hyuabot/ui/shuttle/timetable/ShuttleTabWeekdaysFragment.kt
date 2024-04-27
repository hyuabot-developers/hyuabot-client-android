package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableTabBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleTabWeekdaysFragment @Inject constructor() : Fragment() {
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
        binding.shuttleTimetableRecyclerView.apply {
            setAdapter(adapter)
        }
        parentViewModel.result.observe(viewLifecycleOwner) {
            adapter.updateData(it.filter { item -> item.weekdays })
        }
        return binding.root
    }
}
