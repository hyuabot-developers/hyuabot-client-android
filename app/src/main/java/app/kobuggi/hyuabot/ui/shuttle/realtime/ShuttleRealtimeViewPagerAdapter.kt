package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ShuttleRealtimeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ShuttleRealtimeDormitoryFragment()
            1 -> ShuttleRealtimeDormitoryFragment()
            2 -> ShuttleRealtimeDormitoryFragment()
            3 -> ShuttleRealtimeDormitoryFragment()
            4 -> ShuttleRealtimeDormitoryFragment()
            5 -> ShuttleRealtimeDormitoryFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
