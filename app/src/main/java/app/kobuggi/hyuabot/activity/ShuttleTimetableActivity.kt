package app.kobuggi.hyuabot.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.ShuttleTimetableStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ShuttleTimetableActivity : AppCompatActivity() {
    lateinit var viewPager2: ViewPager2
    lateinit var tabLayout: TabLayout
    private val tabTextList = arrayListOf("평일", "주말")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shuttle_timetable)

        viewPager2 = findViewById(R.id.shuttle_timetable_tab_pager)
        tabLayout = findViewById(R.id.shuttle_timetable_tab_layout)
        viewPager2.adapter = ShuttleTimetableStateAdapter(this)
        TabLayoutMediator(tabLayout, viewPager2){
            tab, position -> tab.text = tabTextList[position]
        }.attach()
    }
}