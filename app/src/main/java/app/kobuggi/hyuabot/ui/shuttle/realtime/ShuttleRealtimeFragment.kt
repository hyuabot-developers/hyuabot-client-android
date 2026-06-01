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
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeBinding
import app.kobuggi.hyuabot.service.safeNavigate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Runnable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class ShuttleRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleRealtimeViewModel by viewModels()
    private val args: ShuttleRealtimeFragmentArgs by navArgs()
    private var currentPosition = 0
    private var setClosestStop = false
    private var honorDeepLinkStop = false
    private val scrollHandler = Handler(Looper.getMainLooper())
    private lateinit var autoScrollRunnable: Runnable

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
                binding.showDepartureTime.isChecked = it
            }
            viewModel.userPreferencesRepository.getShowShuttleByDestination().first().let {
                binding.showByDestination.isChecked = it
            }
        }
        binding.viewPager.adapter = viewpagerAdapter
        binding.helpFab.setOnClickListener {
            ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleHelpDialogFragment().also {
                findNavController().safeNavigate(it)
            }
        }
        binding.showByDestination.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowByDestination(isChecked)
        }
        binding.showDepartureTime.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowDepartureTime(isChecked)
        }
        binding.noticeViewPager.adapter = noticeAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        stopNameToTabIndex(args.stop)?.let { index ->
            binding.viewPager.setCurrentItem(index, false)
            setClosestStop = true
            honorDeepLinkStop = true
        }
        // If weekdays is from monday to friday and time is before 10:00, it is true and else false
        if (now.dayOfWeek.value in 1..5 && now.hour < 10) {
            Toast.makeText(requireContext(), getString(R.string.shuttle_realtime_toast), Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        viewModel.fetchData()
        viewModel.start()
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.notices.observe(viewLifecycleOwner) { notices ->
            if (notices.isNotEmpty()) {
                binding.noticeLayout.visibility = View.VISIBLE
                (binding.noticeViewPager.adapter as ShuttleNoticeAdapter).updateList(notices)
                autoScrollRunnable = Runnable {
                    if (binding.noticeViewPager.adapter != null && binding.noticeViewPager.adapter!!.itemCount > 0) {
                        currentPosition = (currentPosition + 1) % binding.noticeViewPager.adapter!!.itemCount
                        binding.noticeViewPager.setCurrentItem(currentPosition, true)
                        scrollHandler.postDelayed(autoScrollRunnable, 5000)
                    }
                }
                scrollHandler.postDelayed(autoScrollRunnable, 5000)
            } else {
                binding.noticeLayout.visibility = View.GONE
                if (::autoScrollRunnable.isInitialized) {
                    scrollHandler.removeCallbacks(autoScrollRunnable)
                }
            }
        }
        viewModel.result.observe(viewLifecycleOwner) { stops ->
            if (setClosestStop) {
                return@observe
            }
            if (stops.isNotEmpty()) {
                moveToNearestStop(fusedLocationProviderClient, stops)
            }
        }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.shuttle_realtime_error), Toast.LENGTH_SHORT).show() }
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
        }
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
        if (!honorDeepLinkStop) {
            setClosestStop = false
        }
        if (::autoScrollRunnable.isInitialized) {
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

    @SuppressLint("MissingPermission")
    private fun moveToNearestStop(
        client: FusedLocationProviderClient,
        stops: List<ShuttleRealtimePageQuery.Stop>,
    ) {
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null && isFresh(location)) {
                    selectNearestStop(stops, location)
                } else {
                    requestCurrentLocation(client, stops)
                }
            }
            .addOnFailureListener {
                requestCurrentLocation(client, stops)
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
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                location?.let { selectNearestStop(stops, it) }
            }
            .addOnFailureListener {
                Log.e("ShuttleRealtimeFragment", "Failed to get user location", it)
            }
    }

    private fun selectNearestStop(stops: List<ShuttleRealtimePageQuery.Stop>, location: Location) {
        if (setClosestStop) {
            return
        }
        val nearestStop = stops.map { stopItem ->
            Pair(stopItem, calculateDistance(stopItem, location))
        }.minByOrNull { it.second }?.first
        setClosestStop = true
        Log.d("ShuttleRealtimeFragment", "Nearest stop: ${nearestStop?.name}, distance: ${nearestStop?.let { calculateDistance(it, location) }}")
        binding.viewPager.setCurrentItem(stopNameToTabIndex(nearestStop?.name) ?: 0, false)
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
    }
}
