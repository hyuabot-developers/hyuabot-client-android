package app.kobuggi.hyuabot.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.analytics.WatchAnalyticsTracker
import app.kobuggi.hyuabot.presentation.NavigationUtils.Companion.NavigationStack
import app.kobuggi.hyuabot.presentation.theme.HYUabotTheme
import app.kobuggi.hyuabot.service.GraphQLModule
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private lateinit var watchAnalyticsTracker: WatchAnalyticsTracker
    private var appOpenEntryPoint = WatchAnalyticsTracker.EntryPoint.APP

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        val stopID = intent.getStringExtra("stopID")?.let(::normalizeStopId)
        val stopPreferences = WatchStopPreferences(applicationContext)
        watchAnalyticsTracker = WatchAnalyticsTracker(applicationContext)
        if (stopID != null) {
            appOpenEntryPoint = WatchAnalyticsTracker.EntryPoint.TILE
            stopPreferences.recentStopId = stopID
            if (savedInstanceState == null) {
                watchAnalyticsTracker.trackStopSelected(stopID, WatchAnalyticsTracker.EntryPoint.TILE)
            }
        }
        setContent {
            NavHostScreen(stopID, stopPreferences.recentStopId) { selectedStopID ->
                stopPreferences.recentStopId = selectedStopID
                watchAnalyticsTracker.trackStopSelected(selectedStopID, WatchAnalyticsTracker.EntryPoint.APP)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        watchAnalyticsTracker.trackAppOpen(appOpenEntryPoint)
        appOpenEntryPoint = WatchAnalyticsTracker.EntryPoint.APP
    }

    companion object {
        private val hanyangBlue = Color(0xFF0E4A84)
        private val stops = listOf(
            ShuttleStop("dormitory", R.string.stop_dormitory),
            ShuttleStop("shuttlecock", R.string.stop_shuttlecock),
            ShuttleStop("station", R.string.stop_station),
            ShuttleStop("terminal", R.string.stop_terminal),
            ShuttleStop("jungang", R.string.stop_jungang),
        )

        @Composable
        fun NavHostScreen(
            stopID: String?,
            recentStopID: String? = null,
            onStopSelected: (String) -> Unit = {},
        ) {
            var isResolved by rememberSaveable(stopID) { mutableStateOf(stopID != null) }
            var initialStopID by rememberSaveable(stopID) { mutableStateOf(stopID) }
            var nearestStopID by rememberSaveable(stopID) { mutableStateOf<String?>(null) }

            HYUabotTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isResolved) {
                        FindNearestStop { resolvedNearestStopID ->
                            nearestStopID = resolvedNearestStopID
                            initialStopID = resolvedNearestStopID ?: recentStopID
                            isResolved = true
                        }
                    } else {
                        NavigationStack(
                            startRoute = initialStopID?.let { "detail/$it" } ?: Screen.Main.route,
                            nearestStopID = nearestStopID,
                            onStopSelected = onStopSelected,
                        )
                    }
                }
            }
        }

        @Composable
        private fun FindNearestStop(onResolved: (String?) -> Unit) {
            val context = androidx.compose.ui.platform.LocalContext.current
            var canUseLocation by remember { mutableStateOf<Boolean?>(null) }
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { result ->
                canUseLocation = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            }

            LaunchedEffect(Unit) {
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
                if (isGranted) {
                    canUseLocation = true
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            }

            LaunchedEffect(canUseLocation) {
                when (canUseLocation) {
                    true -> {
                        val location = WatchLocationProvider(context).currentLocation()
                        onResolved(
                            location?.let {
                                NearestStopResolver.resolve(it.latitude, it.longitude, it.accuracy)
                            },
                        )
                    }
                    false -> onResolved(null)
                    null -> Unit
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.finding_nearest_stop),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }

        @Composable
        fun MainScreen(
            navController: NavHostController,
            onStopSelected: (String) -> Unit = {},
        ) {
            val scrollState = rememberScalingLazyListState()
            Scaffold (
                positionIndicator = { PositionIndicator(scalingLazyListState = scrollState) }
            ) {
                ScalingLazyColumn (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = scrollState,
                ) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    stops.forEach { stop ->
                        item {
                            Button(
                                onClick = {
                                    onStopSelected(stop.id)
                                    navController.navigate("detail/${stop.id}")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White,
                                    backgroundColor = hanyangBlue,
                                    disabledBackgroundColor = hanyangBlue.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(stringResource(stop.labelRes))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }

        @Composable
        fun DepartureListScreen(
            stopID: String,
            isNearest: Boolean = false,
            onShowOtherStops: () -> Unit = {},
        ) {
            val viewModel = remember(stopID) { MainViewModel(GraphQLModule.getInstance(), stopID) }
            // Define the lifecycle events to start and stop the ViewModel
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.start() }
            LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.stop() }
            // Observe the result from the ViewModel
            val firstItem = viewModel.firstItem.observeAsState()
            val secondItem = viewModel.secondItem.observeAsState()
            val thirdItem = viewModel.thirdItem.observeAsState()
            val fourthItem = viewModel.fourthItem.observeAsState()
            val result = viewModel.result.observeAsState()
            val isLoading = viewModel.isLoading.observeAsState(true)
            val scrollState = rememberScalingLazyListState()
            // Display the result
            Scaffold (
                positionIndicator = { PositionIndicator(scalingLazyListState = scrollState) }
            ) {
                if (stopID == "dormitory") {
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                        item { DepartureHeader(stopID, isNearest) }
                        item { ShuttleButton(stringResource(R.string.stop_station), firstItem.value?.time, isLoading.value) }
                        item { ShuttleButton(stringResource(R.string.stop_terminal), secondItem.value?.time, isLoading.value) }
                        item { ShuttleButton(stringResource(R.string.stop_jungang), thirdItem.value?.time, isLoading.value) }
                        item { OtherStopsButton(onShowOtherStops) }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                } else if (stopID == "shuttlecock"){
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                        item { DepartureHeader(stopID, isNearest) }
                        item { ShuttleButton(stringResource(R.string.stop_station), firstItem.value?.time, isLoading.value) }
                        item { ShuttleButton(stringResource(R.string.stop_terminal), secondItem.value?.time, isLoading.value) }
                        item { ShuttleButton(stringResource(R.string.stop_jungang), thirdItem.value?.time, isLoading.value) }
                        item { ShuttleButton(stringResource(R.string.stop_dormitory), fourthItem.value?.time, isLoading.value) }
                        item { OtherStopsButton(onShowOtherStops) }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                } else if (stopID == "station"){
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                        item { DepartureHeader(stopID, isNearest) }
                        item { ShuttleButton(
                            if (firstItem.value?.route?.name?.endsWith("S") == true) {
                                stringResource(R.string.stop_shuttlecock)
                            } else {
                                stringResource(R.string.stop_dormitory)
                            },
                            firstItem.value?.time,
                            isLoading.value,
                        )}
                        item { ShuttleButton(stringResource(R.string.stop_terminal), secondItem.value?.time, isLoading.value) }
                        item { ShuttleButton(stringResource(R.string.stop_jungang), thirdItem.value?.time, isLoading.value) }
                        item { OtherStopsButton(onShowOtherStops) }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                } else if (stopID == "terminal"){
                    if (result.value == null || result.value!!.isEmpty()) {
                        ScalingLazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            state = scrollState,
                        ) {
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                            item { DepartureHeader(stopID, isNearest) }
                            item {
                                if (isLoading.value) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        stringResource(R.string.no_scheduled_shuttle),
                                        textAlign = TextAlign.Center,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 16.dp),
                                    )
                                }
                            }
                            item { OtherStopsButton(onShowOtherStops) }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    } else {
                        ScalingLazyColumn (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            state = scrollState,
                        ) {
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                            item { DepartureHeader(stopID, isNearest) }
                            result.value?.subList(0, minOf(result.value!!.size, 3))?.forEach { item ->
                                item { ShuttleButton(
                                    if (item.route.name.endsWith("S")) {
                                        stringResource(R.string.stop_shuttlecock)
                                    } else {
                                        stringResource(R.string.stop_dormitory)
                                    },
                                    item.time
                                )}
                            }
                            item { OtherStopsButton(onShowOtherStops) }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                } else if (stopID == "jungang"){
                    if (result.value == null || result.value!!.isEmpty()) {
                        ScalingLazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            state = scrollState,
                        ) {
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                            item { DepartureHeader(stopID, isNearest) }
                            item {
                                if (isLoading.value) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        stringResource(R.string.no_scheduled_shuttle),
                                        textAlign = TextAlign.Center,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 16.dp),
                                    )
                                }
                            }
                            item { OtherStopsButton(onShowOtherStops) }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    } else {
                        ScalingLazyColumn (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            state = scrollState,
                        ) {
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                            item { DepartureHeader(stopID, isNearest) }
                            result.value?.subList(0, minOf(result.value!!.size, 3))?.forEach { item ->
                                item { ShuttleButton(stringResource(R.string.stop_dormitory), item.time) }
                            }
                            item { OtherStopsButton(onShowOtherStops) }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }

        @SuppressLint("DefaultLocale")
        private fun shuttleTime(time: LocalTime): String {
            return String.format("%02d:%02d", time.hour, time.minute)
        }

        @Composable
        private fun DepartureHeader(stopID: String, isNearest: Boolean) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isNearest) {
                    Text(
                        text = stringResource(R.string.nearest_stop),
                        color = Color(0xFFB8C7D9),
                        maxLines = 1,
                    )
                }
                Text(
                    text = stopName(stopID),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        @Composable
        private fun OtherStopsButton(onClick: () -> Unit) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    backgroundColor = Color(0xFF303033),
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Text(
                    text = stringResource(R.string.other_stops),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        @Composable
        private fun stopName(stopID: String): String = stringResource(
            stops.firstOrNull { it.id == stopID }?.labelRes ?: R.string.shuttle,
        )

        @Composable
        private fun ShuttleButton(
            destination: String,
            time: LocalTime?,
            isLoading: Boolean = false,
            modifier: Modifier = Modifier
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(hanyangBlue),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = destination,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (time == null) stringResource(R.string.shuttle_service_ended) else shuttleTime(time),
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        private data class ShuttleStop(val id: String, val labelRes: Int)

        private fun normalizeStopId(stopID: String): String = when (stopID) {
            "기숙사" -> "dormitory"
            "셔틀콕" -> "shuttlecock"
            "한대앞" -> "station"
            "예술인" -> "terminal"
            "중앙역" -> "jungang"
            else -> stopID
        }
    }
}
