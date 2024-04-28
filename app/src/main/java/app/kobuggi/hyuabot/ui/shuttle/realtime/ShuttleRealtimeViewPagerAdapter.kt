package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ShuttleRealtimeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ShuttleTabDormitoryFragment()
            1 -> ShuttleTabShuttlecockOutFragment()
            2 -> ShuttleTabStationFragment()
            3 -> ShuttleTabTerminalFragment()
            4 -> ShuttleTabJungangStationFragment()
            5 -> ShuttleTabShuttlecockInFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
