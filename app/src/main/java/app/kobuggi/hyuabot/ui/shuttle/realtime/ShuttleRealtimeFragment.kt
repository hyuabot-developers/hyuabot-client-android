package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleRealtimeFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentShuttleRealtimeBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleRealtimeViewModel by viewModels()

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
