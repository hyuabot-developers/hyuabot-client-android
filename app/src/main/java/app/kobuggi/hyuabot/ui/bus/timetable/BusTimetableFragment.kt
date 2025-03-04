package app.kobuggi.hyuabot.ui.bus.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusTimetableBinding
import app.kobuggi.hyuabot.service.safeNavigate
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class BusTimetableFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusTimetableBinding.inflate(layoutInflater) }
    private val viewModel: BusTimetableViewModel by viewModels()
    private val args: BusTimetableFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val firstRouteID = args.firstRouteID
        val secondRouteID = args.secondRouteID
        val thirdRouteID = args.thirdRouteID
        val routes = listOf(firstRouteID, secondRouteID, thirdRouteID).filter { it > 0 }
        viewModel.fetchData(routes, args.stopID)
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.bus_timetable_error), Toast.LENGTH_SHORT).show() }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        }

        val viewpagerAdapter = BusTimetableViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.weekdays,
            R.string.saturdays,
            R.string.sundays
        )
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.setCurrentItem(when (LocalDate.now().dayOfWeek.value) {
            1, 2, 3, 4, 5 -> 0
            6 -> 1
            7 -> 2
            else -> 0
        }, false)
        binding.infoFab.setOnClickListener {
            BusTimetableFragmentDirections.actionBusTimetableFragmentToBusRouteInfoDialogFragment(
                routeID = args.firstRouteID,
                stopID = args.stopID
            ).also {
                findNavController().safeNavigate(it)
            }
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
