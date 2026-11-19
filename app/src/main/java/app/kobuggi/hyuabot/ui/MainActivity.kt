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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.CheckBox
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.FloatingWindow
import androidx.navigation.NavController
import androidx.navigation.navOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.StartScreen
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import app.kobuggi.hyuabot.service.alarm.ShuttleServiceNoticeScheduler
import app.kobuggi.hyuabot.service.InquiryPush
import app.kobuggi.hyuabot.service.InquiryService
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.AnalyticsScreen
import app.kobuggi.hyuabot.util.AnalyticsScreenDispatcher
import app.kobuggi.hyuabot.util.InAppReviewManager
import app.kobuggi.hyuabot.ui.common.applyGodoTypography
import app.kobuggi.hyuabot.ui.common.applyPermissionDialogButtonColors
import app.kobuggi.hyuabot.ui.bus.realtime.BusQuickSettingsDialog
import app.kobuggi.hyuabot.ui.bus.realtime.BusSeoulTargetStop
import app.kobuggi.hyuabot.ui.home.HomeQuickSettingsDialog
import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination
import app.kobuggi.hyuabot.ui.map.BuildingWebViewSheet
import app.kobuggi.hyuabot.ui.shuttle.realtime.ShuttleAlternativeDisplayMode
import app.kobuggi.hyuabot.ui.shuttle.realtime.ShuttleQuickSettingsDialog
import app.kobuggi.hyuabot.ui.shuttle.via.ShuttleViaSheetDialog
import app.kobuggi.hyuabot.widget.ShuttleWidgetProvider
import app.kobuggi.hyuabot.widget.refreshHyuabotWidgets
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import androidx.core.content.edit
import app.kobuggi.hyuabot.BuildConfig

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationBarView.OnItemReselectedListener, NavigationBarView.OnItemSelectedListener, DialogInterface.OnDismissListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()
    private val navController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id)!! as NavHostFragment
        navHostFragment.navController
    }
    private val analyticsScreenDispatcher = AnalyticsScreenDispatcher {
        AnalyticsManager.logScreen(it, it.id)
    }
    private val locationDisclosurePreferences by lazy {
        getSharedPreferences(LOCATION_DISCLOSURE_PREFERENCES, MODE_PRIVATE)
    }
    private var pendingBackgroundLocationRequest = false
    private var backgroundLocationSettingsOpened = false
    private var backgroundLocationDisclosureDeferredThisSession = false
    private var backgroundLocationDisclosureDialog: AlertDialog? = null
    private var foregroundLocationPermissionAction: (() -> Unit)? = null
    private var foregroundLocationDisclosureShown = false

    private fun logLocationFlow(message: String) {
        if (BuildConfig.DEBUG) Log.d("LocationFlow", message)
    }

    private val foregroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            resetLocationDisclosureDeclineCount(FOREGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT)
            foregroundLocationPermissionAction?.invoke()
            foregroundLocationPermissionAction = null
            logLocationFlow("foreground granted; pending=$pendingBackgroundLocationRequest widget=${hasShuttleWidget()}")
            if (pendingBackgroundLocationRequest || hasShuttleWidget()) {
                pendingBackgroundLocationRequest = true
                maybeRequestBackgroundLocation()
            }
        } else {
            foregroundLocationPermissionAction = null
            pendingBackgroundLocationRequest = false
        }
    }

    @Inject
    lateinit var inAppReviewManager: InAppReviewManager

    @Inject
    lateinit var shuttleServiceNoticeScheduler: ShuttleServiceNoticeScheduler

    @Inject
    lateinit var inquiryService: InquiryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backgroundLocationSettingsOpened =
            savedInstanceState?.getBoolean(BACKGROUND_LOCATION_SETTINGS_OPENED_STATE) == true
        initializeLocationDisclosureSession()
        setContentView(binding.root)
        applyStatusBarStyle(navController.currentDestination?.id)
        binding.bottomNavigation.apply {
            populateBottomNavigationMenu()
            setupWithNavController(navController)
            updateBottomNavigationLabels()
            setOnItemSelectedListener(this@MainActivity)
            setOnItemReselectedListener(this@MainActivity)
        }
        setupTopAppBar()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            applyStatusBarStyle(destination.id)
            updatePrimaryNavigationItem(destination.id)
            screenForDestination(destination.id)?.let {
                analyticsScreenDispatcher.onDestinationChanged(it)
            }
        }
        viewModel.theme.observe(this) {
            when (it) {
                "light" -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
                "dark" -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
                else -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
            }
        }
        syncAppLanguage()
        suggestLanguageIfNeeded()
        openBirthDayDialog()
        requestInAppReview()
        syncShuttleServiceNotices()
        lifecycleScope.launch { inquiryService.syncPushToken() }
        pendingBackgroundLocationRequest =
            intent.getBooleanExtra(EXTRA_REQUEST_BACKGROUND_LOCATION, false) ||
                hasShuttleWidget()
        logLocationFlow("onCreate; pending=$pendingBackgroundLocationRequest widget=${hasShuttleWidget()} foreground=${hasLocationPermission()} background=${hasBackgroundLocationPermission()}")
        val handledDeepLink = (savedInstanceState == null && handleInquiryNotificationIntent(intent)) || handleDebugDeepLink(intent) || navController.handleDeepLink(intent)
        if (savedInstanceState == null && !handledDeepLink) {
            openConfiguredStartScreen()
        }
    }

    /** 설정의 '첫 화면'이 버스·전철·학식이면 앱 시작 때 해당 탭을 연다. 셔틀은 기존 시작 화면 그대로. */
    private fun openConfiguredStartScreen() {
        lifecycleScope.launch {
            val destination = when (viewModel.startScreen()) {
                StartScreen.BUS -> R.id.busRealtimeFragment
                StartScreen.SUBWAY -> R.id.subwayRealtimeFragment
                StartScreen.CAFETERIA -> R.id.cafeteriaFragment
                else -> return@launch
            }
            if (navController.currentDestination?.id == R.id.shuttleRealtimeFragment) {
                navController.navigate(
                    destination,
                    null,
                    navOptions {
                        popUpTo(R.id.shuttleRealtimeFragment) { inclusive = true }
                        launchSingleTop = true
                    },
                )
            }
        }
    }

    private fun setupTopAppBar() {
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.shuttleRealtimeFragment,
                R.id.busRealtimeFragment,
                R.id.subwayRealtimeFragment,
                R.id.cafeteriaFragment,
                R.id.menuFragment
            )
        )
        binding.topAppBar.setupWithNavController(navController, appBarConfiguration)
        val appBarContentColor = ContextCompat.getColor(this, android.R.color.white)
        binding.topAppBar.setNavigationIconTint(appBarContentColor)
        binding.topAppBar.setTitleTextColor(appBarContentColor)
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            if (destination is FloatingWindow) return@addOnDestinationChangedListener
            val title = timetableTitle(destination.id, arguments)
            binding.topAppBar.isVisible = title != null
            if (title != null) {
                binding.topAppBar.title = title
                binding.topAppBar.navigationIcon?.setTint(appBarContentColor)
                (binding.topAppBar.navigationIcon as? DrawerArrowDrawable)?.color = appBarContentColor
            }
        }
    }

    private fun timetableTitle(destinationId: Int, arguments: Bundle?): String? = when (destinationId) {
        R.id.shuttleTimetableFragment -> shuttleTimetableTitle(arguments)
        R.id.busTimetableFragment -> busTimetableTitle(arguments)
        R.id.subwayTimetableFragment -> subwayTimetableTitle(arguments)
        else -> null
    }

    private fun shuttleTimetableTitle(arguments: Bundle?): String {
        val stop = when (val stopId = arguments?.getInt("stopID")) {
            R.string.shuttle_tab_dormitory_out,
            R.string.shuttle_tab_shuttlecock_out,
            R.string.shuttle_tab_station,
            R.string.shuttle_tab_terminal,
            R.string.shuttle_tab_jungang_station,
            R.string.shuttle_tab_shuttlecock_in -> getString(stopId)
            else -> null
        }
        val destination = when (arguments?.getInt("destinationID")) {
            R.string.shuttle_header_bound_for_station -> getString(R.string.shuttle_tab_station)
            R.string.shuttle_header_bound_for_dormitory -> getString(R.string.shuttle_tab_dormitory_out)
            R.string.shuttle_header_bound_for_terminal -> getString(R.string.shuttle_tab_terminal)
            R.string.shuttle_header_bound_for_jungang_station -> getString(R.string.shuttle_tab_jungang_station)
            else -> null
        }
        return if (stop != null && destination != null) {
            getString(R.string.timetable_route_title, stop, destination)
        } else {
            getString(R.string.home_movement_timetable)
        }
    }

    private fun busTimetableTitle(arguments: Bundle?): String {
        val route = when (arguments?.getInt("firstRouteID")) {
            216000068 -> "10-1"
            216000061 -> "3102"
            216000096 -> "3100/3101"
            216000075 -> "50"
            else -> null
        }
        val stop = when (arguments?.getInt("stopID")) {
            216000379 -> R.string.bus_stop_convention
            216000381 -> R.string.bus_stop_cluster
            216000383 -> R.string.bus_stop_dormitory
            216000138 -> R.string.bus_stop_sangnoksu_station
            216000719 -> R.string.bus_stop_main_gate
            216000759 -> R.string.bus_stop_terminal
            213000487 -> R.string.bus_stop_gwangmyeong_station
            else -> null
        }
        return if (route != null && stop != null) {
            getString(R.string.bus_header_format, route, getString(stop))
        } else {
            getString(R.string.home_movement_timetable)
        }
    }

    private fun subwayTimetableTitle(arguments: Bundle?): String {
        val title = when (arguments?.getString("stationID") to arguments?.getString("heading")) {
            "K449" to "up" -> R.string.subway_timetable_title_line4_up
            "K449" to "down" -> R.string.subway_timetable_title_line4_down
            "K251" to "up" -> R.string.subway_timetable_title_suin_up
            "K251" to "down" -> R.string.subway_timetable_title_suin_down
            else -> null
        }
        return title?.let(::getString) ?: getString(R.string.home_movement_timetable)
    }

    override fun onPostResume() {
        super.onPostResume()
        analyticsScreenDispatcher.onResumed()
    }

    override fun onPause() {
        analyticsScreenDispatcher.onPaused()
        super.onPause()
    }

    private fun NavigationBarView.populateBottomNavigationMenu() {
        if (menu.size() > 0) return
        menu.add(0, R.id.shuttleRealtimeFragment, 0, R.string.tabbar_shuttle).setIcon(R.drawable.ic_shuttle)
        menu.add(0, R.id.busRealtimeFragment, 1, R.string.bus).setIcon(R.drawable.ic_bus)
        menu.add(0, R.id.subwayRealtimeFragment, 2, R.string.subway).setIcon(R.drawable.ic_subway)
        menu.add(0, R.id.cafeteriaFragment, 3, R.string.cafeteria).setIcon(R.drawable.ic_cafeteria)
        menu.add(0, R.id.menuFragment, 4, R.string.tabbar_campus).setIcon(R.drawable.ic_campus)
    }

    private fun applyStatusBarStyle(destinationId: Int?) {
        val isSettings = destinationId == R.id.settingFragment || destinationId == R.id.licensesFragment ||
            destinationId == R.id.contactFragment
        val statusBarColor = ContextCompat.getColor(
            this,
            when {
                isSettings -> R.color.md_surface
                else -> R.color.hanyang_blue
            }
        )
        val statusBarStyle = if (isSettings) {
            SystemBarStyle.auto(statusBarColor, statusBarColor)
        } else {
            SystemBarStyle.dark(statusBarColor)
        }
        enableEdgeToEdge(statusBarStyle = statusBarStyle)

        val decorView = window.decorView as? ViewGroup ?: return
        val statusBarBackground = decorView.findViewWithTag<View>(STATUS_BAR_BACKGROUND_TAG)
            ?: View(this).apply {
                tag = STATUS_BAR_BACKGROUND_TAG
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
        statusBarBackground.setBackgroundColor(statusBarColor)
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
        logLocationFlow("onResume; settingsOpened=$backgroundLocationSettingsOpened pending=$pendingBackgroundLocationRequest foreground=${hasLocationPermission()} background=${hasBackgroundLocationPermission()} deferred=$backgroundLocationDisclosureDeferredThisSession")
        if (backgroundLocationSettingsOpened) {
            backgroundLocationDisclosureDialog?.dismiss()
            backgroundLocationDisclosureDialog = null
            backgroundLocationSettingsOpened = false
            if (ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                resetLocationDisclosureDeclineCount(BACKGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT)
                refreshHyuabotWidgets(this)
            }
            return
        }
        handlePendingBackgroundLocationRequest()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BACKGROUND_LOCATION_SETTINGS_OPENED_STATE, backgroundLocationSettingsOpened)
        super.onSaveInstanceState(outState)
    }

    private fun handlePendingBackgroundLocationRequest() {
        logLocationFlow("handlePending; pending=$pendingBackgroundLocationRequest foreground=${hasLocationPermission()} background=${hasBackgroundLocationPermission()} widget=${hasShuttleWidget()}")
        if (!pendingBackgroundLocationRequest) return
        if (hasLocationPermission()) {
            maybeRequestBackgroundLocation()
        } else {
            showForegroundLocationDisclosure()
        }
    }

    private fun updateBottomNavigationLabels() {
        binding.bottomNavigation.menu.findItem(R.id.shuttleRealtimeFragment)?.title = getString(R.string.tabbar_shuttle)
        binding.bottomNavigation.menu.findItem(R.id.busRealtimeFragment)?.title = getString(R.string.tabbar_bus)
        binding.bottomNavigation.menu.findItem(R.id.subwayRealtimeFragment)?.title = getString(R.string.subway)
        binding.bottomNavigation.menu.findItem(R.id.cafeteriaFragment)?.title = getString(R.string.cafeteria)
        binding.bottomNavigation.menu.findItem(R.id.menuFragment)?.title = getString(R.string.tabbar_campus)
    }

    private fun updatePrimaryNavigationItem(destinationId: Int?) {
        updateBottomNavigationLabels()
        val menu = binding.bottomNavigation.menu
        when {
            destinationId.isShuttleDestination() -> menu.findItem(R.id.shuttleRealtimeFragment)?.isChecked = true
            destinationId.isBusDestination() -> menu.findItem(R.id.busRealtimeFragment)?.isChecked = true
            destinationId.isSubwayDestination() -> menu.findItem(R.id.subwayRealtimeFragment)?.isChecked = true
            destinationId.isMoreDestination() -> menu.findItem(R.id.menuFragment)?.isChecked = true
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

    private fun Int?.isBusDestination(): Boolean = when (this) {
        R.id.busRealtimeFragment,
        R.id.busTimetableFragment,
        R.id.busHelpDialogFragment,
        R.id.busStopInfoFragment,
        R.id.busDepartureLogDialogFragment,
        R.id.busRouteInfoDialogFragment -> true
        else -> false
    }

    private fun Int?.isSubwayDestination(): Boolean = when (this) {
        R.id.subwayRealtimeFragment,
        R.id.subwayTimetableFragment -> true
        else -> false
    }

    private fun Int?.isMoreDestination(): Boolean = when (this) {
        R.id.menuFragment,
        R.id.readingRoomFragment,
        R.id.contactFragment,
        R.id.calendarFragment,
        R.id.mapFragment,
        R.id.settingFragment,
        R.id.commuterFragment,
        R.id.licensesFragment -> true
        else -> false
    }

    private fun syncAppLanguage() {
        val language = AppCompatDelegate.getApplicationLocales()[0]?.language
            ?: resources.configuration.locales[0].language
        val prefs = getSharedPreferences("pref", MODE_PRIVATE)
        val applied = prefs.getString(APPLIED_LANGUAGE_KEY, null)
        prefs.edit { putString(APPLIED_LANGUAGE_KEY, language) }
        if (applied != null && applied != language) {
            viewModel.clearLocalizedCaches { refreshHyuabotWidgets(this) }
        }
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

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBackgroundLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasShuttleWidget(): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        return ShuttleWidgetProvider.providerClasses.any { provider ->
            appWidgetManager.getAppWidgetIds(ComponentName(this, provider)).isNotEmpty()
        }
    }

    private fun maybeRequestBackgroundLocation() {
        if (backgroundLocationDisclosureDialog?.isShowing == true) {
            logLocationFlow("maybeBackground skipped; disclosure already showing")
            return
        }
        logLocationFlow("maybeBackground; pending=$pendingBackgroundLocationRequest background=${hasBackgroundLocationPermission()} widget=${hasShuttleWidget()} deferred=$backgroundLocationDisclosureDeferredThisSession")
        if (ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            resetLocationDisclosureDeclineCount(BACKGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT)
            return
        }
        if (!hasShuttleWidget()) {
            pendingBackgroundLocationRequest = false
            return
        }
        if (backgroundLocationDisclosureDeferredThisSession) {
            pendingBackgroundLocationRequest = false
            return
        }
        backgroundLocationDisclosureDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.widget_shuttle_background_location_title))
            .setMessage(getString(R.string.widget_shuttle_background_location_message))
            .setPositiveButton(getString(R.string.widget_shuttle_background_location_allow)) { dialog, _ ->
                backgroundLocationDisclosureDialog?.dismiss()
                backgroundLocationDisclosureDialog = null
                pendingBackgroundLocationRequest = false
                backgroundLocationDisclosureDeferredThisSession = true
                logLocationFlow("background disclosure accepted; opening settings")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    backgroundLocationSettingsOpened = true
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName"),
                        ),
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE,
                    )
                }
            }
            .setNegativeButton(getString(R.string.widget_shuttle_background_location_later)) { dialog, _ ->
                dialog.dismiss()
                backgroundLocationDisclosureDialog = null
                pendingBackgroundLocationRequest = false
                backgroundLocationDisclosureDeferredThisSession = true
            }
            .show()
            .applyGodoTypography()
            .applyPermissionDialogButtonColors()
    }

    private fun showForegroundLocationDisclosure() {
        if (
            foregroundLocationDisclosureShown ||
            isFinishing ||
            isDestroyed ||
            !canShowForegroundLocationDisclosure()
        ) {
            pendingBackgroundLocationRequest = false
            return
        }
        foregroundLocationDisclosureShown = true
        AlertDialog.Builder(this)
            .setTitle(R.string.location_permission_disclosure_title)
            .setMessage(R.string.location_permission_disclosure_message)
            .setPositiveButton(R.string.location_permission_disclosure_allow) { dialog, _ ->
                dialog.dismiss()
                foregroundLocationPermissionLauncher.launch(
                    arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
                )
            }
            .setNegativeButton(R.string.location_permission_disclosure_later) { dialog, _ ->
                dialog.dismiss()
                pendingBackgroundLocationRequest = false
                deferForegroundLocationDisclosure()
            }
            .show()
            .applyGodoTypography()
            .applyPermissionDialogButtonColors()
    }

    fun requestForegroundLocationPermission(onGranted: () -> Unit) {
        if (hasLocationPermission()) {
            onGranted()
            return
        }
        foregroundLocationPermissionAction = onGranted
        showForegroundLocationDisclosure()
    }

    private fun recordLocationDisclosureDecline(key: String) {
        val count = locationDisclosurePreferences.getInt(key, 0)
        locationDisclosurePreferences.edit().putInt(key, count + 1).apply()
    }

    fun canShowForegroundLocationDisclosure(): Boolean {
        return !locationDisclosurePreferences.getBoolean(
            FOREGROUND_LOCATION_DISCLOSURE_DEFERRED,
            false,
        ) &&
            locationDisclosurePreferences.getInt(
                FOREGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT,
                0,
            ) < MAX_LOCATION_DISCLOSURE_DECLINES
    }

    fun deferForegroundLocationDisclosure() {
        locationDisclosurePreferences.edit()
            .putBoolean(FOREGROUND_LOCATION_DISCLOSURE_DEFERRED, true)
            .apply()
        recordLocationDisclosureDecline(FOREGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT)
    }

    private fun initializeLocationDisclosureSession() {
        if (locationDisclosureSessionInitialized) return
        locationDisclosurePreferences.edit()
            .remove(FOREGROUND_LOCATION_DISCLOSURE_DEFERRED)
            .remove(BACKGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT)
            .apply()
        locationDisclosureSessionInitialized = true
    }

    private fun resetLocationDisclosureDeclineCount(key: String) {
        locationDisclosurePreferences.edit().putInt(key, 0).apply()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_REQUEST_BACKGROUND_LOCATION, false)) {
            pendingBackgroundLocationRequest = true
        }
        handleInquiryNotificationIntent(intent) || handleDebugDeepLink(intent) || navController.handleDeepLink(intent)
        binding.root.post(::handlePendingBackgroundLocationRequest)
    }

    private fun handleDebugDeepLink(intent: Intent): Boolean {
        val uri = DebugDeepLinkRouter.uriFrom(intent) ?: return false
        if (DebugDeepLinkRouter.navigate(uri, navController)) return true

        val route = uri.pathSegments.firstOrNull()?.lowercase() ?: return true
        when (route) {
            "home-quick-settings" -> HomeQuickSettingsDialog.newInstance(
                showPresenceStatus = true,
                showBus50Transfer = true,
                showSubwayTransfer = true,
                subwayTransferDestination = HomeSubwayTransferDestination.SEOUL,
                showSeoulBusStop = true,
                seoulBusStop = BusSeoulTargetStop.GANGNAM,
            ).show(supportFragmentManager, "debug-home-quick-settings")

            "bus-quick-settings" -> BusQuickSettingsDialog.newInstance(
                showSecondaryEta = true,
                seoulTarget = BusSeoulTargetStop.GANGNAM,
            ).show(supportFragmentManager, "debug-bus-quick-settings")

            "shuttle-quick-settings" -> ShuttleQuickSettingsDialog.newInstance(
                showByDestination = true,
                showDepartureTime = false,
                showPresenceStatus = true,
                showBusTransfer = true,
                showSubwayTransfer = true,
                subwayDestination = HomeSubwayTransferDestination.SEOUL,
                alternativeMode = ShuttleAlternativeDisplayMode.AUTOMATIC,
            ).show(supportFragmentManager, "debug-shuttle-quick-settings")

            "shuttle-via" -> ShuttleViaSheetDialog().show(
                supportFragmentManager,
                "debug-shuttle-via",
            )

            "building-webview" -> BuildingWebViewSheet.newInstance(
                title = uri.getQueryParameter("title") ?: "Debug WebView",
                url = uri.getQueryParameter("url") ?: "https://hyuabot.app",
            ).show(supportFragmentManager, "debug-building-webview")

            else -> Unit
        }
        return true
    }

    private fun handleInquiryNotificationIntent(intent: Intent): Boolean {
        val open = InquiryPush.isInquiryLaunch(
            intent.getBooleanExtra(InquiryPush.EXTRA_OPEN_INQUIRY, false),
            intent.getStringExtra(InquiryPush.DATA_TYPE_KEY)
        )
        if (!open) return false
        intent.removeExtra(InquiryPush.EXTRA_OPEN_INQUIRY)
        intent.removeExtra(InquiryPush.DATA_TYPE_KEY)
        if (navController.currentDestination?.id != R.id.inquiryChatFragment) {
            navController.navigate(R.id.inquiryChatFragment)
        }
        NotificationManagerCompat.from(this).cancel(InquiryPush.NOTIFICATION_ID)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        tabItemForDestination(item.itemId)?.let {
            AnalyticsManager.logSelect(it, AnalyticsContentType.TAB)
        }
        return item.onNavDestinationSelected(navController)
    }

    /** Maps a nav-graph destination id to its analytics screen (null = not tracked as a screen). */
    private fun screenForDestination(destinationId: Int): AnalyticsScreen? = when (destinationId) {
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
        R.id.menuFragment -> AnalyticsScreen.CAMPUS
        R.id.commuterFragment -> AnalyticsScreen.COMMUTER_BUS
        else -> null
    }

    /** Maps a bottom-navigation item id to its analytics tab item. */
    private fun tabItemForDestination(destinationId: Int): AnalyticsItem? = when (destinationId) {
        R.id.shuttleRealtimeFragment -> AnalyticsItem.TAB_SHUTTLE
        R.id.busRealtimeFragment -> AnalyticsItem.TAB_BUS
        R.id.subwayRealtimeFragment -> AnalyticsItem.TAB_SUBWAY
        R.id.cafeteriaFragment -> AnalyticsItem.TAB_CAFETERIA
        R.id.menuFragment -> AnalyticsItem.TAB_CAMPUS
        else -> null
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        tabItemForDestination(item.itemId)?.let {
            AnalyticsManager.logSelect(it, AnalyticsContentType.TAB)
        }
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
        private const val APPLIED_LANGUAGE_KEY = "appliedLanguage"
        const val EXTRA_REQUEST_BACKGROUND_LOCATION = "request_background_location"
        private const val BACKGROUND_LOCATION_SETTINGS_OPENED_STATE =
            "background_location_settings_opened"
        const val LOCATION_DISCLOSURE_PREFERENCES = "location_disclosure"
        const val FOREGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT = "foreground_decline_count"
        const val FOREGROUND_LOCATION_DISCLOSURE_DEFERRED = "foreground_deferred"
        const val BACKGROUND_LOCATION_DISCLOSURE_DECLINE_COUNT = "background_decline_count"
        const val MAX_LOCATION_DISCLOSURE_DECLINES = 3
        private var locationDisclosureSessionInitialized = false
        private const val STATUS_BAR_BACKGROUND_TAG = "status_bar_background"
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}
