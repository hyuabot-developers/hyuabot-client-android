/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package app.kobuggi.hyuabot.presentation

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
import app.kobuggi.hyuabot.presentation.NavigationUtils.Companion.NavigationStack
import app.kobuggi.hyuabot.presentation.theme.HYUabotTheme
import app.kobuggi.hyuabot.service.GraphQLModule
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        val stopID = intent.getStringExtra("stopID")
        setContent {
            NavHostScreen(stopID)
        }
    }

    companion object {
        private val hanyangBlue = Color(0xFF0E4A84)
        private val stops = listOf(
            "기숙사",
            "셔틀콕",
            "한대앞",
            "예술인",
            "중앙역",
        )

        @Composable
        fun NavHostScreen(stopID: String?) {
            HYUabotTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000)),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationStack(if (stopID != null) "detail/${stopID}" else Screen.Main.route)
                }
            }
        }

        @Composable
        fun MainScreen(navController: NavHostController) {
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
                                onClick = { navController.navigate("detail/$stop") },
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
                                Text(stop)
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
            val result = viewModel.result.observeAsState()
            val firstItem = viewModel.firstItem.observeAsState()
            val secondItem = viewModel.secondItem.observeAsState()
            val thirdItem = viewModel.thirdItem.observeAsState()
            val fourthItem = viewModel.fourthItem.observeAsState()
            val scrollState = rememberScalingLazyListState()
            // Display the result
            Scaffold (
                positionIndicator = { PositionIndicator(scalingLazyListState = scrollState) }
            ) {
                if (stopID == "기숙사") {
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("한대앞")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (firstItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(firstItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("예술인")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (secondItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(secondItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("중앙역")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (thirdItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(thirdItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                } else if (stopID == "셔틀콕"){
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("한대앞")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (firstItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(firstItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("예술인")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (secondItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(secondItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("중앙역")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (thirdItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(thirdItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("기숙사")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (fourthItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(fourthItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                } else if (stopID == "한대앞"){
                    ScalingLazyColumn (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        state = scrollState,
                    ) {
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    if (firstItem.value?.route?.endsWith("S") == true) {
                                        Text("셔틀콕")
                                    } else {
                                        Text("기숙사")
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (firstItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(firstItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("예술인")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (secondItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(secondItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = { },
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                ) {
                                    Text("중앙역")
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (thirdItem.value == null) {
                                        Text("운행 종료")
                                    } else {
                                        Text(shuttleTime(thirdItem.value!!.time))
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                } else if (stopID == "예술인"){
                    if (result.value == null || result.value!!.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("도착 예정인\n셔틀이 없습니다.", textAlign = TextAlign.Center, color = Color(0xFF0E4A84))
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
                                item {
                                    Button(
                                        onClick = { },
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
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        ) {
                                            if (item.route.endsWith("S")) {
                                                Text("셔틀콕")
                                            } else if (item.route.endsWith("D")) {
                                                Text("기숙사")
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(shuttleTime(item.time))
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                } else if (stopID == "중앙역"){
                    if (result.value == null || result.value!!.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("도착 예정인\n셔틀이 없습니다.", textAlign = TextAlign.Center, color = Color(0xFF0E4A84))
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
                                item {
                                    Button(
                                        onClick = { },
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
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        ) {
                                            Text("기숙사")
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(shuttleTime(item.time))
                                        }
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }

        private fun shuttleTime(departureTimeString: String): String {
            val departureTime: LocalTime = LocalTime.parse(departureTimeString)
            return "${departureTime.hour}:${departureTime.minute}"
        }
    }
}
