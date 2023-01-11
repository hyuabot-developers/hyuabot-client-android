package app.kobuggi.hyuabot.ui.bus.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusTimetableBinding
import app.kobuggi.hyuabot.ui.bus.timetable.TimetableFragmentArgs
import app.kobuggi.hyuabot.ui.shuttle.timetable.TimetablePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class TimetableFragment : Fragment() {
    companion object {
        fun newInstance() = TimetableFragment()
    }
    private val viewModel: TimetableViewModel by viewModels()
    private val binding by lazy { FragmentBusTimetableBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val res = resources
        val args = TimetableFragmentArgs.fromBundle(requireArguments())
        val now = LocalDate.now()
        binding.busTimetableTitle.title = res.getString(R.string.bus_timetable_title, args.routeName)
        binding.busTimetableTitle.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.busTimetableViewpager.adapter = TimetablePagerAdapter(this)
        when (now.dayOfWeek.value) {
            in 1..5 -> {
                binding.busTimetableViewpager.setCurrentItem(0, false)
            }
            6 -> {
                binding.busTimetableViewpager.setCurrentItem(1, false)
            }
            else -> {
                binding.busTimetableViewpager.setCurrentItem(2, false)
            }
        }
        TabLayoutMediator(binding.busTimetableTab, binding.busTimetableViewpager) { tab, position ->
            tab.text = when (position) {
                0 -> res.getString(R.string.weekdays)
                1 -> res.getString(R.string.saturdays)
                else -> res.getString(R.string.sundays)
            }
        }.attach()

        viewModel.setTimetableData(args.routeId, args.startStop)
        viewModel.fetchTimetable()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.busTimetableProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        return binding.root
    }
}