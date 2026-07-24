package app.kobuggi.hyuabot.ui.shuttle.realtime

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeBinding
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkController
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkShape
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.ensureCoachmarkEligibility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import app.kobuggi.hyuabot.util.disableViewStateSaving
import app.kobuggi.hyuabot.util.setSkeletonLoading
import kotlin.math.sqrt

@AndroidEntryPoint
class ShuttleRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleRealtimeViewModel by viewModels()
    private val args: ShuttleRealtimeFragmentArgs by navArgs()
    private var currentPosition = 0
    private var manuallyScrolled = false
    private var honorDeepLinkStop = false
    private var hasRequestedInitialStopLocation = false
    private var hasManualStopSelection = false
    private var isApplyingInitialLocationSelection = false
    private var coachmarkShown = false
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
        val now = LocalDateTime.now()
        val viewpagerAdapter = ShuttleRealtimeViewPagerAdapter(childFragmentManager, lifecycle)
        val noticeAdapter = ShuttleNoticeAdapter(emptyList())
        val tabLabelList = listOf(
            R.string.shuttle_tab_dormitory_out,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_tab_station,
            R.string.shuttle_tab_terminal,
            R.string.shuttle_tab_jungang_station,
            R.string.shuttle_tab_shuttlecock_in
        )
        viewModel.viewModelScope.launch {
            viewModel.userPreferencesRepository.getShowShuttleDepartureTime().first().let {
                viewModel.showDepartureTime.value = it
            }
            viewModel.userPreferencesRepository.getShowShuttleByDestination().first().let {
                viewModel.showByDestination.value = it
            }
            viewModel.userPreferencesRepository.getShowShuttlePresence().first().let {
                viewModel.applyShowPresenceStatus(it)
            }
        }
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setPresenceStop(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    hasManualStopSelection = true
                }
            }
        })
        if (BuildConfig.DEBUG) {
            requireActivity().intent.getIntExtra(EXTRA_PRESENCE_PREVIEW_COUNT, -1)
                .takeIf { it >= 0 }
                ?.let(viewModel::setPresencePreviewCount)
        }
        binding.shuttleQuickSettingsButton.setOnClickListener { openQuickSettings() }
        childFragmentManager.setFragmentResultListener(
            ShuttleQuickSettingsDialog.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            if (result.containsKey(ShuttleQuickSettingsDialog.KEY_SHOW_BY_DESTINATION)) {
                viewModel.setShowByDestination(result.getBoolean(ShuttleQuickSettingsDialog.KEY_SHOW_BY_DESTINATION))
            }
            if (result.containsKey(ShuttleQuickSettingsDialog.KEY_SHOW_DEPARTURE_TIME)) {
                viewModel.setShowDepartureTime(result.getBoolean(ShuttleQuickSettingsDialog.KEY_SHOW_DEPARTURE_TIME))
            }
            if (result.containsKey(ShuttleQuickSettingsDialog.KEY_SHOW_PRESENCE_STATUS)) {
                viewModel.setShowPresenceStatus(result.getBoolean(ShuttleQuickSettingsDialog.KEY_SHOW_PRESENCE_STATUS))
            }
            if (result.getBoolean(ShuttleQuickSettingsDialog.KEY_OPEN_HOME, false)) {
                findNavController().navigate(R.id.homeFragment)
            }
        }
        binding.noticeViewPager.adapter = noticeAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!isApplyingInitialLocationSelection) {
                    hasManualStopSelection = true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        stopNameToTabIndex(args.stop)?.let { index ->
            honorDeepLinkStop = true
            binding.viewPager.setCurrentItem(index, false)
        }
        // If weekdays is from monday to friday and time is before 10:00, it is true and else false
        if (now.dayOfWeek.value in 1..5 && now.hour < 10) {
            Toast.makeText(requireContext(), getString(R.string.shuttle_realtime_toast), Toast.LENGTH_SHORT).show()
        }
        return binding.root.also { disableViewStateSaving(it) }
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.setSkeletonLoading(it)
        }
        viewModel.notices.observe(viewLifecycleOwner) { notices ->
            if (notices.isNotEmpty()) {
                binding.noticeLayout.visibility = View.VISIBLE
                (binding.noticeViewPager.adapter as ShuttleNoticeAdapter).updateList(notices)
                currentPosition = binding.noticeViewPager.currentItem.coerceAtMost(notices.lastIndex)
                scheduleNoticeAutoScroll()
            } else {
                binding.noticeLayout.visibility = View.GONE
                stopNoticeAutoScroll()
            }
        }
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
        viewModel.result.observe(viewLifecycleOwner) { stops ->
            if (honorDeepLinkStop || hasManualStopSelection || hasRequestedInitialStopLocation) {
                return@observe
            }
            if (stops.isNotEmpty()) {
                hasRequestedInitialStopLocation = true
                moveToNearestStop(fusedLocationProviderClient, stops)
            }
        }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.shuttle_realtime_error), Toast.LENGTH_SHORT).show() }
        }
        viewModel.result.observe(viewLifecycleOwner) { stops ->
            if (stops.isNotEmpty()) maybeShowCoachmark()
        }
        viewModel.presenceViewerCount.observe(viewLifecycleOwner) { viewerCount ->
            binding.shuttlePresencePill.isVisible = viewerCount != null
            if (viewerCount != null) {
                binding.shuttlePresenceCount.text = viewerCount.toString()
                binding.shuttlePresencePill.contentDescription = getString(
                    R.string.shuttle_presence_viewer_count,
                    viewerCount,
                )
            } else {
                binding.shuttlePresencePill.contentDescription = null
            }
        }
        viewModel.viewModelScope.apply {
            launch {
                viewModel.userPreferencesRepository.getShowShuttleDepartureTime().collect {
                    viewModel.showDepartureTime.value = it
                }
            }
            launch {
                viewModel.userPreferencesRepository.getShowShuttleByDestination().collect {
                    viewModel.showByDestination.value = it
                }
            }
            launch {
                viewModel.userPreferencesRepository.getShowShuttlePresence().collect {
                    viewModel.applyShowPresenceStatus(it)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
        stopNoticeAutoScroll()
    }

    override fun onResume() {
        super.onResume()
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

    private fun openQuickSettings() {
        if (childFragmentManager.findFragmentByTag(SHUTTLE_QUICK_SETTINGS_TAG) != null) return
        ShuttleQuickSettingsDialog.newInstance(
            showByDestination = viewModel.showByDestination.value ?: false,
            showDepartureTime = viewModel.showDepartureTime.value ?: false,
            showPresenceStatus = viewModel.showPresenceStatus.value ?: true,
        ).show(childFragmentManager, SHUTTLE_QUICK_SETTINGS_TAG)
    }

    @SuppressLint("MissingPermission")
    private fun moveToNearestStop(
        client: FusedLocationProviderClient,
        stops: List<ShuttleRealtimePageQuery.Stop>,
    ) {
        requestCurrentLocation(client, stops)
    }

    @SuppressLint("MissingPermission")
    private fun selectLastKnownLocation(
        client: FusedLocationProviderClient,
        stops: List<ShuttleRealtimePageQuery.Stop>,
    ) {
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null && isFresh(location)) selectNearestStop(stops, location)
            }
            .addOnFailureListener {
                Log.e("ShuttleRealtimeFragment", "Failed to get last known location", it)
            }
    }

    private fun isFresh(location: Location): Boolean {
        val ageMillis = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000
        return ageMillis in 0..LOCATION_MAX_AGE_MILLIS
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation(
        client: FusedLocationProviderClient,
        stops: List<ShuttleRealtimePageQuery.Stop>,
    ) {
        val tokenSource = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    selectNearestStop(stops, location)
                } else {
                    selectLastKnownLocation(client, stops)
                }
            }
            .addOnFailureListener {
                Log.e("ShuttleRealtimeFragment", "Failed to get user location", it)
                selectLastKnownLocation(client, stops)
            }
    }

    private fun selectNearestStop(stops: List<ShuttleRealtimePageQuery.Stop>, location: Location) {
        if (!isAdded || view == null || honorDeepLinkStop || hasManualStopSelection) {
            return
        }
        val gpsCandidates = stops.filterNot { it.name == "shuttlecock_i" }.ifEmpty { stops }
        val nearestStop = gpsCandidates.map { stopItem ->
            Pair(stopItem, calculateDistance(stopItem, location))
        }.minByOrNull { it.second }?.first
        Log.d("ShuttleRealtimeFragment", "Nearest stop: ${nearestStop?.name}, distance: ${nearestStop?.let { calculateDistance(it, location) }}")
        isApplyingInitialLocationSelection = true
        binding.viewPager.setCurrentItem(stopNameToTabIndex(nearestStop?.name) ?: 0, false)
        isApplyingInitialLocationSelection = false
    }

    private fun maybeShowCoachmark() {
        if (coachmarkShown) return
        coachmarkShown = true
        val owner = viewLifecycleOwner
        owner.lifecycleScope.launch {
            requireContext().ensureCoachmarkEligibility(viewModel.userPreferencesRepository)
            val showMainCoachmark = !viewModel.userPreferencesRepository.coachmarkSeen(COACHMARK_KEY).first()
            val showRealtimeUpdatesCoachmark =
                !viewModel.userPreferencesRepository.coachmarkSeen(REALTIME_UPDATES_COACHMARK_KEY).first()
            if (!showMainCoachmark && !showRealtimeUpdatesCoachmark) return@launch

            val originalByDestination = viewModel.userPreferencesRepository.getShowShuttleByDestination().first()
            val originalDepartureTime = viewModel.userPreferencesRepository.getShowShuttleDepartureTime().first()
            binding.viewPager.setCurrentItem(0, false)
            val rootView = view ?: return@launch
            rootView.post {
                if (!isAdded || view == null || !owner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                    return@post
                }
                CoachmarkController.show(requireActivity(), buildShuttleCoachmarkSteps(), owner) {
                    if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                        viewModel.setShowByDestination(originalByDestination)
                        viewModel.setShowDepartureTime(originalDepartureTime)
                    }
                    viewModel.setForceShowBusAlternative(false)
                    lifecycleScope.launch {
                        viewModel.userPreferencesRepository.markCoachmarkSeen(COACHMARK_KEY)
                        viewModel.userPreferencesRepository.markCoachmarkSeen(REALTIME_UPDATES_COACHMARK_KEY)
                    }
                }
            }
        }
    }

    private fun buildShuttleCoachmarkSteps(): List<CoachmarkStep> = listOf(
        CoachmarkStep(
            { requireActivity().findViewById(R.id.bottom_navigation) },
            R.string.coachmark_shuttle_nav_title, R.string.coachmark_shuttle_nav_desc
        ),
        CoachmarkStep(
            { binding.tabLayout },
            R.string.coachmark_shuttle_tab_title, R.string.coachmark_shuttle_tab_desc
        ),
        CoachmarkStep(
            { binding.shuttleQuickSettingsButton },
            R.string.coachmark_shuttle_destination_title, R.string.coachmark_shuttle_destination_desc,
            onShow = { viewModel.setShowByDestination(true) }
        ),
        CoachmarkStep(
            { binding.shuttleQuickSettingsButton },
            R.string.coachmark_shuttle_departure_title, R.string.coachmark_shuttle_departure_desc,
            onShow = { viewModel.setShowDepartureTime(true) }
        ),
        CoachmarkStep(
            {
                firstVisibleChildView(
                    R.id.info_button_bound_for_dormitory,
                    R.id.info_button_bound_for_terminal,
                    R.id.info_button_bound_for_jungang_station,
                    R.id.info_button_bound_for_station
                )
            },
            R.string.coachmark_shuttle_route_title, R.string.coachmark_shuttle_route_desc,
            onShow = { it.performClick() }
        ),
        CoachmarkStep(
            { firstVisibleRealtimeRow() },
            R.string.coachmark_shuttle_via_title, R.string.coachmark_shuttle_via_desc
        ),
        CoachmarkStep(
            { firstVisibleRealtimeRowChild(R.id.shuttle_alarm_button) },
            R.string.coachmark_shuttle_alarm_title,
            R.string.coachmark_shuttle_alarm_desc,
            shape = CoachmarkShape.CIRCLE
        ),
        CoachmarkStep(
            {
                viewModel.setForceShowBusAlternative(true)
                currentTabView()?.findViewById(R.id.bus_alternative_station)
            },
            R.string.coachmark_shuttle_bus_alternative_title,
            R.string.coachmark_shuttle_bus_alternative_desc
        ),
        CoachmarkStep(
            {
                viewModel.setForceShowBusAlternative(true)
                firstVisibleChildView(
                    R.id.bus_alternative_station_info,
                    R.id.bus_alternative_dormitory_info,
                    R.id.bus_alternative_dormitory_2_info,
                    R.id.bus_alternative_terminal_info,
                    R.id.bus_alternative_jungang_station_info
                )
            },
            R.string.coachmark_shuttle_bus_alternative_stop_title,
            R.string.coachmark_shuttle_bus_alternative_stop_desc,
            shape = CoachmarkShape.CIRCLE
        ),
        CoachmarkStep(
            { firstVisibleChildView(R.id.transfer_section) },
            R.string.coachmark_shuttle_transfer_title, R.string.coachmark_shuttle_transfer_desc
        ),
        CoachmarkStep(
            { firstVisibleChildView(R.id.stop_info, R.id.stop_info_2) },
            R.string.coachmark_shuttle_stop_title, R.string.coachmark_shuttle_stop_desc
        ),
        CoachmarkStep(
            { firstVisibleChildView(R.id.help_button, R.id.help_button_2) },
            R.string.coachmark_shuttle_help_title, R.string.coachmark_shuttle_help_desc
        ),
        CoachmarkStep(
            { null },
            R.string.coachmark_shuttle_widget_title, R.string.coachmark_shuttle_widget_desc,
            centered = true
        ),
    )

    private fun currentTabView(): View? =
        childFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}")?.view

    private fun firstVisibleChildView(vararg ids: Int): View? {
        val root = currentTabView() ?: return null
        for (id in ids) {
            val target = root.findViewById<View>(id)
            if (target != null && target.isShown) return target
        }
        return null
    }

    private fun firstVisibleRealtimeRowChild(childId: Int): View? {
        val root = currentTabView() ?: return null
        val recyclerIds = intArrayOf(
            R.id.realtime_view,
            R.id.realtime_view_bound_for_dormitory,
            R.id.realtime_view_bound_for_terminal,
            R.id.realtime_view_bound_for_jungang_station,
            R.id.realtime_view_bound_for_station,
        )
        for (id in recyclerIds) {
            val recycler = root.findViewById<RecyclerView>(id)
            if (recycler != null && recycler.isShown && recycler.isNotEmpty()) {
                val target = recycler.getChildAt(0).findViewById<View>(childId)
                if (target != null && target.isShown) return target
            }
        }
        return null
    }

    private fun firstVisibleRealtimeRow(): View? {
        val root = currentTabView() ?: return null
        val recyclerIds = intArrayOf(
            R.id.realtime_view,
            R.id.realtime_view_bound_for_dormitory,
            R.id.realtime_view_bound_for_terminal,
            R.id.realtime_view_bound_for_jungang_station,
            R.id.realtime_view_bound_for_station,
        )
        for (id in recyclerIds) {
            val recycler = root.findViewById<RecyclerView>(id)
            if (recycler != null && recycler.isShown && recycler.isNotEmpty()) {
                return recycler.getChildAt(0)
            }
        }
        return null
    }

    private fun stopNameToTabIndex(name: String?): Int? = when (name) {
        "dormitory_o" -> 0
        "shuttlecock_o" -> 1
        "station" -> 2
        "terminal" -> 3
        "jungang_stn" -> 4
        "shuttlecock_i" -> 5
        else -> null
    }

    private fun calculateDistance(stopItem: ShuttleRealtimePageQuery.Stop, location: Location): Double {
        val distance = sqrt(
        (stopItem.latitude - location.latitude) * (stopItem.latitude - location.latitude) +
            (stopItem.longitude - location.longitude) * (stopItem.longitude - location.longitude)
        )
        return distance
    }

    companion object {
        private const val LOCATION_MAX_AGE_MILLIS = 60_000L
        private const val NOTICE_AUTO_SCROLL_INTERVAL_MILLIS = 5_000L
        private val COACHMARK_KEY = Coachmarks.SHUTTLE
        private val REALTIME_UPDATES_COACHMARK_KEY = Coachmarks.SHUTTLE_REALTIME_UPDATES
        private const val SHUTTLE_QUICK_SETTINGS_TAG = "ShuttleQuickSettingsDialog"
        private const val EXTRA_PRESENCE_PREVIEW_COUNT = "shuttle_presence_preview_count"
    }
}
