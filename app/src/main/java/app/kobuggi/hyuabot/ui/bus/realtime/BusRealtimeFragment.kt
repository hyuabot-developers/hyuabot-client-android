package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentBusRealtimeBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BusRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentBusRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: BusRealtimeViewModel by viewModels()

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


        val viewpagerAdapter = BusRealtimeViewPagerAdapter(childFragmentManager, lifecycle)
        val tabLabelList = listOf(
            R.string.bus_tab_city,
            R.string.bus_tab_seoul,
            R.string.bus_tab_suwon,
            R.string.bus_tab_other
        )
        binding.viewPager.adapter = viewpagerAdapter
        binding.helpFab.setOnClickListener {
            BusRealtimeFragmentDirections.actionBusRealtimeFragmentToBusTimetableFragment().also {
                findNavController().navigate(it)
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(tabLabelList[position])
        }.attach()
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
        binding.viewPager.adapter = null
    }
}
