package app.kobuggi.hyuabot

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
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
                R.id.fragment_shuttle_timetable, R.id.fragment_bus_timetable -> {
                    binding.bottomNavigationMenu.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigationMenu.visibility = View.VISIBLE
                }
            }
        }
    }
}