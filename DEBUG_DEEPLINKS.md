# Debug deep links

Debug builds accept `hyuabot://debug/<route>?<key>=<value>`. The debug host is
handled only when `BuildConfig.DEBUG` is true; release builds continue to use
the existing public routes and ignore this host.

Examples:

```sh
adb shell am start -a android.intent.action.VIEW \
  -d 'hyuabot://debug/shuttle?stop=station&to=terminal'
adb shell am start -a android.intent.action.VIEW \
  -d 'hyuabot://debug/bus-departure-dialog?stopID=216000383&firstRouteID=216000068'
adb shell am start -a android.intent.action.VIEW \
  -d 'hyuabot://debug/building-webview?title=Library&url=https%3A%2F%2Fhyuabot.app'
```

Supported page routes are `home`, `shuttle`, `bus`, `subway`, `cafeteria`,
`reading-room`, `map`, `setting`, `contact`, `calendar`, `inquiry`, and
`campus`. Timetable routes are `shuttle-timetable`, `bus-timetable`, and
`subway-timetable`.

Supported navigation dialogs are `shuttle-stop-dialog`,
`shuttle-help-dialog`, `shuttle-timetable-dialog`,
`shuttle-timetable-filter-dialog`, `bus-help-dialog`, `bus-stop-info`,
`bus-departure-dialog`, `bus-route-dialog`, `language-setting-dialog`,
`campus-setting-dialog`, `theme-setting-dialog`, and `developer-dialog`.

Supported direct sheets are `home-quick-settings`, `bus-quick-settings`,
`shuttle-quick-settings`, `shuttle-via`, and `building-webview`.

Useful arguments include `stopID`, `destinationID`, `firstRouteID`,
`secondRouteID`, `thirdRouteID`, `routeID`, `seq`, `stationID`, `heading`,
`tab`, `stop`, `to`, `url`, and `title`. Missing or invalid numeric values use
safe preview defaults.
