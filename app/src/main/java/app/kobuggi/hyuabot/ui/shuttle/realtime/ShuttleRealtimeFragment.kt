package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeBinding
import app.kobuggi.hyuabot.service.safeNavigate
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class ShuttleRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleRealtimeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val now = LocalDateTime.now()
        val viewpagerAdapter = ShuttleRealtimeViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.shuttle_tab_dormitory_out,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_tab_station,
            R.string.shuttle_tab_terminal,
            R.string.shuttle_tab_jungang_station,
            R.string.shuttle_tab_shuttlecock_in
        )
        binding.viewPager.adapter = viewpagerAdapter
        binding.helpFab.setOnClickListener {
            ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleHelpDialogFragment().also {
                findNavController().safeNavigate(it)
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
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
        viewModel.stopInfo.observe(viewLifecycleOwner) {stops ->
            if (stops.isNotEmpty()) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (location == null) {
                        Toast.makeText(requireContext(), getString(R.string.shuttle_realtime_location_error), Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    val nearestStop = stops.mapIndexed { index, stopItem ->
                        Pair(stopItem, calculateDistance(stopItem, location))
                    }.minByOrNull { it.second }?.first
                    when(nearestStop?.name) {
                        "dormitory_o" -> binding.viewPager.setCurrentItem(0, false)
                        "shuttlecock_o" -> binding.viewPager.setCurrentItem(1, false)
                        "station" -> binding.viewPager.setCurrentItem(2, false)
                        "terminal" -> binding.viewPager.setCurrentItem(3, false)
                        "jungang_stn" -> binding.viewPager.setCurrentItem(4, false)
                        "shuttlecock_i" -> binding.viewPager.setCurrentItem(5, false)
                        else -> binding.viewPager.setCurrentItem(0, false)
                    }
                }
            }
        }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.shuttle_realtime_error), Toast.LENGTH_SHORT).show() }
        }
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
        binding.viewPager.adapter = null
    }

    private fun calculateDistance(stopItem: ShuttleRealtimePageQuery.Stop, location: Location): Double {
        val distance = sqrt(
        (stopItem.latitude - location.latitude) * (stopItem.latitude - location.latitude) +
            (stopItem.longitude - location.longitude) * (stopItem.longitude - location.longitude)
        )
        return distance
    }
}
