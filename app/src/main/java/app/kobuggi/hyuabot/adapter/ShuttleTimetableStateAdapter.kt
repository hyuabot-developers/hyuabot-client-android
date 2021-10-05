package app.kobuggi.hyuabot.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.kobuggi.hyuabot.fragment.ShuttleTimetableWeekdaysFragment

class ShuttleTimetableStateAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ShuttleTimetableWeekdaysFragment()
            else -> ShuttleTimetableWeekdaysFragment()
        }
    }
}