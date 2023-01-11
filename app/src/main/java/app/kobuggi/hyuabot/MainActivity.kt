package app.kobuggi.hyuabot

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import app.kobuggi.hyuabot.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DialogInterface.OnDismissListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bottomNavigationMenu.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{
            _, destination, _ ->
            when(destination.id){
                R.id.fragment_shuttle_timetable, R.id.fragment_bus_timetable, R.id.fragment_subway_timetable -> {
                    binding.bottomNavigationMenu.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigationMenu.visibility = View.VISIBLE
                }
            }
        }
        val sharedPreferences = getSharedPreferences("hyuabot", MODE_PRIVATE)
        val localeCode = sharedPreferences.getString("locale", "")
        LocaleHelper.setLocale(localeCode!!)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
    }
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.wrap(newBase!!))
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        recreate()
    }
}