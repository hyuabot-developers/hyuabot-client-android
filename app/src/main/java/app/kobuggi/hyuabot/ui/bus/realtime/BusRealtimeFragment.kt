package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkShape
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import app.kobuggi.hyuabot.util.setSkeletonLoading
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Runnable
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving

@AndroidEntryPoint
class BusRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: BusRealtimeViewModel by viewModels()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private var currentPosition = 0
    private var manuallyScrolled = false
    private var setClosestStop = false
    private val scrollHandler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = Runnable {
        val adapter = binding.noticeViewPager.adapter
        if (adapter != null && adapter.itemCount > 1 && !manuallyScrolled && isResumed) {
            currentPosition = (binding.noticeViewPager.currentItem + 1) % adapter.itemCount
            binding.noticeViewPager.setCurrentItem(currentPosition, true)
            scheduleNoticeAutoScroll()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.initSelectedStopID()
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.bus_realtime_error), Toast.LENGTH_SHORT).show() }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.setSkeletonLoading(it)
        }
        viewModel.notices.observe(viewLifecycleOwner) { notices ->
            if (notices.isNotEmpty()) {
                binding.noticeLayout.visibility = View.VISIBLE
                (binding.noticeViewPager.adapter as BusNoticeAdapter).updateList(notices)
                currentPosition = binding.noticeViewPager.currentItem.coerceAtMost(notices.lastIndex)
                scheduleNoticeAutoScroll()
            } else {
                binding.noticeLayout.visibility = View.GONE
                stopNoticeAutoScroll()
            }
        }
        viewModel.result.observe(viewLifecycleOwner) { buses ->
            if (!setClosestStop && buses.isNotEmpty()) {
                moveToNearestStop(LocationServices.getFusedLocationProviderClient(requireActivity()))
            }
        }
        val viewpagerAdapter = BusRealtimeViewPagerAdapter(childFragmentManager, lifecycle)
        val noticeAdapter = BusNoticeAdapter(emptyList())
        val tabLabelList = listOf(
            R.string.bus_tab_city,
            R.string.bus_tab_seoul,
            R.string.bus_tab_suwon,
            R.string.bus_tab_other
        )
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.offscreenPageLimit = 1
        binding.busQuickSettingsButton.setOnClickListener { openQuickSettings() }
        childFragmentManager.setFragmentResultListener(
            BusQuickSettingsDialog.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            if (result.containsKey(BusQuickSettingsDialog.KEY_SHOW_SECONDARY_ETA)) {
                viewModel.setShowSecondaryEta(result.getBoolean(BusQuickSettingsDialog.KEY_SHOW_SECONDARY_ETA))
            }
            result.getString(BusQuickSettingsDialog.KEY_SEOUL_TARGET)?.let {
                viewModel.setSeoulTarget(BusSeoulTargetStop.from(it))
            }
            if (result.getBoolean(BusQuickSettingsDialog.KEY_OPEN_HELP, false)) {
                AnalyticsManager.logSelect(AnalyticsItem.BUS_OPEN_HELP)
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusHelpDialogFragment().also {
                    findNavController().safeNavigate(it)
                }
            }
            if (result.getBoolean(BusQuickSettingsDialog.KEY_OPEN_INQUIRY, false)) {
                BusRealtimeFragmentDirections.actionBusRealtimeFragmentToInquiryChatFragment().also {
                    findNavController().safeNavigate(it)
                }
            }
        }
        binding.noticeViewPager.adapter = noticeAdapter
        binding.noticeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    manuallyScrolled = true
                    stopNoticeAutoScroll()
                }
                if (state == ViewPager2.SCROLL_STATE_IDLE && manuallyScrolled) {
                    currentPosition = binding.noticeViewPager.currentItem
                }
            }
        })
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.BUS) {
            listOf(
                CoachmarkStep(
                    { binding.tabLayout },
                    R.string.coachmark_bus_tab_title, R.string.coachmark_bus_tab_desc
                ),
                CoachmarkStep(
                    { binding.busQuickSettingsButton },
                    R.string.coachmark_bus_stop_title, R.string.coachmark_bus_stop_desc,
                    shape = CoachmarkShape.ROUNDED_RECT
                ),
                CoachmarkStep(
                    {
                        firstVisibleBusChildView(
                            R.id.departure_log_first,
                            R.id.departure_log_second,
                            R.id.departure_log_third
                        )
                    },
                    R.string.coachmark_bus_log_title, R.string.coachmark_bus_log_desc,
                    centered = true
                ),
            )
        }
        return binding.root.also { disableViewStateSaving(it) }
    }

    private fun openQuickSettings() {
        if (childFragmentManager.findFragmentByTag(BUS_QUICK_SETTINGS_TAG) != null) return
        BusQuickSettingsDialog.newInstance(
            showSecondaryEta = viewModel.showSecondaryEta.value ?: true,
            seoulTarget = viewModel.seoulTarget.value ?: BusSeoulTargetStop.GANGNAM,
        ).show(childFragmentManager, BUS_QUICK_SETTINGS_TAG)
    }

    private fun firstVisibleBusChildView(vararg ids: Int): View? {
        val root = childFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}")?.view ?: return null
        for (id in ids) {
            val target = root.findViewById<View>(id)
            if (target != null && target.isShown) return target
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private fun moveToNearestStop(client: FusedLocationProviderClient) {
        val allStops = viewModel.result.value?.distinctBy { it.stop.seq } ?: emptyList()

        fun candidates(seqToRes: Map<Int, Int>): List<Triple<Int, Double, Double>> {
            return allStops.filter { it.stop.seq in seqToRes.keys }.map { item ->
                Triple(seqToRes.getValue(item.stop.seq), item.stop.latitude, item.stop.longitude)
            }
        }

        val campusStopMap = mapOf(
            216000379 to R.string.bus_stop_convention,
            216000381 to R.string.bus_stop_cluster,
            216000383 to R.string.bus_stop_dormitory,
        )
        val seoulRemoteStopMap = mapOf(
            121000060 to R.string.bus_stop_seocho,
            121000929 to R.string.bus_stop_gyodae,
            121000974 to R.string.bus_stop_gangnam,
            121000970 to R.string.bus_stop_yangjae,
            121000220 to R.string.bus_stop_yangjae_forest,
        )
        val cityCandidates = candidates(campusStopMap)
        val seoulFirstCandidates = candidates(campusStopMap + seoulRemoteStopMap)
        val seoulSecondCandidates = candidates(mapOf(216000719 to R.string.bus_stop_main_gate) + seoulRemoteStopMap)
        val suwonCandidates = candidates(
            mapOf(
                216000070 to R.string.bus_stop_entrance,
                202000106 to R.string.bus_stop_suwon_station,
            )
        )

        if (cityCandidates.isEmpty() && seoulFirstCandidates.isEmpty() && seoulSecondCandidates.isEmpty() && suwonCandidates.isEmpty()) return

        fun nearestKey(candidateList: List<Triple<Int, Double, Double>>, location: Location): Int? {
            return candidateList.minByOrNull { (_, lat, lng) ->
                (lat - location.latitude) * (lat - location.latitude) +
                    (lng - location.longitude) * (lng - location.longitude)
            }?.first
        }

        fun selectNearest(location: Location) {
            if (setClosestStop) return
            setClosestStop = true
            nearestKey(cityCandidates, location)?.let { viewModel.setSelectedStopID(it) }
            nearestKey(seoulFirstCandidates, location)?.let { viewModel.setSeoulFirstStopID(it) }
            nearestKey(seoulSecondCandidates, location)?.let { viewModel.setSeoulSecondStopID(it) }
            nearestKey(suwonCandidates, location)?.let { viewModel.setSuwonStopID(it) }
        }

        client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null && isFresh(location)) {
                    selectNearest(location)
                } else {
                    val tokenSource = CancellationTokenSource()
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                        .addOnSuccessListener { loc -> loc?.let { selectNearest(it) } }
                        .addOnFailureListener { Log.e("BusRealtimeFragment", "Failed to get location", it) }
                }
            }
            .addOnFailureListener {
                val tokenSource = CancellationTokenSource()
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                    .addOnSuccessListener { loc -> loc?.let { selectNearest(it) } }
            }
    }

    private fun isFresh(location: Location): Boolean {
        val ageMillis = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000
        return ageMillis in 0..LOCATION_MAX_AGE_MILLIS
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
        stopNoticeAutoScroll()
    }

    override fun onResume() {
        super.onResume()
        setClosestStop = false
        binding.viewPager.post {
            if (isAdded && view != null && viewModel.result.value?.isNotEmpty() == true) {
                moveToNearestStop(LocationServices.getFusedLocationProviderClient(requireActivity()))
            }
        }
        viewModel.start()
        manuallyScrolled = false
        scheduleNoticeAutoScroll()
    }

    override fun onDestroyView() {
        stopNoticeAutoScroll()
        super.onDestroyView()
        childFragmentManager.fragments.toList().forEach {
            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
        binding.viewPager.adapter = null
    }

    private fun scheduleNoticeAutoScroll() {
        stopNoticeAutoScroll()
        val itemCount = binding.noticeViewPager.adapter?.itemCount ?: 0
        if (itemCount > 1 && !manuallyScrolled && isResumed) {
            scrollHandler.postDelayed(autoScrollRunnable, NOTICE_AUTO_SCROLL_INTERVAL_MILLIS)
        }
    }

    private fun stopNoticeAutoScroll() {
        scrollHandler.removeCallbacks(autoScrollRunnable)
    }

    companion object {
        private const val LOCATION_MAX_AGE_MILLIS = 60_000L
        private const val NOTICE_AUTO_SCROLL_INTERVAL_MILLIS = 5_000L
        private const val BUS_QUICK_SETTINGS_TAG = "BusQuickSettingsDialog"
    }
}
