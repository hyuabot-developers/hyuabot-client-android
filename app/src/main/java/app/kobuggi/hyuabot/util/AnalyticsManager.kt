package app.kobuggi.hyuabot.util

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

/**
 * Central Firebase Analytics catalog.
 *
 * All event names, screen names, and item identifiers live here so the set of
 * tracked events is defined in one place. The string values use GA4-style
 * snake_case and the GA4 reserved events ([FirebaseAnalytics.Event.SCREEN_VIEW],
 * [FirebaseAnalytics.Event.SELECT_CONTENT]).
 *
 * Unification rule: the [AnalyticsScreen.id] / [AnalyticsItem.id] strings are the
 * cross-platform contract and MUST stay identical to the iOS client
 * (see iOS `hyuabot/Analytics/AnalyticsManager.swift` and `docs/analytics-events.md`).
 * Enum case names may differ between platforms; the string id may not.
 *
 * Collection is gated by the user's analytics consent via
 * `setAnalyticsCollectionEnabled` (see GlobalApplication / SettingFragment), so
 * callers here do not need to check consent — disabled collection drops events.
 */

/** Every user-visible screen. [id] == GA4 `screen_name` (shared with iOS). */
enum class AnalyticsScreen(val id: String) {
    // Home
    HOME("home"),

    // Shuttle
    SHUTTLE_REALTIME("shuttle_realtime"),
    SHUTTLE_TIMETABLE("shuttle_timetable"),
    SHUTTLE_TIMETABLE_FILTER("shuttle_timetable_filter"),
    SHUTTLE_STOP_INFO("shuttle_stop_info"),
    SHUTTLE_STOP_TIMETABLE("shuttle_stop_timetable"), // Android: per-stop entire-timetable dialog
    SHUTTLE_VIA("shuttle_via"),
    SHUTTLE_HELP("shuttle_help"),

    // Bus
    BUS_REALTIME("bus_realtime"),
    BUS_TIMETABLE("bus_timetable"),
    BUS_STOP_INFO("bus_stop_info"),
    BUS_HELP("bus_help"),
    BUS_DEPARTURE_LOG("bus_departure_log"),
    BUS_ROUTE_INFO("bus_route_info"), // Android: bus route info dialog

    // Subway
    SUBWAY_REALTIME("subway_realtime"),
    SUBWAY_TIMETABLE("subway_timetable"),

    // Cafeteria
    CAFETERIA("cafeteria"),

    // Map
    MAP("map"),

    // Others
    READING_ROOM("reading_room"),
    CONTACT("contact"),
    CALENDAR("calendar"),
    SETTING("setting"),
    WEB_VIEW("web_view"),
    BIRTHDAY("birthday"),

    // Android-only screens
    MENU("menu"),                       // bottom-nav "more" hub
    CAMPUS("campus"),
    NOTICE("notice"),                   // notice list
    SETTING_CAMPUS("setting_campus"),   // campus picker dialog
    SETTING_THEME("setting_theme"),     // theme picker dialog
    SETTING_LANGUAGE("setting_language"), // language picker dialog
    SETTING_DEVELOPER("setting_developer"), // app info / developer dialog
}

/** What kind of element was selected. [id] == GA4 `content_type` (shared with iOS). */
enum class AnalyticsContentType(val id: String) {
    BUTTON("button"),
    TAB("tab"),
    LIST_ITEM("list_item"),
    TOGGLE("toggle"),
    MENU("menu"),
    DATE_CONTROL("date_control"),
}

/** Event-scoped parameters registered as GA4 custom dimensions. */
object AnalyticsParameter {
    const val SCHEMA_VERSION = "analytics_schema_version"
    const val ELEMENT_ID = "element_id"
    const val ELEMENT_TYPE = "element_type"
    const val DESTINATION_ID = "destination_id"
}

const val ANALYTICS_SCHEMA_VERSION = "2"

/** Every tappable element. [id] == GA4 `item_id` (shared with iOS). */
enum class AnalyticsItem(val id: String) {
    // Tab bar (bottom navigation)
    TAB_HOME("tab_home"),
    TAB_SHUTTLE("tab_shuttle"),
    TAB_BUS("tab_bus"),
    TAB_SUBWAY("tab_subway"),
    TAB_CAFETERIA("tab_cafeteria"),
    TAB_MENU("tab_menu"), // Android 5th tab
    TAB_CAMPUS("tab_campus"),

    // Home
    HOME_TRY("home_try"),
    HOME_DISMISS_PROMPT("home_dismiss_prompt"),
    HOME_OPEN_LEGACY_SHUTTLE("home_open_legacy_shuttle"),
    HOME_OPEN_SHUTTLE_DETAIL("home_open_shuttle_detail"),
    HOME_OPEN_CAFETERIA("home_open_cafeteria"),
    HOME_REFRESH("home_refresh"),
    HOME_SELECT_DESTINATION("home_select_destination"),

    // Shuttle
    SHUTTLE_OPEN_HELP("shuttle_open_help"),
    SHUTTLE_SHOW_STOP_MODAL("shuttle_show_stop_modal"),
    SHUTTLE_SHOW_ENTIRE_TIMETABLE("shuttle_show_entire_timetable"),
    SHUTTLE_TRANSFER_INFO("shuttle_transfer_info"),
    SHUTTLE_SELECT_VIA_ROW("shuttle_select_via_row"),
    SHUTTLE_OPEN_FILTER("shuttle_open_filter"),
    SHUTTLE_FILTER_CONFIRM("shuttle_filter_confirm"),

    // Bus
    BUS_OPEN_HELP("bus_open_help"),
    BUS_STOP_BUTTON("bus_stop_button"),
    BUS_SHOW_ENTIRE_TIMETABLE("bus_show_entire_timetable"),
    BUS_SHOW_DEPARTURE_LOG("bus_show_departure_log"),
    BUS_ROUTE_INFO("bus_route_info"),

    // Subway
    SUBWAY_SHOW_ENTIRE_TIMETABLE("subway_show_entire_timetable"),

    // Cafeteria
    CAFETERIA_PREVIOUS_DATE("cafeteria_previous_date"),
    CAFETERIA_NEXT_DATE("cafeteria_next_date"),
    CAFETERIA_DATE_CHANGED("cafeteria_date_changed"),
    CAFETERIA_SHARE_BUTTON("cafeteria_share_button"),

    // Reading room
    READING_ROOM_SELECT_ROW("reading_room_select_row"),
    READING_ROOM_ALARM_TOGGLE("reading_room_alarm_toggle"),

    // Contact / Map
    CONTACT_SELECT_ROW("contact_select_row"),
    MAP_SELECT_SEARCH_RESULT("map_select_search_result"),
    MAP_RECENTER("map_recenter"),

    // Setting
    SETTING_SELECT_ROW("setting_select_row"),
    SETTING_SELECT_CAMPUS("setting_select_campus"),
    SETTING_SELECT_THEME("setting_select_theme"),
    SETTING_SELECT_LANGUAGE("setting_select_language"),

    // Notice (Android-only)
    NOTICE_OPEN("notice_open"),
    MENU_SELECT_ROW("menu_select_row"),
    CAMPUS_SELECT_TOOL("campus_select_tool"),

    // Birthday dialog
    BIRTHDAY_DO_NOT_SHOW("birthday_do_not_show"),
    BIRTHDAY_DISMISS("birthday_dismiss"),
}

/** Thin wrapper over Firebase Analytics. The only place that calls `logEvent`. */
object AnalyticsManager {
    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }
    private val collectionGate = AnalyticsCollectionGate()

    /**
     * Applies the user's collection preference and flushes events that arrived
     * while DataStore was still loading the preference at app startup.
     */
    fun setCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
        collectionGate.setEnabled(enabled)
    }

    /** Logs a GA4 `screen_view` event. */
    fun logScreen(screen: AnalyticsScreen, screenClass: String? = null) {
        collectionGate.runWhenEnabled {
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, screen.id)
                screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
                param(AnalyticsParameter.SCHEMA_VERSION, ANALYTICS_SCHEMA_VERSION)
            }
        }
    }

    /**
     * Logs a GA4 `select_content` event for a tap/selection.
     *
     * @param item the catalog identifier (becomes `item_id`).
     * @param type the kind of element (becomes `content_type`).
     * @param name optional contextual label (becomes `item_name`), e.g. the
     *   selected stop, contact name, building name, campus, or theme.
     * @param destinationId optional low-cardinality destination identifier for
     *   funnel analysis. Unlike [name], this value is reportable in GA4.
     */
    fun logSelect(
        item: AnalyticsItem,
        type: AnalyticsContentType = AnalyticsContentType.BUTTON,
        name: String? = null,
        destinationId: String? = null,
    ) {
        val parameters = selectionParameters(item, type, name, destinationId)
        collectionGate.runWhenEnabled {
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                parameters.forEach { (key, value) -> param(key, value) }
            }
        }
    }
}

internal fun selectionParameters(
    item: AnalyticsItem,
    type: AnalyticsContentType,
    name: String?,
    destinationId: String?,
): Map<String, String> = buildMap {
    put(FirebaseAnalytics.Param.CONTENT_TYPE, type.id)
    put(FirebaseAnalytics.Param.ITEM_ID, item.id)
    name?.let { put(FirebaseAnalytics.Param.ITEM_NAME, it) }
    put(AnalyticsParameter.SCHEMA_VERSION, ANALYTICS_SCHEMA_VERSION)
    put(AnalyticsParameter.ELEMENT_ID, item.id)
    put(AnalyticsParameter.ELEMENT_TYPE, type.id)
    destinationId?.let { put(AnalyticsParameter.DESTINATION_ID, it) }
}

/** Thread-safe startup gate for consent-aware Analytics events. */
internal class AnalyticsCollectionGate {
    private val lock = Any()
    private var enabled: Boolean? = null
    private val pendingEvents = mutableListOf<() -> Unit>()

    fun setEnabled(isEnabled: Boolean) {
        val eventsToRun = synchronized(lock) {
            enabled = isEnabled
            val pending = if (isEnabled) pendingEvents.toList() else emptyList()
            pendingEvents.clear()
            pending
        }
        eventsToRun.forEach { it() }
    }

    fun runWhenEnabled(event: () -> Unit) {
        val shouldRun = synchronized(lock) {
            when (enabled) {
                true -> true
                false -> false
                null -> {
                    pendingEvents += event
                    false
                }
            }
        }
        if (shouldRun) event()
    }
}
