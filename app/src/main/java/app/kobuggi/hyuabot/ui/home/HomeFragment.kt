package app.kobuggi.hyuabot.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.HomePageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentHomeBinding
import app.kobuggi.hyuabot.databinding.ItemHomeRowBinding
import app.kobuggi.hyuabot.databinding.ItemHomeTransferRowBinding
import app.kobuggi.hyuabot.util.localizedSubwayStationName
import app.kobuggi.hyuabot.util.setSkeletonLoading
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Duration
import java.util.Date
import kotlin.math.ceil

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels()
    private val homeTypeface by lazy { ResourcesCompat.getFont(requireContext(), R.font.godo) }
    private var selectedDeparture = HomeDeparture.DORMITORY
    private var selectedDestination = HomeDestination.STATION
    private var lockDepartureSelection = false
    private var debugSubwayTransferDestination: HomeSubwayTransferDestination? = null
    private var noticePosition = 0
    private var noticeManuallyScrolled = false
    private var locationCancellationTokenSource: CancellationTokenSource? = null
    private lateinit var noticeAutoScrollRunnable: Runnable
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val noticeScrollHandler = Handler(Looper.getMainLooper())
    private val autoRefreshRunnable = object : Runnable {
        override fun run() {
            refreshHome()
            refreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL_MILLIS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        applyDebugRouteOverride()
        binding.dateText.text = DateFormat.getDateInstance(DateFormat.FULL).format(Date())
        setupDestinationButtons()
        binding.homeSwipeRefreshLayout.setOnRefreshListener {
            refreshHome()
        }
        binding.homeSwipeRefreshLayout.setColorSchemeResources(R.color.hanyang_blue)
        binding.movementDetail.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_shuttleRealtimeFragment)
        }
        binding.legacyShuttleButton.setOnClickListener {
            openQuickSettings()
        }
        setupNotices()
        childFragmentManager.setFragmentResultListener(
            HomeQuickSettingsDialog.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            if (result.getBoolean(HomeQuickSettingsDialog.KEY_OPEN_LEGACY_SHUTTLE, false)) {
                findNavController().navigate(R.id.action_homeFragment_to_shuttleRealtimeFragment)
            }
            if (result.containsKey(HomeQuickSettingsDialog.KEY_SHOW_BUS50_TRANSFER)) {
                viewModel.setShowBus50Transfer(result.getBoolean(HomeQuickSettingsDialog.KEY_SHOW_BUS50_TRANSFER))
            }
            if (result.containsKey(HomeQuickSettingsDialog.KEY_SHOW_SUBWAY_TRANSFER)) {
                viewModel.setShowSubwayTransfer(result.getBoolean(HomeQuickSettingsDialog.KEY_SHOW_SUBWAY_TRANSFER))
            }
            if (result.containsKey(HomeQuickSettingsDialog.KEY_SUBWAY_TRANSFER_DESTINATION)) {
                debugSubwayTransferDestination = null
                viewModel.setSubwayTransferDestination(
                    HomeSubwayTransferDestination.from(result.getString(HomeQuickSettingsDialog.KEY_SUBWAY_TRANSFER_DESTINATION)),
                )
            }
        }
        binding.mealDetail.setOnClickListener {
            val args = Bundle().apply {
                putString("tab", activeMealPeriod().tab)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_cafeteriaFragment,
                args,
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.movementLoading.setSkeletonLoading(isLoading)
            binding.mealLoading.setSkeletonLoading(isLoading)
            binding.movementLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.mealLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.data.observe(viewLifecycleOwner) {
            binding.homeSwipeRefreshLayout.isRefreshing = false
            renderNotices(it)
            render(it)
        }
        viewModel.showBus50Transfer.observe(viewLifecycleOwner) { render(viewModel.data.value) }
        viewModel.showSubwayTransfer.observe(viewLifecycleOwner) { render(viewModel.data.value) }
        viewModel.subwayTransferDestination.observe(viewLifecycleOwner) { render(viewModel.data.value) }
        viewModel.bus50TerminalLogTimes.observe(viewLifecycleOwner) { render(viewModel.data.value) }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let {
                binding.homeSwipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), getString(R.string.shuttle_no_realtime_data), Toast.LENGTH_SHORT).show()
            }
        }
        moveToNearestDeparture()
        refreshHome()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.removeCallbacks(autoRefreshRunnable)
        refreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL_MILLIS)
        if (::noticeAutoScrollRunnable.isInitialized) {
            noticeScrollHandler.postDelayed(noticeAutoScrollRunnable, NOTICE_AUTO_SCROLL_INTERVAL_MILLIS)
        }
    }

    override fun onPause() {
        refreshHandler.removeCallbacks(autoRefreshRunnable)
        if (::noticeAutoScrollRunnable.isInitialized) {
            noticeScrollHandler.removeCallbacks(noticeAutoScrollRunnable)
        }
        super.onPause()
    }

    override fun onDestroyView() {
        locationCancellationTokenSource?.cancel()
        locationCancellationTokenSource = null
        binding.noticeViewPager.adapter = null
        super.onDestroyView()
    }

    private fun setupNotices() {
        binding.noticeViewPager.adapter = HomeNoticeAdapter(emptyList())
        binding.noticeViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    noticeManuallyScrolled = true
                    if (::noticeAutoScrollRunnable.isInitialized) {
                        noticeScrollHandler.removeCallbacks(noticeAutoScrollRunnable)
                    }
                }
                if (state == ViewPager2.SCROLL_STATE_IDLE && noticeManuallyScrolled) {
                    noticePosition = binding.noticeViewPager.currentItem
                }
            }
        })
    }

    private fun renderNotices(data: HomePageQuery.Data?) {
        val notices = data?.notices?.flatMap { it.notices }.orEmpty()
        if (notices.isEmpty()) {
            binding.noticeLayout.visibility = View.GONE
            if (::noticeAutoScrollRunnable.isInitialized) {
                noticeScrollHandler.removeCallbacks(noticeAutoScrollRunnable)
            }
            return
        }

        binding.noticeLayout.visibility = View.VISIBLE
        (binding.noticeViewPager.adapter as HomeNoticeAdapter).updateList(notices)
        noticeAutoScrollRunnable = Runnable {
            val adapter = binding.noticeViewPager.adapter
            if (adapter != null && adapter.itemCount > 0 && !noticeManuallyScrolled) {
                noticePosition = (noticePosition + 1) % adapter.itemCount
                binding.noticeViewPager.setCurrentItem(noticePosition, true)
                noticeScrollHandler.postDelayed(noticeAutoScrollRunnable, NOTICE_AUTO_SCROLL_INTERVAL_MILLIS)
            }
        }
        if (!noticeManuallyScrolled) {
            noticeScrollHandler.removeCallbacks(noticeAutoScrollRunnable)
            noticeScrollHandler.postDelayed(noticeAutoScrollRunnable, NOTICE_AUTO_SCROLL_INTERVAL_MILLIS)
        }
    }

    private fun setupDestinationButtons() {
        binding.destinationGroup.clearOnButtonCheckedListeners()
        binding.destinationGroup.removeAllViews()
        val buttonContext = ContextThemeWrapper(requireContext(), R.style.Widget_App_SegmentedButton)
        val textColor = ContextCompat.getColorStateList(requireContext(), R.color.home_destination_button_text)
        val backgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.home_destination_button_background)
        val strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.home_destination_button_stroke)
        selectedDeparture.destinations.forEach { destination ->
            val button = MaterialButton(buttonContext, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                id = View.generateViewId()
                text = getString(destination.titleRes)
                minHeight = resources.getDimensionPixelSize(R.dimen.home_destination_button_min_height)
                minWidth = 0
                minimumWidth = 0
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                insetTop = 0
                insetBottom = 0
                isCheckable = true
                isAllCaps = false
                setTextColor(textColor)
                backgroundTintList = backgroundColor
                this.strokeColor = strokeColor
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                tag = destination
            }
            binding.destinationGroup.addView(button)
            if (destination == selectedDestination) binding.destinationGroup.check(button.id)
        }
        binding.destinationGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val destination = group.findViewById<View>(checkedId)?.tag as? HomeDestination ?: return@addOnButtonCheckedListener
            selectedDestination = destination
            render(viewModel.data.value)
        }
    }

    private fun applyDebugRouteOverride() {
        if (!BuildConfig.DEBUG) return
        val intent = activity?.intent ?: return
        val departure = HomeDeparture.fromDebugValue(intent.getStringExtra(DEBUG_DEPARTURE_EXTRA)) ?: return
        val destination = HomeDestination.fromDebugValue(intent.getStringExtra(DEBUG_DESTINATION_EXTRA))
            ?.takeIf { it in departure.destinations }
            ?: departure.destinations.first()
        selectedDeparture = departure
        selectedDestination = destination
        lockDepartureSelection = true
        debugSubwayTransferDestination = intent.getStringExtra(DEBUG_SUBWAY_DESTINATION_EXTRA)?.let(HomeSubwayTransferDestination::from)
    }

    private fun moveToNearestDeparture() {
        if (lockDepartureSelection || !hasLocationPermission()) return
        val client = LocationServices.getFusedLocationProviderClient(requireActivity())
        requestCurrentLocation(client)
    }

    @SuppressLint("MissingPermission")
    private fun selectLastKnownLocation(client: FusedLocationProviderClient) {
        client.lastLocation
            .addOnSuccessListener { location ->
                if (!isAdded || view == null) return@addOnSuccessListener
                if (location != null && isFresh(location)) selectNearestDeparture(location)
            }
            .addOnFailureListener {
                if (!isAdded || view == null) return@addOnFailureListener
                Log.e("HomeFragment", "Failed to get last known location", it)
            }
    }

    private fun hasLocationPermission(): Boolean {
        val context = context ?: return false
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isFresh(location: Location): Boolean {
        val ageMillis = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000
        return ageMillis in 0..LOCATION_MAX_AGE_MILLIS
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation(client: FusedLocationProviderClient) {
        if (!hasLocationPermission()) return
        locationCancellationTokenSource?.cancel()
        val tokenSource = CancellationTokenSource()
        locationCancellationTokenSource = tokenSource
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                if (locationCancellationTokenSource === tokenSource) {
                    locationCancellationTokenSource = null
                }
                if (!isAdded || view == null) return@addOnSuccessListener
                if (location != null) {
                    selectNearestDeparture(location)
                } else {
                    selectLastKnownLocation(client)
                }
            }
            .addOnFailureListener {
                if (locationCancellationTokenSource === tokenSource) {
                    locationCancellationTokenSource = null
                }
                if (!isAdded || view == null) return@addOnFailureListener
                Log.e("HomeFragment", "Failed to get user location", it)
                selectLastKnownLocation(client)
            }
    }

    private fun selectNearestDeparture(location: Location) {
        if (!isAdded || view == null) return
        if (lockDepartureSelection) return
        val nearestDeparture = HomeDeparture.entries.minByOrNull { it.distanceTo(location) } ?: return
        if (nearestDeparture == selectedDeparture) return

        selectedDeparture = nearestDeparture
        if (selectedDestination !in selectedDeparture.destinations) {
            selectedDestination = selectedDeparture.destinations.first()
        }
        setupDestinationButtons()
        render(viewModel.data.value)
    }

    private fun openQuickSettings() {
        if (childFragmentManager.findFragmentByTag(HOME_QUICK_SETTINGS_TAG) != null) return
        HomeQuickSettingsDialog.newInstance(
            showBus50Transfer = viewModel.showBus50Transfer.value ?: true,
            showSubwayTransfer = viewModel.showSubwayTransfer.value ?: true,
            subwayTransferDestination = selectedSubwayTransferDestination(),
        ).show(childFragmentManager, HOME_QUICK_SETTINGS_TAG)
    }

    private fun refreshHome() {
        binding.dateText.text = DateFormat.getDateInstance(DateFormat.FULL).format(Date())
        moveToNearestDeparture()
        viewModel.fetchData()
    }

    private fun render(data: HomePageQuery.Data?) {
        binding.routeText.text = getString(
            R.string.home_route_format,
            getString(selectedDeparture.titleRes),
            getString(selectedDestination.titleRes),
        )
        val mealPeriod = activeMealPeriod()
        binding.mealTitle.text = mealPeriod.title(requireContext())
        binding.mealIcon.setImageResource(mealPeriod.iconRes)
        renderMovement(data)
        renderMeals(data)
    }

    private fun renderMovement(data: HomePageQuery.Data?) {
        binding.movementContainer.removeAllViews()
        if (data == null) return

        val route = selectedDeparture.routeTo(selectedDestination)
        val shuttleCandidates = shuttleEntries(data).take(SHUTTLE_TRANSFER_LOOKAHEAD_COUNT)
        val displayCandidates = shuttleCandidates.take(SHUTTLE_DISPLAY_COUNT)
        val transferGroups = shuttleCandidates.map { transferConnections(data, route, it) }
        val shuttleRows = displayCandidates.mapIndexed { index, entry ->
            val routeDisplay = routeDisplay(route, entry)
            val subtitle = routeSubtitle(entry).let {
                if (entry.seq == shuttleCandidates.lastOrNull()?.seq) {
                    getString(R.string.home_shuttle_last_run_subtitle, it)
                } else {
                    it
                }
            }
            HomeShuttleMovement(
                HomeRow(
                    badge = routeDisplay.badge,
                    title = getString(R.string.home_departure_format, compactTime(entry.time)),
                    subtitle = subtitle,
                    trailing = minutesUntil(entry.time)?.let { getString(R.string.home_minutes, it) } ?: getString(R.string.home_check),
                    tint = routeDisplay.tint,
                ),
                displayableConnections(index, route, transferGroups, shuttleCandidates),
            )
        }
        val busRows = busAlternatives(data).take(2)
        val nextShuttleMinutes = shuttleCandidates.firstOrNull()?.time?.let { minutesUntil(it) }
        val shouldEmphasizeSupport = nextShuttleMinutes?.let { it > SUPPORT_EMPHASIS_THRESHOLD_MINUTES } ?: true

        if (shuttleRows.isEmpty()) {
            addEmptyRow(binding.movementContainer, R.string.home_no_data_title, R.string.home_no_data_message)
        } else {
            shuttleRows.forEach { addShuttleMovement(binding.movementContainer, it) }
        }
        if (busRows.isNotEmpty()) {
            addSupportHeader(binding.movementContainer, shouldEmphasizeSupport)
            busRows
                .take(if (shouldEmphasizeSupport) 4 else 2)
                .forEach { addHomeRow(binding.movementContainer, it) }
        }
    }

    private fun shuttleEntries(data: HomePageQuery.Data): List<HomePageQuery.Entry> {
        val route = selectedDeparture.routeTo(selectedDestination)
        val entries = data.shuttle.stops
            .firstOrNull { it.name == route.stop }
            ?.timetable
            ?.destination
            ?.firstOrNull { it.destination == route.destination }
            ?.entries
            .orEmpty()
        return route.filter?.let { entries.filter(it) } ?: entries
    }

    private fun busAlternatives(data: HomePageQuery.Data): List<HomeRow> {
        fun item(routeSeq: Int, stopSeq: Int): HomePageQuery.Bus? {
            return data.bus.firstOrNull { it.route.seq == routeSeq && it.stop.seq == stopSeq }
        }

        fun option(item: HomePageQuery.Bus?, route: String, stopName: String, direction: String, tint: Int): HomeRow? {
            if (item == null) return null
            val minutes = item.arrival.firstOrNull()?.minutes ?: return null
            return HomeRow(
                badge = route,
                title = stopName,
                subtitle = getString(R.string.home_alt_bus_direction, direction),
                trailing = getString(R.string.home_minutes, minutes),
                tint = tint,
            )
        }

        fun best(vararg options: HomeRow?): HomeRow? {
            return options.filterNotNull().minByOrNull { row -> row.trailing.filter(Char::isDigit).toIntOrNull() ?: Int.MAX_VALUE }
        }

        val green = ContextCompat.getColor(requireContext(), R.color.green_bus)
        val blue = ContextCompat.getColor(requireContext(), R.color.blue_bus)
        val terminal = getString(R.string.home_alt_direction_intercity_terminal)
        val terminalStop = getString(R.string.home_alt_intercity_terminal)
        val jungang = getString(R.string.home_destination_jungang)
        val dormitory = getString(R.string.home_destination_dormitory)
        val shuttlecock = getString(R.string.shuttle_tab_shuttlecock_out)
        val sangnoksu = getString(R.string.home_alt_direction_sangnoksu)
        val shuttlecockDormitory = getString(R.string.home_alt_direction_shuttlecock_dormitory)
        val route80A = best(
            option(item(216000081, 216000028), "80A", getString(R.string.home_alt_gyeonggi_technopark), terminal, blue),
            option(item(216000101, 216000028), "N80A", getString(R.string.home_alt_gyeonggi_technopark), terminal, blue),
        )
        val route80AToJungang = best(
            option(item(216000081, 216000028), "80A", getString(R.string.home_alt_gyeonggi_technopark), jungang, blue),
            option(item(216000101, 216000028), "N80A", getString(R.string.home_alt_gyeonggi_technopark), jungang, blue),
        )
        val terminal80B = best(
            option(item(216000082, 216000077), "80B", terminalStop, dormitory, blue),
            option(item(216000102, 216000077), "N80B", terminalStop, dormitory, blue),
        )
        val jungang80B = best(
            option(item(216000082, 217000140), "80B", jungang, dormitory, blue),
            option(item(216000102, 217000140), "N80B", jungang, dormitory, blue),
        )

        return when (selectedDeparture to selectedDestination) {
            HomeDeparture.DORMITORY to HomeDestination.STATION -> listOf(
                option(item(216000068, 216000383), "10-1", getString(R.string.home_alt_dormitory_nearby), sangnoksu, green),
            )
            HomeDeparture.DORMITORY to HomeDestination.TERMINAL -> listOf(route80A)
            HomeDeparture.DORMITORY to HomeDestination.JUNGANG -> listOf(route80AToJungang)
            HomeDeparture.SHUTTLECOCK to HomeDestination.TERMINAL -> listOf(
                option(item(216000016, 216000152), "62", getString(R.string.home_alt_seongan_entrance), terminal, green),
            )
            HomeDeparture.SHUTTLECOCK to HomeDestination.JUNGANG -> listOf(
                option(item(216000016, 216000152), "62", getString(R.string.home_alt_seongan_entrance), jungang, green),
            )
            HomeDeparture.STATION to HomeDestination.DORMITORY -> listOf(
                option(item(216000068, 216000138), "10-1", getString(R.string.home_alt_sangnoksu), shuttlecockDormitory, green),
            )
            HomeDeparture.TERMINAL to HomeDestination.DORMITORY -> listOf(
                terminal80B,
                option(item(216000016, 216000074), "62", terminalStop, shuttlecock, green),
            )
            HomeDeparture.JUNGANG to HomeDestination.DORMITORY -> listOf(
                jungang80B,
                option(item(216000016, 217000264), "62", jungang, shuttlecock, green),
            )
            else -> emptyList()
        }
            .filterNotNull()
    }

    private fun transferConnections(
        data: HomePageQuery.Data,
        route: HomeShuttleRoute,
        entry: HomePageQuery.Entry,
    ): List<HomeConnection> {
        bus50TransferConnection(data, route, entry)?.let { return listOf(it) }
        return subwayTransferConnections(data, route, entry)
    }

    private fun displayableConnections(
        index: Int,
        route: HomeShuttleRoute,
        connectionGroups: List<List<HomeConnection>>,
        candidates: List<HomePageQuery.Entry>,
    ): List<HomeConnection> {
        val connections = connectionGroups.getOrNull(index).orEmpty()
        val firstConnection = connections.firstOrNull() ?: return emptyList()
        val laterCandidates = candidates.drop(index + 1)
        val laterCandidateCanCatch = laterCandidates.any { candidate ->
            val transferArrival = candidateArrivalDate(candidate, route.destination) ?: return@any false
            Duration.between(transferArrival, firstConnection.arrivalDate).toMinutes() >= firstConnection.minimumTransferMinutes
        }
        return if (laterCandidateCanCatch) emptyList() else connections
    }

    private fun bus50TransferConnection(
        data: HomePageQuery.Data,
        route: HomeShuttleRoute,
        entry: HomePageQuery.Entry,
    ): HomeConnection? {
        if (viewModel.showBus50Transfer.value != true) return null
        if (route.destination != "TERMINAL" || route.stop !in setOf("dormitory_o", "shuttlecock_o")) return null
        val terminalArrival = candidateArrivalDate(entry, "TERMINAL") ?: return null
        val busArrival = data.transferBus
            .filter { it.stop.seq == 216000759 }
            .flatMap { it.arrival }
            .mapNotNull { it.minutes?.let(::timeAfterMinutes) }
            .filter { !it.isBefore(terminalArrival) }
            .minOrNull()
            ?: viewModel.bus50TerminalLogTimes.value
                .orEmpty()
                .map(::dateTimeFor)
                .filter { !it.isBefore(terminalArrival) }
                .minOrNull()
            ?: return null
        val bufferMinutes = Duration.between(terminalArrival, busArrival).toMinutes().coerceAtLeast(0).toInt()
        val tint = ContextCompat.getColor(
            requireContext(),
            if (bufferMinutes >= 3) R.color.green_bus else R.color.hanyang_orange,
        )
        return HomeConnection(
            row = HomeRow(
                badge = getString(R.string.home_transfer_bus50_badge),
                title = getString(R.string.home_transfer_bus50_title, compactTime(busArrival.toLocalTime())),
                subtitle = getString(R.string.home_transfer_bus50_subtitle),
                trailing = getString(R.string.home_transfer_buffer, bufferMinutes),
                tint = tint,
            ),
            arrivalDate = busArrival,
            minimumTransferMinutes = 0,
        )
    }

    private fun subwayTransferConnections(
        data: HomePageQuery.Data,
        route: HomeShuttleRoute,
        entry: HomePageQuery.Entry,
    ): List<HomeConnection> {
        if (!showSubwayTransferEnabled()) return emptyList()
        if (route.destination != "STATION" || route.stop !in setOf("dormitory_o", "shuttlecock_o")) return emptyList()
        val stationArrival = candidateArrivalDate(entry, "STATION") ?: return emptyList()
        return bestSubwayConnections(data, stationArrival)
    }

    private fun bestSubwayConnections(
        data: HomePageQuery.Data,
        stationArrival: ZonedDateTime,
    ): List<HomeConnection> {
        return when (selectedSubwayTransferDestination()) {
            HomeSubwayTransferDestination.SEOUL,
            HomeSubwayTransferDestination.SUWON_YONGIN,
            HomeSubwayTransferDestination.OIDO -> {
                subwayArrivalOptions(data)
                    .filter { canTransfer(it.arrivalDate, stationArrival) }
                    .minByOrNull { it.arrivalDate }
                    ?.let { listOf(subwayConnection(it, stationArrival)) }
                    .orEmpty()
            }
            HomeSubwayTransferDestination.INCHEON -> {
                val direct = subwayArrivalOptions(data, HomeSubwayRouteTarget.INCHEON_DIRECT)
                    .filter { canTransfer(it.arrivalDate, stationArrival) }
                    .map { listOf(subwayConnection(it, stationArrival)) }
                val transfer = oidoTransferSubwayConnections(data, stationArrival)
                (direct + transfer)
                    .minByOrNull { it.lastOrNull()?.arrivalDate ?: ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1) }
                    .orEmpty()
            }
            HomeSubwayTransferDestination.SOSA -> {
                chojiTransferSubwayConnections(data, stationArrival)
                    .minByOrNull { it.lastOrNull()?.arrivalDate ?: ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(1) }
                    .orEmpty()
            }
        }
    }

    private fun oidoTransferSubwayConnections(
        data: HomePageQuery.Data,
        stationArrival: ZonedDateTime,
    ): List<List<HomeConnection>> {
        val firstLegs = subwayArrivalOptions(data, HomeSubwayRouteTarget.OIDO)
            .filter { canTransfer(it.arrivalDate, stationArrival) }
        val secondLegs = subwayArrivalOptions(data, HomeSubwayRouteTarget.INCHEON_FROM_OIDO)
        return firstLegs.mapNotNull { firstLeg ->
            val secondLeg = secondLegs
                .filter { canTransfer(it.arrivalDate, firstLeg.arrivalDate) }
                .minByOrNull { it.arrivalDate }
                ?: return@mapNotNull null
            listOf(
                subwayConnection(firstLeg, stationArrival),
                subwayConnection(secondLeg, firstLeg.arrivalDate, R.string.home_transfer_subway_oido_subtitle),
            )
        }
    }

    private fun chojiTransferSubwayConnections(
        data: HomePageQuery.Data,
        stationArrival: ZonedDateTime,
    ): List<List<HomeConnection>> {
        val firstLegs = subwayArrivalOptions(data, HomeSubwayRouteTarget.CHOJI)
            .filter { canTransfer(it.arrivalDate, stationArrival) }
        val secondLegs = subwayArrivalOptions(data, HomeSubwayRouteTarget.SOSA_FROM_CHOJI)
        return firstLegs.mapNotNull { firstLeg ->
            val secondLeg = secondLegs
                .filter { canTransfer(it.arrivalDate, firstLeg.arrivalDate, CHOJI_MINIMUM_TRANSFER_MINUTES) }
                .minByOrNull { it.arrivalDate }
                ?: return@mapNotNull null
            listOf(
                subwayConnection(firstLeg, stationArrival),
                subwayConnection(
                    secondLeg,
                    firstLeg.arrivalDate,
                    R.string.home_transfer_subway_choji_subtitle,
                    CHOJI_MINIMUM_TRANSFER_MINUTES,
                ),
            )
        }
    }

    private fun subwayArrivalOptions(data: HomePageQuery.Data): List<HomeSubwayArrival> {
        return when (selectedSubwayTransferDestination()) {
            HomeSubwayTransferDestination.SEOUL -> subwayArrivalOptions(data, HomeSubwayRouteTarget.SEOUL)
            HomeSubwayTransferDestination.SUWON_YONGIN -> subwayArrivalOptions(data, HomeSubwayRouteTarget.SUWON_YONGIN)
            HomeSubwayTransferDestination.INCHEON -> subwayArrivalOptions(data, HomeSubwayRouteTarget.INCHEON_DIRECT)
            HomeSubwayTransferDestination.OIDO -> subwayArrivalOptions(data, HomeSubwayRouteTarget.OIDO)
            HomeSubwayTransferDestination.SOSA -> subwayArrivalOptions(data, HomeSubwayRouteTarget.SOSA_FROM_CHOJI)
        }
    }

    private fun subwayArrivalOptions(
        data: HomePageQuery.Data,
        target: HomeSubwayRouteTarget,
    ): List<HomeSubwayArrival> {
        if (target == HomeSubwayRouteTarget.OIDO) {
            return subwayArrivalOptionsFrom(
                data = data,
                stationId = "K449",
                direction = "down",
                badge = getString(R.string.subway_line4),
                tint = ContextCompat.getColor(requireContext(), R.color.subway_line4),
            ) { it.terminal.stationID == "K456" } + subwayArrivalOptionsFrom(
                data = data,
                stationId = "K251",
                direction = "down",
                badge = getString(R.string.home_transfer_subway_suin_bundang_badge),
                tint = ContextCompat.getColor(requireContext(), R.color.home_subway_yellow),
            ) { it.terminal.stationID >= "K258" && it.terminal.stationID.startsWith("K2") }
        }
        if (target == HomeSubwayRouteTarget.CHOJI) {
            return subwayArrivalOptionsFrom(
                data = data,
                stationId = "K449",
                direction = "down",
                badge = getString(R.string.subway_line4),
                tint = ContextCompat.getColor(requireContext(), R.color.subway_line4),
            ) { it.terminal.stationID >= "K452" && it.terminal.stationID.startsWith("K4") } + subwayArrivalOptionsFrom(
                data = data,
                stationId = "K251",
                direction = "down",
                badge = getString(R.string.home_transfer_subway_suin_bundang_badge),
                tint = ContextCompat.getColor(requireContext(), R.color.home_subway_yellow),
            ) { it.terminal.stationID >= "K254" && it.terminal.stationID.startsWith("K2") }
        }
        if (target == HomeSubwayRouteTarget.SOSA_FROM_CHOJI) {
            return subwayTimetableOptionsFrom(
                data = data,
                stationId = "S26",
                direction = "up",
                badge = getString(R.string.home_transfer_subway_seohae_badge),
                tint = ContextCompat.getColor(requireContext(), R.color.subway_seohae),
            ) { it.terminal.stationID <= "S16" && it.terminal.stationID.startsWith("S") }
        }
        val subway = when (target) {
            HomeSubwayRouteTarget.SEOUL -> data.subway.firstOrNull { it.stationID == "K449" }
            HomeSubwayRouteTarget.SUWON_YONGIN,
            HomeSubwayRouteTarget.INCHEON_DIRECT -> data.subway.firstOrNull { it.stationID == "K251" }
            HomeSubwayRouteTarget.INCHEON_FROM_OIDO -> data.subway.firstOrNull { it.stationID == "K258" }
            HomeSubwayRouteTarget.SOSA_FROM_CHOJI -> data.subway.firstOrNull { it.stationID == "S26" }
            HomeSubwayRouteTarget.OIDO,
            HomeSubwayRouteTarget.CHOJI -> null
        }
        val direction = when (target) {
            HomeSubwayRouteTarget.SEOUL,
            HomeSubwayRouteTarget.SUWON_YONGIN,
            HomeSubwayRouteTarget.SOSA_FROM_CHOJI -> "up"
            HomeSubwayRouteTarget.INCHEON_DIRECT,
            HomeSubwayRouteTarget.INCHEON_FROM_OIDO,
            HomeSubwayRouteTarget.OIDO,
            HomeSubwayRouteTarget.CHOJI -> "down"
        }
        val badge = when (target) {
            HomeSubwayRouteTarget.SEOUL -> getString(R.string.subway_line4)
            HomeSubwayRouteTarget.SOSA_FROM_CHOJI -> getString(R.string.home_transfer_subway_seohae_badge)
            else -> getString(R.string.home_transfer_subway_suin_bundang_badge)
        }
        val tint = ContextCompat.getColor(
            requireContext(),
            when (target) {
                HomeSubwayRouteTarget.SEOUL -> R.color.subway_line4
                HomeSubwayRouteTarget.SOSA_FROM_CHOJI -> R.color.subway_seohae
                else -> R.color.home_subway_yellow
            },
        )
        return subwayArrivalOptionsFrom(data, subway?.stationID, direction, badge, tint) { subwayEntryEligible(target, it) }
    }

    private fun subwayArrivalOptionsFrom(
        data: HomePageQuery.Data,
        stationId: String?,
        direction: String,
        badge: String,
        tint: Int,
        isEligible: (HomePageQuery.Entry1) -> Boolean,
    ): List<HomeSubwayArrival> {
        val subway = data.subway.firstOrNull { it.stationID == stationId }
        return subway?.arrival
            ?.firstOrNull { it.direction == direction }
            ?.entries
            ?.filter(isEligible)
            ?.map {
                HomeSubwayArrival(
                    lineBadge = badge,
                    terminalStationID = it.terminal.stationID,
                    terminalName = it.terminal.name,
                    arrivalDate = timeAfterMinutes(it.minutes),
                    tint = tint,
                )
            }
            .orEmpty()
    }

    private fun subwayTimetableOptionsFrom(
        data: HomePageQuery.Data,
        stationId: String,
        direction: String,
        badge: String,
        tint: Int,
        isEligible: (HomePageQuery.Timetable1) -> Boolean,
    ): List<HomeSubwayArrival> {
        val subway = data.subway.firstOrNull { it.stationID == stationId }
        return subway?.timetable
            ?.filter { it.direction == direction }
            ?.filter(isEligible)
            ?.map {
                HomeSubwayArrival(
                    lineBadge = badge,
                    terminalStationID = it.terminal.stationID,
                    terminalName = it.terminal.name,
                    arrivalDate = upcomingDateTimeFor(it.time),
                    tint = tint,
                )
            }
            .orEmpty()
    }

    private fun subwayEntryEligible(target: HomeSubwayRouteTarget, entry: HomePageQuery.Entry1): Boolean {
        return when (target) {
            HomeSubwayRouteTarget.INCHEON_DIRECT,
            HomeSubwayRouteTarget.INCHEON_FROM_OIDO -> entry.terminal.stationID > "K258" && entry.terminal.stationID.startsWith("K2")
            HomeSubwayRouteTarget.OIDO -> entry.terminal.stationID >= "K258" && entry.terminal.stationID.startsWith("K2")
            HomeSubwayRouteTarget.SOSA_FROM_CHOJI -> entry.terminal.stationID <= "S16" && entry.terminal.stationID.startsWith("S")
            else -> true
        }
    }

    private fun subwayConnection(
        arrival: HomeSubwayArrival,
        transferStartDate: ZonedDateTime,
        subtitleRes: Int = R.string.home_transfer_subway_subtitle,
        minimumTransferMinutes: Int = SUBWAY_MINIMUM_TRANSFER_MINUTES,
    ): HomeConnection {
        val bufferMinutes = Duration.between(transferStartDate, arrival.arrivalDate).toMinutes().coerceAtLeast(0).toInt()
        return HomeConnection(
            row = HomeRow(
                badge = arrival.lineBadge,
                title = getString(
                    R.string.home_transfer_subway_title,
                    localizedSubwayStationName(requireContext(), arrival.terminalStationID, arrival.terminalName),
                ),
                subtitle = getString(subtitleRes),
                trailing = getString(R.string.home_transfer_buffer, bufferMinutes),
                tint = arrival.tint,
            ),
            arrivalDate = arrival.arrivalDate,
            minimumTransferMinutes = minimumTransferMinutes,
        )
    }

    private fun canTransfer(
        arrivalDate: ZonedDateTime,
        previousArrivalDate: ZonedDateTime,
        minimumTransferMinutes: Int = SUBWAY_MINIMUM_TRANSFER_MINUTES,
    ): Boolean {
        return Duration.between(previousArrivalDate, arrivalDate).toMinutes() >= minimumTransferMinutes
    }

    private fun showSubwayTransferEnabled(): Boolean {
        return debugSubwayTransferDestination != null || viewModel.showSubwayTransfer.value == true
    }

    private fun selectedSubwayTransferDestination(): HomeSubwayTransferDestination {
        return debugSubwayTransferDestination ?: viewModel.subwayTransferDestination.value ?: HomeSubwayTransferDestination.SEOUL
    }

    private fun candidateArrivalDate(entry: HomePageQuery.Entry, destination: String): ZonedDateTime? {
        val stopId = shuttleStopId(destination) ?: return null
        return entry.stops.firstOrNull { it.stop == stopId }?.time?.let(::dateTimeFor)
    }

    private fun dateTimeFor(time: LocalTime): ZonedDateTime {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
    }

    private fun upcomingDateTimeFor(time: LocalTime): ZonedDateTime {
        val dateTime = dateTimeFor(time)
        return if (dateTime.isBefore(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))) {
            dateTime.plusDays(1)
        } else {
            dateTime
        }
    }

    private fun timeAfterMinutes(minutes: Int): ZonedDateTime {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(minutes.toLong())
    }

    private fun renderMeals(data: HomePageQuery.Data?) {
        binding.mealContainer.removeAllViews()
        if (data == null) return
        val period = activeMealPeriod()
        val sections = data.cafeteria
            .sortedBy { it.seq }
            .mapNotNull { cafeteria ->
                val items = cafeteria.menus
                    .filter { it.type.contains(period.marker) }
                    .mapNotNull { menu ->
                        val text = representativeMenu(menu.food)
                        if (text.isBlank()) {
                            null
                        } else {
                            HomeMealItem(text, menu.price)
                        }
                    }
                if (items.isEmpty()) {
                    null
                } else {
                    HomeMealSection(
                        cafeteria = cafeteriaName(cafeteria.seq),
                        runningTime = runningTime(cafeteria, period).orEmpty(),
                        items = items,
                    )
                }
            }
            .take(5)

        if (sections.isEmpty()) {
            addEmptyRow(
                binding.mealContainer,
                getString(R.string.home_meal_empty_title, period.title(requireContext())),
                getString(R.string.home_meal_empty_message),
            )
        } else {
            sections.forEach { addMealSection(binding.mealContainer, it) }
        }
    }

    private fun addShuttleMovement(container: LinearLayout, movement: HomeShuttleMovement) {
        if (movement.connections.isEmpty()) {
            addHomeRow(container, movement.row)
            return
        }
        val pair = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
        }
        val shuttleView = createHomeRowView(movement.row)
        pair.addView(shuttleView)
        movement.connections.forEach { connection ->
            pair.addView(createLinkBadge(connection.row.tint), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(22)).apply {
                topMargin = -dp(7)
                bottomMargin = -dp(7)
            })
            pair.addView(createHomeTransferRowView(connection.row), LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ))
        }
        container.addView(pair, rowLayoutParams(container.childCount))
    }

    private fun addHomeRow(container: LinearLayout, row: HomeRow) {
        container.addView(createHomeRowView(row), rowLayoutParams(container.childCount))
    }

    private fun createHomeRowView(row: HomeRow): View {
        val rowBinding = ItemHomeRowBinding.inflate(layoutInflater)
        rowBinding.badge.applyHomeTypeface(Typeface.BOLD)
        rowBinding.title.applyHomeTypeface(Typeface.BOLD)
        rowBinding.subtitle.applyHomeTypeface(Typeface.NORMAL)
        rowBinding.trailing.applyHomeTypeface(Typeface.BOLD)
        rowBinding.badge.text = row.badge
        rowBinding.badge.backgroundTintList = ColorStateList.valueOf(row.tint)
        rowBinding.root.backgroundTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(row.tint, ROW_BACKGROUND_ALPHA))
        rowBinding.title.text = row.title
        rowBinding.subtitle.text = row.subtitle
        rowBinding.trailing.text = row.trailing
        rowBinding.trailing.setTextColor(row.tint)
        return rowBinding.root
    }

    private fun createHomeTransferRowView(row: HomeRow): View {
        val rowBinding = ItemHomeTransferRowBinding.inflate(layoutInflater)
        rowBinding.badge.applyHomeTypeface(Typeface.BOLD)
        rowBinding.title.applyHomeTypeface(Typeface.BOLD)
        rowBinding.subtitle.applyHomeTypeface(Typeface.NORMAL)
        rowBinding.trailing.applyHomeTypeface(Typeface.BOLD)
        rowBinding.badge.text = row.badge
        rowBinding.badge.backgroundTintList = ColorStateList.valueOf(row.tint)
        rowBinding.root.background = android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(ColorUtils.setAlphaComponent(row.tint, TRANSFER_ROW_BACKGROUND_ALPHA))
            setStroke(dp(1), ColorUtils.setAlphaComponent(row.tint, TRANSFER_ROW_STROKE_ALPHA))
        }
        rowBinding.title.text = row.title
        rowBinding.subtitle.text = row.subtitle
        rowBinding.trailing.text = row.trailing
        rowBinding.trailing.setTextColor(row.tint)
        return rowBinding.root
    }

    private fun createLinkBadge(tint: Int): View {
        return LinearLayout(requireContext()).apply {
            clipChildren = false
            clipToPadding = false
            gravity = Gravity.CENTER
            addView(ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_home_link)
                imageTintList = ColorStateList.valueOf(ColorUtils.setAlphaComponent(tint, 184))
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(dp(5), dp(5), dp(5), dp(5))
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(requireContext(), R.color.background))
                    setStroke(dp(1), ColorUtils.setAlphaComponent(tint, 46))
                }
            }, LinearLayout.LayoutParams(dp(22), dp(22)))
        }
    }

    private fun addMealSection(container: LinearLayout, section: HomeMealSection) {
        val sectionView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_home_row)
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        sectionView.addView(TextView(requireContext()).apply {
            applyHomeTypeface(Typeface.BOLD)
            text = section.cafeteria
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_text))
            textSize = 16f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })
        if (section.runningTime.isNotBlank()) {
            sectionView.addView(TextView(requireContext()).apply {
                applyHomeTypeface(Typeface.NORMAL)
                text = section.runningTime
                setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
                textSize = 12f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(3)
            })
        }

        section.items.forEachIndexed { index, item ->
            if (index > 0) {
                sectionView.addView(View(requireContext()).apply {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.calendar_grid_line))
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
                    topMargin = dp(8)
                    bottomMargin = dp(8)
                })
            } else {
                sectionView.addView(View(requireContext()), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(10)))
            }
            sectionView.addView(mealMenuRow(item))
        }

        container.addView(sectionView, rowLayoutParams(container.childCount))
    }

    private fun mealMenuRow(item: HomeMealItem): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(requireContext()).apply {
                applyHomeTypeface(Typeface.NORMAL)
                text = item.menu
                setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_text))
                textSize = 14f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(TextView(requireContext()).apply {
                applyHomeTypeface(Typeface.BOLD)
                text = item.price
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_text))
                textSize = 14f
                gravity = Gravity.END
                maxLines = 1
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = dp(8)
            })
        }
    }

    private fun addEmptyRow(container: LinearLayout, titleRes: Int, messageRes: Int) {
        addEmptyRow(container, getString(titleRes), getString(messageRes))
    }

    private fun addEmptyRow(container: LinearLayout, title: String, message: String) {
        val view = layoutInflater.inflate(R.layout.item_home_row, container, false)
        view.findViewById<TextView>(R.id.badge).visibility = View.GONE
        view.findViewById<View>(R.id.text_container).layoutParams =
            (view.findViewById<View>(R.id.text_container).layoutParams as ConstraintLayout.LayoutParams).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                startToEnd = ConstraintLayout.LayoutParams.UNSET
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                endToStart = ConstraintLayout.LayoutParams.UNSET
                marginStart = 0
                marginEnd = 0
            }
        view.findViewById<TextView>(R.id.title).apply {
            applyHomeTypeface(Typeface.BOLD)
            text = title
        }
        view.findViewById<TextView>(R.id.subtitle).apply {
            applyHomeTypeface(Typeface.NORMAL)
            text = message
        }
        view.findViewById<TextView>(R.id.trailing).visibility = View.GONE
        container.addView(view, rowLayoutParams(container.childCount))
    }

    private fun addSupportHeader(container: LinearLayout, emphasized: Boolean) {
        val header = TextView(requireContext()).apply {
            applyHomeTypeface(Typeface.BOLD)
            text = getString(if (emphasized) R.string.home_support_emphasized else R.string.home_support_default)
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (emphasized) R.color.primary_text else R.color.secondary_text,
                ),
            )
            textSize = 13f
        }
        container.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = resources.getDimensionPixelSize(R.dimen.home_row_gap)
            bottomMargin = dp(2)
        })
    }

    private fun rowLayoutParams(index: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            if (index > 0) topMargin = resources.getDimensionPixelSize(R.dimen.home_row_gap)
        }
    }

    private fun TextView.applyHomeTypeface(style: Int) {
        typeface = Typeface.create(homeTypeface, style)
    }

    private fun routeSubtitle(entry: HomePageQuery.Entry): String {
        val route = selectedDeparture.routeTo(selectedDestination)
        val pathStopIds = shuttlePathStopIds(route.stop, route.destination, entry.stops.map { it.stop })
        val viaStops = pathStopIds.drop(1).dropLast(1).map { localizedViaStopName(it) }.filter { it.isNotBlank() }
        return if (viaStops.isEmpty()) {
            getString(R.string.home_shuttle_no_via)
        } else {
            getString(R.string.home_shuttle_via, viaStops.joinToString(" · "))
        }
    }

    private fun shuttlePathStopIds(stopName: String, destination: String, stopIds: List<String>): List<String> {
        val destinationStopId = shuttleStopId(destination)
        val startIndex = stopIds.indexOf(stopName)
        if (startIndex < 0) return stopIds
        val remainingStopIds = stopIds.drop(startIndex)
        if (destinationStopId != null) {
            val destinationIndex = remainingStopIds.indexOf(destinationStopId)
            if (destinationIndex >= 0) {
                return remainingStopIds.take(destinationIndex + 1)
            }
        }
        return remainingStopIds
    }

    private fun shuttleStopId(destination: String): String? = when (destination) {
        "STATION" -> "station"
        "TERMINAL" -> "terminal"
        "JUNGANG" -> "jungang_stn"
        "CAMPUS" -> "dormitory_i"
        else -> null
    }

    private fun localizedViaStopName(stop: String): String {
        return localizedStopName(if (stop == "shuttlecock_i") "shuttlecock_o" else stop)
    }

    private fun localizedStopName(stop: String): String = when (stop) {
        "dormitory_o" -> getString(R.string.shuttle_tab_dormitory_out)
        "shuttlecock_o" -> getString(R.string.shuttle_tab_shuttlecock_out)
        "station" -> getString(R.string.shuttle_tab_station)
        "terminal" -> getString(R.string.shuttle_tab_terminal)
        "jungang_stn" -> getString(R.string.shuttle_tab_jungang_station)
        "shuttlecock_i" -> getString(R.string.shuttle_tab_shuttlecock_in)
        "dormitory_i" -> getString(R.string.shuttle_bound_for_dormitory)
        else -> stop
    }

    private fun routeDisplay(route: HomeShuttleRoute, entry: HomePageQuery.Entry): HomeRouteDisplay {
        val routeTag = entry.route.tag
        val routeName = entry.route.name
        val blue = ContextCompat.getColor(requireContext(), R.color.blue_bus)
        val green = ContextCompat.getColor(requireContext(), R.color.green_bus)
        val red = ContextCompat.getColor(requireContext(), R.color.red_bus)
        val hanyangBlue = ContextCompat.getColor(requireContext(), R.color.hanyang_blue)

        return when (route.stop to route.destination) {
            "dormitory_o" to "STATION",
            "shuttlecock_o" to "STATION" -> when {
                routeTag == "DH" || routeTag == "DJ" -> HomeRouteDisplay(getString(R.string.shuttle_type_direct), red)
                routeTag == "C" -> HomeRouteDisplay(getString(R.string.shuttle_type_circular), blue)
                else -> HomeRouteDisplay(getString(R.string.home_badge_free), hanyangBlue)
            }
            "dormitory_o" to "TERMINAL",
            "shuttlecock_o" to "TERMINAL" -> when {
                routeTag == "DY" -> HomeRouteDisplay(getString(R.string.shuttle_type_direct), red)
                routeTag == "DJ" -> HomeRouteDisplay(getString(R.string.shuttle_type_jungang), green)
                routeTag == "C" -> HomeRouteDisplay(getString(R.string.shuttle_type_circular), blue)
                else -> HomeRouteDisplay(getString(R.string.home_badge_free), hanyangBlue)
            }
            "dormitory_o" to "JUNGANG",
            "shuttlecock_o" to "JUNGANG",
            "station" to "JUNGANG" -> HomeRouteDisplay(getString(R.string.shuttle_type_jungang), green)
            "station" to "CAMPUS" -> when {
                routeTag == "DH" -> HomeRouteDisplay(getString(R.string.shuttle_type_direct), red)
                routeTag == "DJ" -> HomeRouteDisplay(getString(R.string.shuttle_type_jungang), green)
                routeTag == "C" -> HomeRouteDisplay(getString(R.string.shuttle_type_circular), blue)
                else -> HomeRouteDisplay(getString(R.string.home_badge_free), hanyangBlue)
            }
            "station" to "TERMINAL" -> when {
                routeTag == "C" -> HomeRouteDisplay(getString(R.string.shuttle_type_circular), blue)
                else -> HomeRouteDisplay(getString(R.string.home_badge_free), hanyangBlue)
            }
            "terminal" to "CAMPUS",
            "jungang_stn" to "CAMPUS",
            "shuttlecock_i" to "CAMPUS" -> when {
                routeName.endsWith("S") -> HomeRouteDisplay(getString(R.string.shuttle_type_shuttlecock), red)
                routeName.endsWith("D") -> HomeRouteDisplay(getString(R.string.shuttle_type_dormitory), hanyangBlue)
                else -> HomeRouteDisplay(getString(R.string.home_badge_free), hanyangBlue)
            }
            else -> HomeRouteDisplay(getString(R.string.home_badge_free), hanyangBlue)
        }
    }

    private fun compactTime(time: LocalTime): String = "%02d:%02d".format(time.hour, time.minute)

    private fun minutesUntil(time: LocalTime): Int? {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val target = now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
        return ceil((target.toEpochSecond() - now.toEpochSecond()) / 60.0).toInt().coerceAtLeast(0)
    }

    private fun activeMealPeriod(): HomeMealPeriod {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return when {
            now.hour < 10 -> HomeMealPeriod("조식", R.string.home_meal_breakfast, "breakfast", R.drawable.ic_meal_breakfast)
            now.hour < 15 -> HomeMealPeriod("중식", R.string.home_meal_lunch, "lunch", R.drawable.ic_meal_lunch)
            now.hour < 20 -> HomeMealPeriod("석식", R.string.home_meal_dinner, "dinner", R.drawable.ic_meal_dinner)
            else -> HomeMealPeriod("조식", R.string.home_meal_tomorrow_breakfast, "breakfast", R.drawable.ic_meal_breakfast)
        }
    }

    private fun runningTime(cafeteria: HomePageQuery.Cafeterium, period: HomeMealPeriod): String? = when (period.marker) {
        "조식" -> cafeteria.runningTime.breakfast
        "중식" -> cafeteria.runningTime.lunch
        else -> cafeteria.runningTime.dinner
    }

    private fun cafeteriaName(seq: Int): String {
        val resId = when (seq) {
            1 -> R.string.cafeteria_1
            2 -> R.string.cafeteria_2
            4 -> R.string.cafeteria_4
            6 -> R.string.cafeteria_6
            7 -> R.string.cafeteria_7
            8 -> R.string.cafeteria_8
            11 -> R.string.cafeteria_11
            12 -> R.string.cafeteria_12
            13 -> R.string.cafeteria_13
            14 -> R.string.cafeteria_14
            15 -> R.string.cafeteria_15
            else -> R.string.cafeteria_1
        }
        return getString(resId)
    }

    private fun representativeMenu(food: String): String {
        return food
            .replace("\"", "")
            .replace(Regex("^\\s*\\[[^]]+]\\s*"), "")
            .replace(Regex("^\\s*<[^>]+>\\s*"), "")
            .replace(Regex("^\\s*[\\w가-힣]+\\)\\s*"), "")
            .split(Regex("\\s+"))
            .firstOrNull { it.isNotBlank() }
            ?: food
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val LOCATION_MAX_AGE_MILLIS = 60_000L
        private const val ROW_BACKGROUND_ALPHA = 24
        private const val TRANSFER_ROW_BACKGROUND_ALPHA = 20
        private const val TRANSFER_ROW_STROKE_ALPHA = 31
        private const val HOME_QUICK_SETTINGS_TAG = "HomeQuickSettingsDialog"
        private const val SUBWAY_MINIMUM_TRANSFER_MINUTES = 5
        private const val CHOJI_MINIMUM_TRANSFER_MINUTES = 8
        private const val SHUTTLE_DISPLAY_COUNT = 2
        private const val SHUTTLE_TRANSFER_LOOKAHEAD_COUNT = 3
        private const val AUTO_REFRESH_INTERVAL_MILLIS = 60_000L
        private const val NOTICE_AUTO_SCROLL_INTERVAL_MILLIS = 5_000L
        private const val SUPPORT_EMPHASIS_THRESHOLD_MINUTES = 20
        private const val DEBUG_DEPARTURE_EXTRA = "homeDebugDeparture"
        private const val DEBUG_DESTINATION_EXTRA = "homeDebugDestination"
        private const val DEBUG_SUBWAY_DESTINATION_EXTRA = "homeDebugSubwayDestination"
    }
}

private enum class HomeDeparture(
    val titleRes: Int,
    val latitude: Double,
    val longitude: Double,
    val destinations: List<HomeDestination>,
) {
    DORMITORY(R.string.shuttle_tab_dormitory_out, 37.29339607529377, 126.83630604103446, listOf(HomeDestination.STATION, HomeDestination.TERMINAL, HomeDestination.JUNGANG)),
    SHUTTLECOCK(R.string.shuttle_tab_shuttlecock_out, 37.29875417910844, 126.83784054072336, listOf(HomeDestination.STATION, HomeDestination.TERMINAL, HomeDestination.JUNGANG, HomeDestination.DORMITORY)),
    STATION(R.string.shuttle_tab_station, 37.309700971618255, 126.85207173389148, listOf(HomeDestination.DORMITORY, HomeDestination.TERMINAL, HomeDestination.JUNGANG)),
    TERMINAL(R.string.shuttle_tab_terminal, 37.319338173415936, 126.8455263115596, listOf(HomeDestination.DORMITORY)),
    JUNGANG(R.string.shuttle_tab_jungang_station, 37.31487247528457, 126.83963540399434, listOf(HomeDestination.DORMITORY));

    val debugValue: String
        get() = when (this) {
            DORMITORY -> "dormitory"
            SHUTTLECOCK -> "shuttlecock"
            STATION -> "station"
            TERMINAL -> "terminal"
            JUNGANG -> "jungang"
        }

    fun routeTo(destination: HomeDestination): HomeShuttleRoute = when (this to destination) {
        DORMITORY to HomeDestination.STATION -> HomeShuttleRoute("dormitory_o", "STATION")
        DORMITORY to HomeDestination.TERMINAL -> HomeShuttleRoute("dormitory_o", "TERMINAL")
        DORMITORY to HomeDestination.JUNGANG -> HomeShuttleRoute("dormitory_o", "JUNGANG")
        SHUTTLECOCK to HomeDestination.STATION -> HomeShuttleRoute("shuttlecock_o", "STATION")
        SHUTTLECOCK to HomeDestination.TERMINAL -> HomeShuttleRoute("shuttlecock_o", "TERMINAL")
        SHUTTLECOCK to HomeDestination.JUNGANG -> HomeShuttleRoute("shuttlecock_o", "JUNGANG")
        SHUTTLECOCK to HomeDestination.DORMITORY -> HomeShuttleRoute("shuttlecock_i", "CAMPUS") { it.route.name.endsWith("D") }
        STATION to HomeDestination.DORMITORY -> HomeShuttleRoute("station", "CAMPUS") { it.route.name.endsWith("D") }
        STATION to HomeDestination.TERMINAL -> HomeShuttleRoute("station", "TERMINAL")
        STATION to HomeDestination.JUNGANG -> HomeShuttleRoute("station", "JUNGANG")
        TERMINAL to HomeDestination.DORMITORY -> HomeShuttleRoute("terminal", "CAMPUS") { it.route.name.endsWith("D") }
        JUNGANG to HomeDestination.DORMITORY -> HomeShuttleRoute("jungang_stn", "CAMPUS") { it.route.name.endsWith("D") }
        else -> HomeShuttleRoute("dormitory_o", "STATION")
    }

    fun distanceTo(location: Location): Float {
        val departureLocation = Location("home_departure").apply {
            latitude = this@HomeDeparture.latitude
            longitude = this@HomeDeparture.longitude
        }
        return departureLocation.distanceTo(location)
    }

    companion object {
        fun fromDebugValue(value: String?): HomeDeparture? = entries.firstOrNull { it.debugValue == value }
    }
}

private enum class HomeDestination(val titleRes: Int) {
    STATION(R.string.home_destination_station),
    TERMINAL(R.string.home_destination_terminal),
    JUNGANG(R.string.home_destination_jungang),
    DORMITORY(R.string.home_destination_dormitory);

    val debugValue: String
        get() = when (this) {
            STATION -> "station"
            TERMINAL -> "terminal"
            JUNGANG -> "jungang"
            DORMITORY -> "dormitory"
        }

    companion object {
        fun fromDebugValue(value: String?): HomeDestination? = entries.firstOrNull { it.debugValue == value }
    }
}

private data class HomeShuttleRoute(
    val stop: String,
    val destination: String,
    val filter: ((HomePageQuery.Entry) -> Boolean)? = null,
)

private data class HomeMealPeriod(
    val marker: String,
    val titleRes: Int,
    val tab: String,
    val iconRes: Int,
) {
    fun title(context: android.content.Context): String = context.getString(titleRes)
}

private data class HomeMealSection(
    val cafeteria: String,
    val runningTime: String,
    val items: List<HomeMealItem>,
)

private data class HomeMealItem(
    val menu: String,
    val price: String,
)

private data class HomeRow(
    val badge: String,
    val title: String,
    val subtitle: String,
    val trailing: String,
    val tint: Int,
)

private data class HomeShuttleMovement(
    val row: HomeRow,
    val connections: List<HomeConnection>,
)

private data class HomeRouteDisplay(
    val badge: String,
    val tint: Int,
)

private data class HomeConnection(
    val row: HomeRow,
    val arrivalDate: ZonedDateTime,
    val minimumTransferMinutes: Int,
)

private data class HomeSubwayArrival(
    val lineBadge: String,
    val terminalStationID: String,
    val terminalName: String,
    val arrivalDate: ZonedDateTime,
    val tint: Int,
)

private enum class HomeSubwayRouteTarget {
    SEOUL,
    SUWON_YONGIN,
    INCHEON_DIRECT,
    INCHEON_FROM_OIDO,
    SOSA_FROM_CHOJI,
    CHOJI,
    OIDO,
}
