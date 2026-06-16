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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Runnable
import javax.inject.Inject

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
    private lateinit var autoScrollRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.initSelectedStopID()
        viewModel.fetchData()
        viewModel.start()
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.bus_realtime_error), Toast.LENGTH_SHORT).show() }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.notices.observe(viewLifecycleOwner) { notices ->
            if (notices.isNotEmpty()) {
                binding.noticeLayout.visibility = View.VISIBLE
                (binding.noticeViewPager.adapter as BusNoticeAdapter).updateList(notices)
                autoScrollRunnable = Runnable {
                    if (binding.noticeViewPager.adapter != null && binding.noticeViewPager.adapter!!.itemCount > 0 && !manuallyScrolled) {
                        currentPosition = (currentPosition + 1) % binding.noticeViewPager.adapter!!.itemCount
                        binding.noticeViewPager.setCurrentItem(currentPosition, true)
                        scrollHandler.postDelayed(autoScrollRunnable, 5000)
                    }
                }
                if (!manuallyScrolled) scrollHandler.postDelayed(autoScrollRunnable, 5000)
            } else {
                binding.noticeLayout.visibility = View.GONE
                if (::autoScrollRunnable.isInitialized) {
                    scrollHandler.removeCallbacks(autoScrollRunnable)
                }
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
        binding.stopFab.setOnClickListener {
            AnalyticsManager.logSelect(AnalyticsItem.BUS_STOP_BUTTON)
            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusHelpDialogFragment().also {
                findNavController().safeNavigate(it)
            }
        }
        binding.noticeViewPager.adapter = noticeAdapter
        binding.noticeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    manuallyScrolled = true
                    scrollHandler.removeCallbacks(autoScrollRunnable)
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
                    { binding.stopFab },
                    R.string.coachmark_bus_stop_title, R.string.coachmark_bus_stop_desc,
                    shape = CoachmarkShape.CIRCLE
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
        return binding.root
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
        val stops = viewModel.result.value
            ?.distinctBy { it.stop.seq }
            ?.filter { it.stop.seq in listOf(216000379, 216000381, 216000383) }
            ?.map { item ->
                val resId = when (item.stop.seq) {
                    216000379 -> R.string.bus_stop_convention
                    216000381 -> R.string.bus_stop_cluster
                    216000383 -> R.string.bus_stop_dormitory
                    else -> -1
                }
                Triple(resId, item.stop.latitude, item.stop.longitude)
            } ?: emptyList()

        if (stops.isEmpty()) return

        fun selectNearest(location: Location) {
            if (setClosestStop) return
            val nearest = stops.minByOrNull { (_, lat, lng) ->
                (lat - location.latitude) * (lat - location.latitude) +
                    (lng - location.longitude) * (lng - location.longitude)
            }
            nearest?.let { (stopRes, _, _) ->
                setClosestStop = true
                viewModel.setSelectedStopID(stopRes)
            }
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
        if (::autoScrollRunnable.isInitialized) {
            scrollHandler.removeCallbacks(autoScrollRunnable)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
        manuallyScrolled = false
        if (::autoScrollRunnable.isInitialized && !manuallyScrolled) {
            scrollHandler.postDelayed(autoScrollRunnable, 5000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        childFragmentManager.fragments.toList().forEach {
            childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
        }
        binding.viewPager.adapter = null
    }

    companion object {
        private const val LOCATION_MAX_AGE_MILLIS = 60_000L
    }
}
