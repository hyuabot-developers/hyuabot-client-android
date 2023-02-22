package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableBinding
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
    private val binding by lazy { FragmentShuttleTimetableBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val res = resources
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
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (it) {
                val toast = Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "셔틀 시간표 목록")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ShuttleTimetableFragment")
            })
        }
    }
}