package app.kobuggi.hyuabot.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.ContactFragmentStateAdapter
import app.kobuggi.hyuabot.function.DatabaseHelper
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ContactActivity : FragmentActivity() {
    lateinit var databaseHelper : DatabaseHelper
    lateinit var viewPager2 : ViewPager2
    lateinit var tabLayout: TabLayout
    private val tabTextList = arrayListOf("교내", "교외")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        databaseHelper = DatabaseHelper(this)
        viewPager2 = findViewById(R.id.contact_tab_pager)
        tabLayout = findViewById(R.id.contact_tab_layout)
        init()
    }

    private fun init(){
        viewPager2.adapter = ContactFragmentStateAdapter(this)
        TabLayoutMediator(tabLayout, viewPager2){
            tab, position -> tab.text = tabTextList[position]
        }.attach()
    }
}