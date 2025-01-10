package app.kobuggi.hyuabot.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneId
import java.time.ZonedDateTime

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationBarView.OnItemReselectedListener, NavigationBarView.OnItemSelectedListener, DialogInterface.OnDismissListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()
    private val navController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id)!! as NavHostFragment
        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bottomNavigation.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener(this@MainActivity)
            setOnItemReselectedListener(this@MainActivity)
        }
        viewModel.theme.observe(this) {
            when (it) {
                "light" -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
                "dark" -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
                else -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
            }
        }
        checkLocationPermission()
        openBirthDayDialog()
    }

    private fun checkLocationPermission() {
        if (
            ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                Toast.makeText(
                    this,
                    "가까운 정류장을 찾기 위해 위치 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                    1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                    ),
                    1
                )
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == navController.currentDestination?.id) {
            navController.popBackStack(navController.graph.startDestinationId, false)
        } else {
            navController.navigate(item.itemId)
        }
        return true
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val reselectedDestinationId = item.itemId
        navController.popBackStack(reselectedDestinationId, false)
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        recreate()
    }

    private fun openBirthDayDialog() {
        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val lastOpenedYear = pref.getInt("birthDayOpened", 0)
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

        if (now.monthValue == 12 && now.dayOfMonth == 12 && lastOpenedYear != now.year){
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogLayout = R.layout.dialog_birthday
            val dialogView = LayoutInflater.from(this).inflate(dialogLayout,null)
            val dialogCheckBox = dialogView.findViewById<CheckBox>(R.id.do_not_show_checkbox)
            dialogBuilder.setTitle(getString(R.string.dialog_title))
            dialogBuilder.setMessage(getString(R.string.dialog_message))
            dialogBuilder.setView(dialogView)

            dialogBuilder.setPositiveButton("확인") { dialogInterface, _ ->
                if (dialogCheckBox.isChecked){
                    pref.edit().putInt("birthDayOpened", now.year).apply()
                }
                dialogInterface.dismiss()
            }
            dialogBuilder.create().show()
        }
    }
}
