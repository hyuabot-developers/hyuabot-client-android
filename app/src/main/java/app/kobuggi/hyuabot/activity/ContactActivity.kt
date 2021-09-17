package app.kobuggi.hyuabot.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.adapter.ContactQueryResultListAdapter
import app.kobuggi.hyuabot.function.AppDatabase
import app.kobuggi.hyuabot.model.DatabaseItem
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint

class ContactActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout : DrawerLayout
    lateinit var searchView: SearchView
    lateinit var queryResult: ArrayList<DatabaseItem>
    lateinit var contactQueryResultListAdapter: ContactQueryResultListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        val toolbar = findViewById<Toolbar>(R.id.contact_app_bar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.contact_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.contact_navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        val database = Room.databaseBuilder(this, AppDatabase::class.java, "app.db")
            .createFromAsset("app.db")
            .build()

        val contactQueryListView = findViewById<RecyclerView>(R.id.contact_card_list)
        val getQueryAllItems = GlobalScope.launch {
            queryResult = ArrayList()
            for(item : DatabaseItem in database.databaseHelper()!!.getPhoneNumberAll()){
                if(item.phoneNumber!!.trim().isNotEmpty()){
                    Log.d("Phone", item.phoneNumber.toString())
                    queryResult.add(item)
                }
            }
            contactQueryResultListAdapter = ContactQueryResultListAdapter(queryResult)
            contactQueryListView.layoutManager = LinearLayoutManager(this@ContactActivity, RecyclerView.VERTICAL, false)
            contactQueryListView.adapter = contactQueryResultListAdapter
            Log.d("Count of result", queryResult.size.toString())
        }

        runBlocking {
            getQueryAllItems.join()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.contact_action_bar_menu, menu!!)

        val searchItem = menu.findItem(R.id.contact_search)
        searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val viewID = item.itemId
        when(viewID){
            R.id.contact_search -> {}
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        return false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.contact_on_campus -> {
                Toast.makeText(this, "교내 기관 목록입니다.", Toast.LENGTH_SHORT).show()
            }
            R.id.contact_restaurant -> {
                Toast.makeText(this, "교외 식당 목록입니다.", Toast.LENGTH_SHORT).show()
            }
            R.id.contact_bakery -> {
                Toast.makeText(this, "교외 빵집 목록입니다.", Toast.LENGTH_SHORT).show()
            }
            R.id.contact_cafe -> {
                Toast.makeText(this, "교외 카페 목록입니다.", Toast.LENGTH_SHORT).show()
            }
            R.id.contact_pub -> {
                Toast.makeText(this, "교외 주점 목록입니다.", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(searchView.isEnabled){
            searchView.isEnabled = false
        } else {
            super.onBackPressed()
        }
    }
}