package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class BusRealtimeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BusTabCityFragment()
            1 -> BusTabSeoulFragment()
            2 -> BusTabSuwonFragment()
            3 -> BusTabOtherFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
