package app.kobuggi.hyuabot.ui.cafeteria

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class CafeteriaViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CafeteriaTabBreakfastFragment()
            1 -> CafeteriaTabLunchFragment()
            2 -> CafeteriaTabDinnerFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
