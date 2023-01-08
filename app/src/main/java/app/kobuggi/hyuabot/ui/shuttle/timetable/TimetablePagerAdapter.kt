package app.kobuggi.hyuabot.ui.shuttle.timetable

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TimetablePagerAdapter(fragment: TimetableFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return TimetableTab(position)
    }
}