package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentSubwayRealtimeBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubwayRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSubwayRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: SubwayRealtimeViewModel by viewModels()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.fetchData()
        viewModel.start()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.subway_realtime_error), Toast.LENGTH_SHORT).show() }
        }

        val viewpagerAdapter = SubwayRealtimeViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.subway_tab_blue,
            R.string.subway_tab_yellow,
            R.string.subway_tab_transfer
        )
        binding.viewPager.adapter = viewpagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.SUBWAY) {
            listOf(
                CoachmarkStep(
                    { binding.tabLayout },
                    R.string.coachmark_subway_tab_title, R.string.coachmark_subway_tab_desc
                ),
                CoachmarkStep(
                    { null },
                    R.string.coachmark_subway_transfer_tip_title, R.string.coachmark_subway_transfer_tip_desc,
                    centered = true
                ),
            )
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        childFragmentManager.fragments.toList().forEach {
            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
        binding.viewPager.adapter = null
    }
}
