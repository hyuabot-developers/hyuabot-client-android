package app.kobuggi.hyuabot

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    inner class ShuttleStopTabAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = ShuttleData.countOfStop()
        override fun createFragment(position: Int) = ShuttleStopTab().newInstance(position)
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val vm by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.pager.adapter = ShuttleStopTabAdapter()
        TabLayoutMediator(binding.dotIndicator, binding.pager) { tab, _ ->
            tab.text = ""
        }.attach()
        binding.refreshButton.setOnClickListener {
            vm.getArrivalList()
        }

        vm.getArrivalList()
        vm.start()
    }

    override fun onPause() {
        super.onPause()
        vm.stop()
    }

    override fun onResume() {
        super.onResume()
        vm.start()
    }
}
