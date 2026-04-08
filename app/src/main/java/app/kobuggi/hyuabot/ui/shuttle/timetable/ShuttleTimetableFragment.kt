package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentShuttleTimetableBinding
import app.kobuggi.hyuabot.util.NavControllerExtension.safeNavigate
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleTimetableFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleTimetableBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleTimetableViewModel by viewModels()

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
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
    }
}
