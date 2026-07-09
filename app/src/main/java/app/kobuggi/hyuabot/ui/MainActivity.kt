package app.kobuggi.hyuabot.ui

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import app.kobuggi.hyuabot.service.alarm.ShuttleServiceNoticeScheduler
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsScreen
import app.kobuggi.hyuabot.util.InAppReviewManager
import app.kobuggi.hyuabot.ui.common.applyGodoTypography
import app.kobuggi.hyuabot.widget.ShuttleWidgetProvider
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationBarView.OnItemReselectedListener, NavigationBarView.OnItemSelectedListener, DialogInterface.OnDismissListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()
    private val navController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id)!! as NavHostFragment
        navHostFragment.navController
    }
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var inAppReviewManager: InAppReviewManager

    @Inject
    lateinit var shuttleServiceNoticeScheduler: ShuttleServiceNoticeScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyStatusBarStyle()
        firebaseAnalytics = Firebase.analytics
        binding.bottomNavigation.apply {
            setupWithNavController(navController)
            updateBottomNavigationLabels()
            setOnItemSelectedListener(this@MainActivity)
            setOnItemReselectedListener(this@MainActivity)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updatePrimaryNavigationItem(destination.id)
            screenForDestination(destination.id)?.let {
                AnalyticsManager.logScreen(it, destination.label?.toString())
            }
        }
        viewModel.theme.observe(this) {
            when (it) {
                "light" -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
                "dark" -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
                else -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
            }
        }
        suggestLanguageIfNeeded()
        checkLocationPermission()
        openBirthDayDialog()
        requestInAppReview()
        syncShuttleServiceNotices()
        navController.handleDeepLink(intent)
    }

    private fun applyStatusBarStyle() {
        val statusBarColor = ContextCompat.getColor(this, R.color.hanyang_blue)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(statusBarColor))

        val decorView = window.decorView as? ViewGroup ?: return
        val statusBarBackground = decorView.findViewWithTag<View>(STATUS_BAR_BACKGROUND_TAG)
            ?: View(this).apply {
                tag = STATUS_BAR_BACKGROUND_TAG
                setBackgroundColor(statusBarColor)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                decorView.addView(
                    this,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        statusBarHeight(),
                        Gravity.TOP
                    )
                )
            }
        statusBarBackground.layoutParams = statusBarBackground.layoutParams.apply {
            height = statusBarHeight()
        }
    }

    private fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    override fun onResume() {
        super.onResume()
        updatePrimaryNavigationItem(navController.currentDestination?.id)
    }

    private fun updateBottomNavigationLabels() {
        binding.bottomNavigation.menu.findItem(R.id.busRealtimeFragment)?.title = getString(R.string.tabbar_bus)
        binding.bottomNavigation.menu.findItem(R.id.subwayRealtimeFragment)?.title = getString(R.string.subway)
        binding.bottomNavigation.menu.findItem(R.id.cafeteriaFragment)?.title = getString(R.string.cafeteria)
        binding.bottomNavigation.menu.findItem(R.id.menuFragment)?.title = getString(R.string.tabbar_more)
    }

    private fun updatePrimaryNavigationItem(destinationId: Int?) {
        updateBottomNavigationLabels()
        val primaryItem = binding.bottomNavigation.menu.findItem(R.id.homeFragment) ?: return
        val moreItem = binding.bottomNavigation.menu.findItem(R.id.menuFragment)
        if (destinationId.isShuttleDestination()) {
            primaryItem.title = getString(R.string.shuttle_bus)
            primaryItem.setIcon(R.drawable.ic_shuttle_bus)
            primaryItem.isChecked = true
        } else {
            primaryItem.title = getString(R.string.home)
            primaryItem.setIcon(R.drawable.ic_home)
            when {
                destinationId == R.id.homeFragment -> primaryItem.isChecked = true
                destinationId.isMoreDestination() -> moreItem?.isChecked = true
            }
        }
    }

    private fun Int?.isShuttleDestination(): Boolean = when (this) {
        R.id.shuttleRealtimeFragment,
        R.id.shuttleTimetableFragment,
        R.id.shuttleStopDialogFragment,
        R.id.shuttleHelpDialogFragment,
        R.id.shuttleTimetableDialogFragment,
        R.id.shuttleTimetableFilterDialogFragment -> true
        else -> false
    }

    private fun Int?.isMoreDestination(): Boolean = when (this) {
        R.id.menuFragment,
        R.id.readingRoomFragment,
        R.id.contactFragment,
        R.id.calendarFragment,
        R.id.mapFragment,
        R.id.settingFragment,
        R.id.languageSettingDialogFragment,
        R.id.campusSettingDialogFragment,
        R.id.themeSettingDialogFragment,
        R.id.settingDeveloperDialogFragment -> true
        else -> false
    }

    private fun suggestLanguageIfNeeded() {
        val prefs = getSharedPreferences("pref", MODE_PRIVATE)
        if (prefs.getBoolean("languageSuggestionShown", false)) return
        prefs.edit { putBoolean("languageSuggestionShown", true) }

        val systemLang = resources.configuration.locales[0].language
        if (systemLang != "ja" && systemLang != "zh") return

        val appLocales = AppCompatDelegate.getApplicationLocales()
        val appLang = appLocales.get(0)?.language

        if (appLang == "en") {
            val targetTag = if (systemLang == "ja") "ja-JP" else "zh-CN"
            val targetLangName = getString(if (systemLang == "ja") R.string.language_japanese else R.string.language_chinese)
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.language_suggestion_title))
                .setMessage(getString(R.string.language_suggestion_message, targetLangName))
                .setPositiveButton(targetLangName) { _, _ ->
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetTag))
                }
                .setNegativeButton(getString(R.string.language_suggestion_keep_english), null)
                .show()
                .applyGodoTypography()
        } else if (appLang != "ja" && appLang != "zh") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
    }

    private fun requestInAppReview() {
        lifecycleScope.launch {
            inAppReviewManager.maybeRequestReview(this@MainActivity)
        }
    }

    private fun syncShuttleServiceNotices() {
        lifecycleScope.launch {
            shuttleServiceNoticeScheduler.sync()
        }
    }

    private fun checkLocationPermission() {
        if (
            ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_nearest_stop),
                    Toast.LENGTH_SHORT
                ).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            maybeRequestBackgroundLocation()
        }
    }

    private fun maybeRequestBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val hasShuttleWidget = ShuttleWidgetProvider.providerClasses.any { provider ->
            appWidgetManager.getAppWidgetIds(ComponentName(this, provider)).isNotEmpty()
        }
        if (!hasShuttleWidget) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.widget_shuttle_background_location_title))
            .setMessage(getString(R.string.widget_shuttle_background_location_message))
            .setPositiveButton(getString(R.string.widget_shuttle_background_location_allow)) { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton(getString(R.string.widget_shuttle_background_location_later)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
            .applyGodoTypography()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) {
            maybeRequestBackgroundLocation()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        tabItemForDestination(item.itemId)?.let {
            AnalyticsManager.logSelect(it, AnalyticsContentType.TAB)
        }
        if (item.itemId == navController.currentDestination?.id) {
            navController.popBackStack(navController.graph.startDestinationId, false)
        } else {
            navController.navigate(item.itemId)
        }
        return true
    }

    /** Maps a nav-graph destination id to its analytics screen (null = not tracked as a screen). */
    private fun screenForDestination(destinationId: Int): AnalyticsScreen? = when (destinationId) {
        R.id.homeFragment -> AnalyticsScreen.SHUTTLE_REALTIME
        R.id.shuttleRealtimeFragment -> AnalyticsScreen.SHUTTLE_REALTIME
        R.id.shuttleTimetableFragment -> AnalyticsScreen.SHUTTLE_TIMETABLE
        R.id.shuttleStopDialogFragment -> AnalyticsScreen.SHUTTLE_STOP_INFO
        R.id.shuttleHelpDialogFragment -> AnalyticsScreen.SHUTTLE_HELP
        R.id.shuttleTimetableDialogFragment -> AnalyticsScreen.SHUTTLE_STOP_TIMETABLE
        R.id.shuttleTimetableFilterDialogFragment -> AnalyticsScreen.SHUTTLE_TIMETABLE_FILTER
        R.id.busRealtimeFragment -> AnalyticsScreen.BUS_REALTIME
        R.id.busTimetableFragment -> AnalyticsScreen.BUS_TIMETABLE
        R.id.busHelpDialogFragment -> AnalyticsScreen.BUS_HELP
        R.id.busStopInfoFragment -> AnalyticsScreen.BUS_STOP_INFO
        R.id.busDepartureLogDialogFragment -> AnalyticsScreen.BUS_DEPARTURE_LOG
        R.id.busRouteInfoDialogFragment -> AnalyticsScreen.BUS_ROUTE_INFO
        R.id.subwayRealtimeFragment -> AnalyticsScreen.SUBWAY_REALTIME
        R.id.subwayTimetableFragment -> AnalyticsScreen.SUBWAY_TIMETABLE
        R.id.cafeteriaFragment -> AnalyticsScreen.CAFETERIA
        R.id.readingRoomFragment -> AnalyticsScreen.READING_ROOM
        R.id.mapFragment -> AnalyticsScreen.MAP
        R.id.settingFragment -> AnalyticsScreen.SETTING
        R.id.noticeWebViewFragment -> AnalyticsScreen.WEB_VIEW
        R.id.contactFragment -> AnalyticsScreen.CONTACT
        R.id.calendarFragment -> AnalyticsScreen.CALENDAR
        R.id.menuFragment -> AnalyticsScreen.MENU
        R.id.languageSettingDialogFragment -> AnalyticsScreen.SETTING_LANGUAGE
        R.id.campusSettingDialogFragment -> AnalyticsScreen.SETTING_CAMPUS
        R.id.themeSettingDialogFragment -> AnalyticsScreen.SETTING_THEME
        R.id.settingDeveloperDialogFragment -> AnalyticsScreen.SETTING_DEVELOPER
        else -> null
    }

    /** Maps a bottom-navigation item id to its analytics tab item. */
    private fun tabItemForDestination(destinationId: Int): AnalyticsItem? = when (destinationId) {
        R.id.homeFragment -> AnalyticsItem.TAB_SHUTTLE
        R.id.busRealtimeFragment -> AnalyticsItem.TAB_BUS
        R.id.subwayRealtimeFragment -> AnalyticsItem.TAB_SUBWAY
        R.id.cafeteriaFragment -> AnalyticsItem.TAB_CAFETERIA
        R.id.menuFragment -> AnalyticsItem.TAB_MENU
        else -> null
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

            dialogBuilder.setPositiveButton(R.string.confirm) { dialogInterface, _ ->
                if (dialogCheckBox.isChecked){
                    AnalyticsManager.logSelect(AnalyticsItem.BIRTHDAY_DO_NOT_SHOW)
                    pref.edit { putInt("birthDayOpened", now.year) }
                }
                AnalyticsManager.logSelect(AnalyticsItem.BIRTHDAY_DISMISS)
                dialogInterface.dismiss()
            }
            AnalyticsManager.logScreen(AnalyticsScreen.BIRTHDAY)
            dialogBuilder.create().apply {
                show()
                applyGodoTypography()
            }
        }
    }

    companion object {
        private const val STATUS_BAR_BACKGROUND_TAG = "status_bar_background"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}
