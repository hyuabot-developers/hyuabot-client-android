package app.kobuggi.hyuabot.ui

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R

internal object DebugDeepLinkRouter {
    private const val DEBUG_HOST = "debug"

    fun uriFrom(intent: Intent?): android.net.Uri? {
        val uri = intent?.data ?: return null
        if (!BuildConfig.DEBUG || intent.action != Intent.ACTION_VIEW) return null
        if (uri.scheme != "hyuabot" || uri.host != DEBUG_HOST) return null
        return uri
    }

    fun navigate(uri: android.net.Uri, navController: NavController): Boolean {
        val route = uri.pathSegments.firstOrNull()?.lowercase() ?: return false
        val arguments = Bundle().apply {
            putInt("stopID", uri.intParameter("stopID", 1))
            putInt("destinationID", uri.intParameter("destinationID", 1))
            putInt("firstRouteID", uri.intParameter("firstRouteID", 0))
            putInt("secondRouteID", uri.intParameter("secondRouteID", 0))
            putInt("thirdRouteID", uri.intParameter("thirdRouteID", 0))
            putInt("routeID", uri.intParameter("routeID", 0))
            putInt("seq", uri.intParameter("seq", -1))
            putString("stationID", uri.getQueryParameter("stationID") ?: "K449")
            putString("heading", uri.getQueryParameter("heading") ?: "up")
            putString("tab", uri.getQueryParameter("tab").orEmpty())
            putString("stop", uri.getQueryParameter("stop").orEmpty())
            putString("to", uri.getQueryParameter("to").orEmpty())
            putString("entryScreen", uri.getQueryParameter("entryScreen") ?: "debug")
            putString("entryScreenName", uri.getQueryParameter("entryScreenName") ?: "Debug")
            uri.getQueryParameter("url")?.let { putString("url", it) }
            uri.getQueryParameter("title")?.let { putString("title", it) }
        }

        val destination = when (route) {
            "home" -> R.id.homeFragment
            "shuttle", "shuttle-realtime" -> R.id.shuttleRealtimeFragment
            "shuttle-timetable" -> R.id.shuttleTimetableFragment
            "shuttle-stop-dialog" -> R.id.shuttleStopDialogFragment
            "shuttle-help-dialog" -> R.id.shuttleHelpDialogFragment
            "shuttle-timetable-dialog" -> R.id.shuttleTimetableDialogFragment
            "shuttle-timetable-filter-dialog" -> R.id.shuttleTimetableFilterDialogFragment
            "bus", "bus-realtime" -> R.id.busRealtimeFragment
            "bus-timetable" -> R.id.busTimetableFragment
            "bus-help-dialog" -> R.id.busHelpDialogFragment
            "bus-stop-info" -> R.id.busStopInfoFragment
            "bus-departure-dialog" -> R.id.busDepartureLogDialogFragment
            "bus-route-dialog" -> R.id.busRouteInfoDialogFragment
            "subway", "subway-realtime" -> R.id.subwayRealtimeFragment
            "subway-timetable" -> R.id.subwayTimetableFragment
            "cafeteria" -> R.id.cafeteriaFragment
            "reading-room" -> R.id.readingRoomFragment
            "map" -> R.id.mapFragment
            "setting" -> R.id.settingFragment
            "contact" -> R.id.contactFragment
            "calendar" -> R.id.calendarFragment
            "inquiry" -> R.id.inquiryChatFragment
            "campus" -> R.id.menuFragment
            "notice" -> R.id.noticeWebViewFragment
            "language-setting-dialog" -> R.id.languageSettingDialogFragment
            "campus-setting-dialog" -> R.id.campusSettingDialogFragment
            "theme-setting-dialog" -> R.id.themeSettingDialogFragment
            "developer-dialog" -> R.id.settingDeveloperDialogFragment
            else -> return false
        }

        navController.navigate(destination, arguments)
        return true
    }

    private fun android.net.Uri.intParameter(name: String, default: Int): Int =
        getQueryParameter(name)?.toIntOrNull() ?: default
}
