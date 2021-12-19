package app.kobuggi.hyuabot.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.kobuggi.hyuabot.fragment.ShuttleTimetableFragment
import app.kobuggi.hyuabot.model.ShuttleItem

class ShuttleTimetableStateAdapter(private val fragmentActivity: FragmentActivity, private val weekdaysTimetable: List<ShuttleItem>, private val weekendsTimetable: List<ShuttleItem>) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ShuttleTimetableFragment(weekdaysTimetable)
            else -> ShuttleTimetableFragment(weekendsTimetable)
        }
    }
}