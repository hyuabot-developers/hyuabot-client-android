package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class TimetableFragment : Fragment() {
    companion object {
        fun newInstance() = TimetableFragment()
    }
    private val viewModel: TimetableViewModel by viewModels()
    private val binding by lazy { FragmentShuttleTimetableBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val res = GlobalApplication.getAppResources()
        val args = TimetableFragmentArgs.fromBundle(requireArguments())
        val now = LocalDate.now()
        binding.shuttleTimetableTitle.title = res.getString(R.string.shuttle_timetable_title,
            res.getString(args.stop), res.getString(args.destination))
        binding.shuttleTimetableTitle.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.shuttleTimetableViewpager.adapter = TimetablePagerAdapter(this)
        if (now.dayOfWeek.value in 1..5) {
            binding.shuttleTimetableViewpager.setCurrentItem(0, false)
        } else {
            binding.shuttleTimetableViewpager.setCurrentItem(1, false)
        }
        TabLayoutMediator(binding.shuttleTimetableTab, binding.shuttleTimetableViewpager) { tab, position ->
            tab.text = when (position) {
                0 -> res.getString(R.string.weekdays)
                else -> res.getString(R.string.weekends)
            }
        }.attach()

        viewModel.setTimetableData(args.stop, args.destination)
        viewModel.fetchTimetable()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.shuttleTimetableProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        return binding.root
    }
}