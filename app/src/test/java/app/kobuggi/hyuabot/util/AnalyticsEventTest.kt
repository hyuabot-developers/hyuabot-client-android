package app.kobuggi.hyuabot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class AnalyticsEventTest {
    @Test
    fun `event envelope adds schema and source without overriding caller values`() {
        val event = eventParameters("home", mapOf("analytics_schema_version" to "1", "value" to 4))

        assertEquals("3", event[AnalyticsParameter.SCHEMA_VERSION])
        assertEquals("home", event[AnalyticsParameter.SOURCE_SCREEN])
        assertEquals(4, event["value"])
    }

    @Test
    fun `navigation catalog classifies actual navigation selections`() {
        assertEquals("shuttle_realtime", navigationDestination(AnalyticsItem.HOME_OPEN_SHUTTLE_DETAIL))
        assertEquals("bus_route_info", navigationDestination(AnalyticsItem.BUS_ROUTE_INFO))
        assertEquals("setting_language", navigationDestination(AnalyticsItem.SETTING_SELECT_LANGUAGE))
        assertNull(navigationDestination(AnalyticsItem.HOME_SELECT_DESTINATION))
        assertNull(navigationDestination(AnalyticsItem.MAP_RECENTER))
    }

    @Test
    fun `menu tab aliases use the existing campus screen id`() {
        assertEquals("campus", navigationDestination(AnalyticsItem.TAB_CAMPUS))
        assertEquals("campus", navigationDestination(AnalyticsItem.TAB_MENU))
    }

    @Test
    fun `manual refresh event maps each domain and preserves home source`() {
        val expected = mapOf(
            ManualRefreshDomain.HOME to ("home_refresh" to "home"),
            ManualRefreshDomain.BUS to ("bus_refresh" to "bus_realtime"),
            ManualRefreshDomain.SUBWAY to ("subway_refresh" to "subway_realtime"),
            ManualRefreshDomain.READING_ROOM to ("reading_room_refresh" to "reading_room"),
        )
        expected.forEach { (domain, pair) ->
            val event = manualRefreshEvent(domain)
            assertEquals(pair.first, event.name)
            assertEquals("manual", event.parameters["trigger"])
            assertEquals(pair.second, event.parameters[AnalyticsParameter.SOURCE_SCREEN])
            assertEquals("3", event.parameters[AnalyticsParameter.SCHEMA_VERSION])
        }
    }

    @Test
    fun `quick setting values are strings and coachmark rejects invalid steps`() {
        val event = quickSettingEvent("show_presence_status", true)
        assertEquals("true", event.parameters["value"])
        assertEquals("show_presence_status", event.parameters["setting"])
        assertNotNull(coachmarkEvent("coachmark_step_view", 3))
        assertNull(coachmarkEvent("coachmark_skip", 4))
    }
}
