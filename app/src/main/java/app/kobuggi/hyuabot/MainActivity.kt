package app.kobuggi.hyuabot

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import app.kobuggi.hyuabot.util.LocaleHelper
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneId
import java.time.ZonedDateTime

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DialogInterface.OnDismissListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()
    private val assetPackManager by lazy { AssetPackManagerFactory.getInstance(GlobalApplication.instance) }
    private val firebaseAnalytics by lazy { Firebase.analytics }
    private val fastFollowAssetPack = "fast_follow_pack"
    private val navController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bottomNavigationMenu.setupWithNavController(navController)
        initAssetPackManager()
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
        askNotificationPermission()
        openBirthDayDialog()
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

    private fun initAssetPackManager() {
        val assetPath = getAbsolutePath(fastFollowAssetPack, "app.db")
        if (assetPath != null) {
            viewModel.upgradeDatabase(assetPath)
        }
        registerListener()
    }

    private fun getAbsolutePath(assetPackName: String, relativeAssetPath: String): String? {
        val assetPackPath = assetPackManager.getPackLocation(assetPackName) ?: return null

        val assetsFolderPath = assetPackPath.assetsPath()
        return "$assetsFolderPath/$relativeAssetPath"
    }

    private fun registerListener(){
        val fastFollowAssetPackPath = getAbsolutePath(fastFollowAssetPack, "")
        if (fastFollowAssetPackPath == null){
            assetPackManager.registerListener(assetPackStateUpdateListener)
            val assetPackList = arrayListOf<String>()
            assetPackList.add(fastFollowAssetPack)
            assetPackManager.fetch(assetPackList)
        } else {
            initFastFollow()
        }
    }

    private val assetPackStateUpdateListener =
        AssetPackStateUpdateListener { state ->
            when(state.status()){
                AssetPackStatus.PENDING -> {
                    Log.i("AssetPackManager", "PENDING")
                }
                AssetPackStatus.DOWNLOADING -> {
                    Log.i("AssetPackManager", "DOWNLOADING")
                }
                AssetPackStatus.COMPLETED -> {
                    Log.i("AssetPackManager", "INSTALLED")
                    initFastFollow()
                    recreate()
                }
                else -> {
                    Log.i("AssetPackManager", "UNKNOWN")
                }
            }
        }

    private fun initFastFollow() {
        val assetsPath = getAbsolutePath(fastFollowAssetPack, "app.db")
        viewModel.initializeDatabase(assetsPath!!)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            askNotificationPermission()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                Toast.makeText(this, "알림 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getAnalytics() = firebaseAnalytics

    private fun openBirthDayDialog() {
        val pref = getSharedPreferences("pref", Activity.MODE_PRIVATE)
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