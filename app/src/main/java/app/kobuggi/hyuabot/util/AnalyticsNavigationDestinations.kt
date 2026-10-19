package app.kobuggi.hyuabot.util

internal fun navigationDestination(item: AnalyticsItem, name: String? = null): String? = when (item) {
    AnalyticsItem.TAB_HOME -> AnalyticsScreen.HOME.id
    AnalyticsItem.TAB_SHUTTLE -> AnalyticsScreen.SHUTTLE_REALTIME.id
    AnalyticsItem.TAB_BUS -> AnalyticsScreen.BUS_REALTIME.id
    AnalyticsItem.TAB_SUBWAY -> AnalyticsScreen.SUBWAY_REALTIME.id
    AnalyticsItem.TAB_CAFETERIA -> AnalyticsScreen.CAFETERIA.id
    AnalyticsItem.TAB_MENU, AnalyticsItem.TAB_CAMPUS -> AnalyticsScreen.CAMPUS.id
    AnalyticsItem.CAMPUS_SELECT_TOOL -> AnalyticsScreen.CAMPUS.id
    AnalyticsItem.HOME_OPEN_SHUTTLE_DETAIL, AnalyticsItem.HOME_OPEN_LEGACY_SHUTTLE -> AnalyticsScreen.SHUTTLE_REALTIME.id
    AnalyticsItem.HOME_OPEN_SHUTTLE_TIMETABLE -> AnalyticsScreen.SHUTTLE_TIMETABLE.id
    AnalyticsItem.HOME_OPEN_CAFETERIA -> AnalyticsScreen.CAFETERIA.id
    AnalyticsItem.SHUTTLE_OPEN_HELP -> AnalyticsScreen.SHUTTLE_HELP.id
    AnalyticsItem.SHUTTLE_SHOW_STOP_MODAL -> AnalyticsScreen.SHUTTLE_STOP_INFO.id
    AnalyticsItem.SHUTTLE_SHOW_ENTIRE_TIMETABLE -> AnalyticsScreen.SHUTTLE_STOP_TIMETABLE.id
    AnalyticsItem.SHUTTLE_SELECT_VIA_ROW -> AnalyticsScreen.SHUTTLE_VIA.id
    AnalyticsItem.SHUTTLE_OPEN_FILTER -> AnalyticsScreen.SHUTTLE_TIMETABLE_FILTER.id
    AnalyticsItem.BUS_OPEN_HELP -> AnalyticsScreen.BUS_HELP.id
    AnalyticsItem.BUS_STOP_BUTTON -> AnalyticsScreen.BUS_STOP_INFO.id
    AnalyticsItem.BUS_SHOW_ENTIRE_TIMETABLE -> AnalyticsScreen.BUS_TIMETABLE.id
    AnalyticsItem.BUS_SHOW_DEPARTURE_LOG -> AnalyticsScreen.BUS_DEPARTURE_LOG.id
    AnalyticsItem.BUS_ROUTE_INFO -> AnalyticsScreen.BUS_ROUTE_INFO.id
    AnalyticsItem.SUBWAY_SHOW_ENTIRE_TIMETABLE -> AnalyticsScreen.SUBWAY_TIMETABLE.id
    AnalyticsItem.NOTICE_OPEN -> AnalyticsScreen.WEB_VIEW.id
    AnalyticsItem.SETTING_SELECT_CAMPUS -> AnalyticsScreen.SETTING_CAMPUS.id
    AnalyticsItem.SETTING_SELECT_THEME -> AnalyticsScreen.SETTING_THEME.id
    AnalyticsItem.SETTING_SELECT_LANGUAGE -> AnalyticsScreen.SETTING_LANGUAGE.id
    AnalyticsItem.SETTING_SELECT_ROW -> when (name) {
        "campus" -> AnalyticsScreen.SETTING_CAMPUS.id
        "theme" -> AnalyticsScreen.SETTING_THEME.id
        "language" -> AnalyticsScreen.SETTING_LANGUAGE.id
        "info", "developer" -> AnalyticsScreen.SETTING_DEVELOPER.id
        else -> null
    }
    AnalyticsItem.MENU_SELECT_ROW -> when (name) {
        "reading_room" -> AnalyticsScreen.READING_ROOM.id
        "map" -> AnalyticsScreen.MAP.id
        "contact" -> AnalyticsScreen.CONTACT.id
        "calendar" -> AnalyticsScreen.CALENDAR.id
        "setting" -> AnalyticsScreen.SETTING.id
        "notice" -> AnalyticsScreen.NOTICE.id
        else -> null
    }
    else -> null
}
