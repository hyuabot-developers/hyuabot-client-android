package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import app.kobuggi.hyuabot.util.disableViewStateSaving
import app.kobuggi.hyuabot.util.setSkeletonLoading

@AndroidEntryPoint
class SubwayRealtimeFragment @Inject constructor() : Fragment() {
    private var pageCallback: androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback? = null
    private val binding by lazy { FragmentSubwayRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: SubwayRealtimeViewModel by viewModels()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.setSkeletonLoading(it)
        }

        val viewpagerAdapter = SubwayRealtimeViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.subway_tab_blue,
            R.string.subway_tab_yellow,
            R.string.subway_tab_transfer
        )
        binding.viewPager.adapter = viewpagerAdapter
        fun updateScreenFreshness(page: Int) {
            val times = viewModel.sectionUpdatedAts.value.orEmpty().filterKeys { it.startsWith("$page:") }.values.toList()
            binding.freshnessScreen.updatedAt = app.kobuggi.hyuabot.ui.common.screenReferenceTime(times)
        }
        binding.freshnessScreen.apply {
            source = app.kobuggi.hyuabot.ui.common.FreshnessLabel.Source.REALTIME
            analyticsDomain = "subway"
            analyticsScreen = "subway_realtime"
            delayAfter = java.time.Duration.ofMinutes(3)
            updatedAt = null
        }
        viewModel.sectionUpdatedAts.observe(viewLifecycleOwner) { updateScreenFreshness(binding.viewPager.currentItem) }
        binding.freshnessScreen.spokenPrefix = getString(R.string.freshness_context_subway)
        pageCallback = object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateScreenFreshness(position)
        }
        binding.viewPager.registerOnPageChangeCallback(pageCallback!!)
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
        return binding.root.also { disableViewStateSaving(it) }
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
        pageCallback?.let { binding.viewPager.unregisterOnPageChangeCallback(it) }
        pageCallback = null
        super.onDestroyView()
        childFragmentManager.fragments.toList().forEach {
            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
        binding.viewPager.adapter = null
    }
}
