# UI/UX verification

This change covers F-01–F-11 in the approved UI/UX handover. The following checks describe what to inspect on an installed app; they do not imply a production device check.

| Area | Check on Android |
|---|---|
| Map (F-01) | Search a building and a room, open each result, and confirm the map marker and detail point to the selected place. |
| Transit status (F-02) | Open each bus and subway tab. Check loading, empty, offline/error with retry, normal `HH:mm` check time, scheduled rows, and stale server data (bus ≥120 s; newest train update per station ≥180 s). Future server times are normal. |
| Transit rows (F-03, F-05, F-09) | Check long route/direction text and a follow-on arrival on its own line with the actual target stop. Check text legibility in light and dark themes; transit colors remain markers or chip backgrounds and green floating buttons use dark icons. |
| Today Home (F-04) | Check shuttle → optional location bus → all cafeterias while scrolling. Confirm two shuttle departures, up to two transfers per departure, bus alternatives, scheduled candidates, bus 50 priority, and the two-leg Incheon/Sosa route with transfer station, line, direction, and connected shuttle. The weather is one line; tap the visible Open-Meteo source link to open its full summary and source. Home settings stays in the header. |
| Home location (F-04) | Deny location and test missing/inaccurate/expired fixes: the bus card hides. Check the 1,500 m range and Suwon 2,000 m range. Manual dormitory and Shuttlecock select their bus groups without GPS. Manual station, terminal, and Jungang request GPS on entry, foreground, and user refresh; automatic polling does not move the manual shuttle departure. |
| Cafeteria and calendar (F-06, F-07) | Confirm every cafeteria/menu and the selected-day empty calendar message, with a month/year heading. |
| Settings (F-08) | Open licenses in a release build and confirm a populated license list and consistent header. The release bundle contains generated license metadata and license text; the actual release screen still needs a device check. |
| Timetable and coach marks (F-10, F-11) | Scroll shuttle timetable to its last departure: the fixed filter footer must not cover the row. Reopen the existing shuttle flow and advance all coach-mark steps, including anchors that are absent. |

Local validation: `./gradlew ktlintCheck :app:compileDevDebugKotlin :app:testDevDebugUnitTest app:koverVerifyDevDebug :app:bundleProductionRelease :app:assembleDevDebug` passed. The production AAB includes `third_party_license_metadata` and `third_party_licenses`.

Visual environment: `Medium_Phone` emulator. Home, bus, subway, shuttle timetable last row, and dark-theme layouts were inspected. Landscape and a temporary 800×1280 dp tablet configuration showed no overlap; the emulator display settings were restored. With both emulator networks disabled, refreshing Home while the location bus card was hidden showed the offline message and retry control above the shuttle card; both networks were restored. The agreed 360×640 dp minimum was checked from layout constraints but not run on a device. Maximum font size and real TalkBack behavior still need a device check. The real release license screen, 02:00–05:00 stale behavior, and location edge cases require device or timed checks. New ko/en/ja/zh text is automatic translation without native-speaker review.
