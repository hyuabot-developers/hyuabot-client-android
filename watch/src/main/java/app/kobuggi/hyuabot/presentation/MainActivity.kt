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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        @Composable
        fun NavHostScreen(stopID: String?) {
            HYUabotTheme {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationStack(if (stopID != null) "detail/${stopID}" else Screen.Main.route)
                }
            }
        }

        @Composable
        fun MainScreen() {
            Text("Hello World!")
        }

        @Composable
        fun DepartureListScreen(stopID: String) {
            Text("Departure List for $stopID")
        }
    }
}
