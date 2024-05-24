package app.kobuggi.hyuabot.ui.subway.timetable

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class SubwayTimetableViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SubwayTabWeekdaysFragment()
            1 -> SubwayTabWeekendsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
