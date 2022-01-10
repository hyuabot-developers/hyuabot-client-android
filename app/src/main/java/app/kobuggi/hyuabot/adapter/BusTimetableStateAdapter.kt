package app.kobuggi.hyuabot.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.kobuggi.hyuabot.fragment.BusTimetableFragment
import app.kobuggi.hyuabot.fragment.ShuttleTimetableFragment
import app.kobuggi.hyuabot.model.BusTimeTableItem
import app.kobuggi.hyuabot.model.ShuttleItem

class BusTimetableStateAdapter(private val fragmentActivity: FragmentActivity, private val weekdaysTimetable: List<BusTimeTableItem>, private val saturdayTimetable: List<BusTimeTableItem>, private val sundayTimetable: List<BusTimeTableItem>) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> BusTimetableFragment(weekdaysTimetable)
            1 -> BusTimetableFragment(saturdayTimetable)
            else -> BusTimetableFragment(sundayTimetable)
        }
    }
}