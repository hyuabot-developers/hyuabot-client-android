package app.kobuggi.hyuabot.ui.subway.realtime

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class SubwayRealtimeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SubwayTabBlueFragment()
            1 -> SubwayTabYellowFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
