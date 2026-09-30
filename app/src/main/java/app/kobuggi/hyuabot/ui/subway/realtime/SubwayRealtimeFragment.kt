package app.kobuggi.hyuabot.ui.subway.realtime

import android.os.Bundle
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import app.kobuggi.hyuabot.util.disableViewStateSaving
import app.kobuggi.hyuabot.util.setSkeletonLoading
import app.kobuggi.hyuabot.util.TransitFreshnessChecker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.setSkeletonLoading(it)
            updateTransitStatus()
        }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.subway_realtime_error), Toast.LENGTH_SHORT).show() }
            updateTransitStatus()
        }
        viewModel.lastSuccessfulCheckAt.observe(viewLifecycleOwner) { updateTransitStatus() }
        binding.transitRetryButton.setOnClickListener { viewModel.fetchData() }

        val viewpagerAdapter = SubwayRealtimeViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.subway_tab_blue,
            R.string.subway_tab_yellow,
            R.string.subway_tab_transfer
        )
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTransitStatus()  // Update status when tab changes
            }
        })
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

    private fun updateTransitStatus() {
        if (!isAdded) return
        val hasError = viewModel.queryError.value != null
        binding.transitRetryButton.visibility = if (hasError) View.VISIBLE else View.GONE
        val stations = when (binding.viewPager.currentItem) {
            0 -> {
                listOfNotNull(
                    viewModel.campusBlue.value,
                    viewModel.oidoBlue.value
                )
            }
            1 -> {
                listOfNotNull(
                    viewModel.campusYellow.value,
                    viewModel.oidoYellow.value
                )
            }
            2 -> {
                listOfNotNull(
                    viewModel.campusYellow.value,
                    viewModel.campusBlue.value,
                    viewModel.oidoYellow.value,
                    viewModel.oidoBlue.value,
                    viewModel.chojiSeohae.value,
                )
            }
            else -> emptyList()
        }
        val now = Instant.now()
        val stationUpdates = stations.map { station -> station.realtime.map { it.updatedAt } }
        val updates = stationUpdates.mapNotNull(TransitFreshnessChecker::latestUpdate)
        val oldestStale = TransitFreshnessChecker.staleSubwayUpdates(stationUpdates, now).minOrNull()
        val lastCheck = viewModel.lastSuccessfulCheckAt.value
        binding.transitStatusText.text = when {
            hasError -> getString(if (isOffline()) R.string.transit_offline else R.string.transit_error)
            viewModel.isLoading.value == true && lastCheck == null -> getString(R.string.transit_loading)
            lastCheck == null || stations.none { station -> station.arrival.any { it.entries.isNotEmpty() } } ->
                getString(R.string.transit_empty)
            oldestStale != null -> getString(
                R.string.freshness_stale_format,
                TransitFreshnessChecker.ageMinutes(oldestStale, now),
            )
            updates.isEmpty() -> getString(R.string.freshness_scheduled_format)
            else -> getString(
                R.string.freshness_fresh_format,
                lastCheck.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
            )
        }
    }

    private fun isOffline(): Boolean {
        val manager = requireContext().getSystemService(ConnectivityManager::class.java)
        val network = manager.activeNetwork ?: return true
        val capabilities = manager.getNetworkCapabilities(network) ?: return true
        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
