package app.kobuggi.hyuabot.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class NavigationUtils {
    companion object {
        @Composable
        fun NavigationStack(
            startRoute: String = Screen.Main.route,
            nearestStopID: String? = null,
            onStopSelected: (String) -> Unit = {},
        ) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = startRoute) {
                composable(Screen.Main.route) {
                    MainActivity.MainScreen(navController, onStopSelected)
                }
                composable(
                    route = Screen.Detail.route + "/{stopID}",
                    arguments = listOf(
                        navArgument("stopID") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) {
                    val stopID = it.arguments?.getString("stopID")
                    if (stopID != null) {
                        MainActivity.DepartureListScreen(
                            stopID = stopID,
                            isNearest = stopID == nearestStopID,
                            onShowOtherStops = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
