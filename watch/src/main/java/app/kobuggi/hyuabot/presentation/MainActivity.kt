/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package app.kobuggi.hyuabot.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import app.kobuggi.hyuabot.presentation.NavigationUtils.Companion.NavigationStack
import app.kobuggi.hyuabot.presentation.theme.HYUabotTheme

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
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationStack(if (stopID != null) "detail/${stopID}" else Screen.Main.route)
                }
            }
        }

        @Composable
        fun MainScreen(navController: NavHostController) {
            val scrollState = rememberScrollState()
            Column (
                modifier = Modifier.fillMaxSize().padding(horizontal=8.dp).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                stops.forEach { stop ->
                    Button(
                        onClick = { navController.navigate("detail/$stop") },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            disabledContentColor = Color.White,
                            backgroundColor = hanyangBlue,
                            disabledBackgroundColor = hanyangBlue.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(stop)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        @Composable
        fun DepartureListScreen(stopID: String) {
            Text("Departure List for $stopID")
        }
    }
}
