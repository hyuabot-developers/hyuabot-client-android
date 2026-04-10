package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeBinding
import app.kobuggi.hyuabot.service.safeNavigate
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Runnable
import javax.inject.Inject

@AndroidEntryPoint
class BusRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: BusRealtimeViewModel by viewModels()
    private var currentPosition = 0
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
            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusStopDialogFragment().also {
                findNavController().safeNavigate(it)
            }
        }
        binding.noticeViewPager.adapter = noticeAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
        return binding.root
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
        if (::autoScrollRunnable.isInitialized) {
            scrollHandler.postDelayed(autoScrollRunnable, 5000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
    }
}
