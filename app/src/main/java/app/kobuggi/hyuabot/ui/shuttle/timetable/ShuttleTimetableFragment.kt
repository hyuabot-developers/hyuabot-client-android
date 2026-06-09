package app.kobuggi.hyuabot.ui.shuttle.timetable

import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.util.NavControllerExtension.safeNavigate
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkController
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkShape
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.ensureCoachmarkBaseline
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleTimetableFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleTimetableBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleTimetableViewModel by viewModels()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val stopID = arguments?.getInt("stopID") ?: 0
        val destinationID = arguments?.getInt("destinationID") ?: 0
        val now = LocalDate.now()

        viewModel.stopResID.value = stopID
        viewModel.headerResID.value = destinationID

        when (stopID) {
            R.string.shuttle_tab_dormitory_out -> {
                viewModel.stopID.value = "dormitory_o"
                when (destinationID) {
                    R.string.shuttle_header_bound_for_station -> {
                        viewModel.destinations.value = listOf("STATION")
                    }

                    R.string.shuttle_header_bound_for_terminal -> {
                        viewModel.destinations.value = listOf("TERMINAL")
                    }

                    R.string.shuttle_header_bound_for_jungang_station -> {
                        viewModel.destinations.value = listOf("JUNGANG")
                    }

                    else -> {
                        viewModel.destinations.value = null
                    }
                }
            }
            R.string.shuttle_tab_shuttlecock_out -> {
                viewModel.stopID.value = "shuttlecock_o"
                when (destinationID) {
                    R.string.shuttle_header_bound_for_station -> {
                        viewModel.destinations.value = listOf("STATION")
                    }

                    R.string.shuttle_header_bound_for_terminal -> {
                        viewModel.destinations.value = listOf("TERMINAL")
                    }

                    R.string.shuttle_header_bound_for_jungang_station -> {
                        viewModel.destinations.value = listOf("JUNGANG")
                    }

                    else -> {
                        viewModel.destinations.value = null
                    }
                }
            }
            R.string.shuttle_tab_station -> {
                viewModel.stopID.value = "station"
                when (destinationID) {
                    R.string.shuttle_header_bound_for_dormitory -> {
                        viewModel.destinations.value = listOf("CAMPUS")
                    }

                    R.string.shuttle_header_bound_for_terminal -> {
                        viewModel.destinations.value = listOf("TERMINAL")
                    }

                    R.string.shuttle_header_bound_for_jungang_station -> {
                        viewModel.destinations.value = listOf("JUNGANG")
                    }

                    else -> {
                        viewModel.destinations.value = null
                    }
                }
            }
            R.string.shuttle_tab_terminal -> {
                viewModel.stopID.value = "terminal"
                when (destinationID) {
                    R.string.shuttle_header_bound_for_dormitory -> {
                        viewModel.destinations.value = listOf("CAMPUS")
                    }

                    else -> {
                        viewModel.destinations.value = null
                    }
                }
            }
            R.string.shuttle_tab_jungang_station -> {
                viewModel.stopID.value = "jungang_stn"
                when (destinationID) {
                    R.string.shuttle_header_bound_for_dormitory -> {
                        viewModel.destinations.value = listOf("CAMPUS")
                    }

                    else -> {
                        viewModel.destinations.value = null
                    }
                }
            }
            R.string.shuttle_tab_shuttlecock_in -> {
                viewModel.stopID.value = "shuttlecock_i"
                when (destinationID) {
                    R.string.shuttle_header_bound_for_dormitory -> {
                        viewModel.destinations.value = listOf("CAMPUS")
                    }

                    else -> {
                        viewModel.destinations.value = null
                    }
                }
            }
            else -> {
                viewModel.stopID.value = null
            }
        }

        viewModel.fetchData()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.period.observe(viewLifecycleOwner) {
            viewModel.fetchData()
        }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.shuttle_timetable_error), Toast.LENGTH_SHORT).show() }
        }

        val viewpagerAdapter = ShuttleTimetableViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.shuttle_timetable_tab_weekdays,
            R.string.shuttle_timetable_tab_weekends,
        )

        binding.filterFab.setOnClickListener {
            AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_OPEN_FILTER)
            findNavController()
                .currentBackStackEntry?.savedStateHandle?.apply {
                    remove<String>("period")
                }?.getLiveData<String>("period")?.observe(viewLifecycleOwner) {
                    viewModel.period.value = it
                }
            val action = ShuttleTimetableFragmentDirections.actionShuttleTimetableFragmentToShuttleTimetableFilterDialogFragment()
            findNavController().safeNavigate(action)
        }
        binding.viewPager.apply {
            adapter = viewpagerAdapter
            setCurrentItem(if (now.dayOfWeek.value < 6) 0 else 1, false)
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        maybeShowCoachmark()
        return binding.root
    }

    private fun maybeShowCoachmark() {
        viewLifecycleOwner.lifecycleScope.launch {
            requireContext().ensureCoachmarkBaseline(userPreferencesRepository)
            if (userPreferencesRepository.coachmarkSeen(Coachmarks.SHUTTLE_TIMETABLE).first()) return@launch
            view?.post {
                if (!isAdded) return@post
                CoachmarkController.show(requireActivity(), buildCoachmarkSteps()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        userPreferencesRepository.markCoachmarkSeen(Coachmarks.SHUTTLE_TIMETABLE)
                    }
                }
            }
        }
    }

    private fun buildCoachmarkSteps(): List<CoachmarkStep> = listOf(
        CoachmarkStep(
            { binding.tabLayout },
            R.string.coachmark_shuttle_timetable_tab_title, R.string.coachmark_shuttle_timetable_tab_desc
        ),
        CoachmarkStep(
            { binding.filterFab },
            R.string.coachmark_shuttle_timetable_filter_title, R.string.coachmark_shuttle_timetable_filter_desc,
            shape = CoachmarkShape.CIRCLE
        ),
    )

    override fun onDestroyView() {
        super.onDestroyView()
        childFragmentManager.fragments.toList().forEach {
            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
        binding.viewPager.adapter = null
    }
}
