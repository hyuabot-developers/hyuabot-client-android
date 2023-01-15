package app.kobuggi.hyuabot.ui.subway.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayTimetableBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class TimetableFragment : Fragment() {
    companion object {
        fun newInstance() = TimetableFragment()
    }
    private val viewModel: TimetableViewModel by viewModels()
    private val binding by lazy { FragmentSubwayTimetableBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val res = resources
        val args = TimetableFragmentArgs.fromBundle(requireArguments())
        val now = LocalDate.now()
        binding.subwayTimetableTitle.title = res.getString(R.string.subway_timetable_title, resources.getString(if(args.heading == "up") R.string.heading_up else R.string.heading_down))
        binding.subwayTimetableTitle.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.subwayTimetableViewpager.adapter = TimetablePagerAdapter(this)
        when (now.dayOfWeek.value) {
            in 1..5 -> {
                binding.subwayTimetableViewpager.setCurrentItem(0, false)
            }
            else -> {
                binding.subwayTimetableViewpager.setCurrentItem(2, false)
            }
        }
        TabLayoutMediator(binding.subwayTimetableTab, binding.subwayTimetableViewpager) { tab, position ->
            tab.text = when (position) {
                0 -> res.getString(R.string.weekdays)
                else -> res.getString(R.string.weekends)
            }
        }.attach()

        viewModel.setTimetableData(args.station, args.heading)
        viewModel.fetchTimetable()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.subwayTimetableProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "지하철 시간표 정보")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "SubwayTimetableFragment")
            })
        }
    }
}