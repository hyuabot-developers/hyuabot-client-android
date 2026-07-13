package app.kobuggi.hyuabot.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
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
        val stopID = intent.getStringExtra("stopID")
        watchAnalyticsTracker = WatchAnalyticsTracker(applicationContext)
        if (stopID != null) {
            appOpenEntryPoint = WatchAnalyticsTracker.EntryPoint.TILE
            if (savedInstanceState == null) {
                watchAnalyticsTracker.trackStopSelected(normalizeStopId(stopID), WatchAnalyticsTracker.EntryPoint.TILE)
            }
        }
        setContent {
            NavHostScreen(stopID) { selectedStopID ->
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
            onStopSelected: (String) -> Unit = {},
        ) {
            HYUabotTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000)),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationStack(
                        startRoute = if (stopID != null) "detail/${normalizeStopId(stopID)}" else Screen.Main.route,
                        onStopSelected = onStopSelected,
                    )
                }
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
        fun DepartureListScreen(stopID: String, viewModel: MainViewModel = MainViewModel(GraphQLModule.getInstance(), stopID)) {
            // Define the lifecycle events to start and stop the ViewModel
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.start() }
            LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.stop() }
            // Observe the result from the ViewModel
            val firstItem = viewModel.firstItem.observeAsState()
            val secondItem = viewModel.secondItem.observeAsState()
            val thirdItem = viewModel.thirdItem.observeAsState()
            val fourthItem = viewModel.fourthItem.observeAsState()
            val result = viewModel.result.observeAsState()
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
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                        item { ShuttleButton(stringResource(R.string.stop_station), firstItem.value?.time) }
                        item { ShuttleButton(stringResource(R.string.stop_terminal), secondItem.value?.time) }
                        item { ShuttleButton(stringResource(R.string.stop_jungang), thirdItem.value?.time) }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                } else if (stopID == "shuttlecock"){
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                        item { ShuttleButton(stringResource(R.string.stop_station), firstItem.value?.time) }
                        item { ShuttleButton(stringResource(R.string.stop_terminal), secondItem.value?.time) }
                        item { ShuttleButton(stringResource(R.string.stop_jungang), thirdItem.value?.time) }
                        item { ShuttleButton(stringResource(R.string.stop_dormitory), fourthItem.value?.time) }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                } else if (stopID == "station"){
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                        item { ShuttleButton(
                            if (firstItem.value?.route?.name?.endsWith("S") == true) {
                                stringResource(R.string.stop_shuttlecock)
                            } else {
                                stringResource(R.string.stop_dormitory)
                            },
                            firstItem.value?.time
                        )}
                        item { ShuttleButton(stringResource(R.string.stop_terminal), secondItem.value?.time) }
                        item { ShuttleButton(stringResource(R.string.stop_jungang), thirdItem.value?.time) }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                } else if (stopID == "terminal"){
                    if (result.value == null || result.value!!.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_scheduled_shuttle), textAlign = TextAlign.Center, color = Color(0xFF0E4A84))
                        }
                    } else {
                        ScalingLazyColumn (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            state = scrollState,
                        ) {
                            item { Spacer(modifier = Modifier.height(40.dp)) }
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
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                } else if (stopID == "jungang"){
                    if (result.value == null || result.value!!.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_scheduled_shuttle), textAlign = TextAlign.Center, color = Color(0xFF0E4A84))
                        }
                    } else {
                        ScalingLazyColumn (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            state = scrollState,
                        ) {
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                            result.value?.subList(0, minOf(result.value!!.size, 3))?.forEach { item ->
                                item { ShuttleButton(stringResource(R.string.stop_dormitory), item.time) }
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
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
        private fun ShuttleButton(
            destination: String,
            time: LocalTime?,
            modifier: Modifier = Modifier
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    backgroundColor = hanyangBlue,
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(destination)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(if (time == null) stringResource(R.string.shuttle_service_ended) else shuttleTime(time))
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
