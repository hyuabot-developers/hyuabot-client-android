package app.kobuggi.hyuabot.analytics

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WatchAnalyticsEventTest {
    private val gson = Gson()

    @Test
    fun appOpenUsesSharedEventContract() {
        val json = gson.toJsonTree(
            WatchAnalyticsEvent.appOpen(
                installationId = "123e4567-e89b-12d3-a456-426614174000",
                appVersion = "5.0.0",
                entryPoint = "app",
            ),
        ).asJsonObject

        assertEquals("watch_app_open", json["event"].asString)
        assertEquals("wear_os", json["platform"].asString)
        assertEquals("app", json["entryPoint"].asString)
        assertFalse(json.has("stopId"))
    }

    @Test
    fun stopSelectedIncludesStopAndEntryPoint() {
        val json = gson.toJsonTree(
            WatchAnalyticsEvent.stopSelected(
                installationId = "123e4567-e89b-12d3-a456-426614174000",
                appVersion = "5.0.0",
                entryPoint = "tile",
                stopId = "station",
            ),
        ).asJsonObject

        assertEquals("watch_stop_selected", json["event"].asString)
        assertEquals("wear_os", json["platform"].asString)
        assertEquals("tile", json["entryPoint"].asString)
        assertEquals("station", json["stopId"].asString)
    }
}
