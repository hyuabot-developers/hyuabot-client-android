# 휴아봇 (HYUabot) — Android Client

A comprehensive campus information app for **Hanyang University ERICA Campus**, providing real-time transportation, dining, facility, and academic information. Includes a companion **Wear OS** app with a shuttle tile.

---

## Features

### Transportation
| Feature         | Description                                                                                                                          |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------|
| **Shuttle Bus** | Real-time arrivals and timetables at 6 campus stops (기숙사, 셔틀콕, 한대앞, 예술인, 중앙역, 셔틀콕 건너편). Nearest-stop detection via GPS.              |
| **City Bus**    | Real-time arrivals and seat availability for multiple routes at 7 stops. Includes departure logs and tab groupings (강남역, 수원역, etc.). |
| **Subway**      | Arrival times for Line 4 (4호선) and Incheon-Bundang Line (수인분당선), both directions.                                                    |

### Campus Life
| Feature          | Description                                                                                                                      |
|------------------|----------------------------------------------------------------------------------------------------------------------------------|
| **Cafeteria**    | Daily menus (breakfast / lunch / dinner) for 15+ dining locations, with operating hours and prices. Future-date picker included. |
| **Reading Room** | Seat availability per room/floor with configurable alerts (3 hr exam / 4 hr normal extension).                                   |
| **Campus Map**   | Interactive Google Maps view of campus buildings with classroom/room search.                                                     |

### Information
| Feature               | Description                                                |
|-----------------------|------------------------------------------------------------|
| **Academic Calendar** | University events with category filtering and date ranges. |
| **Notices**           | University announcements displayed in an in-app WebView.   |
| **Contact Directory** | Searchable campus phone directory.                         |

### Wear OS Companion
- Stop selection and real-time shuttle departure list on the wrist.
- **Shuttle Tile** on the watch face with one-tap deep links to each stop.

---

## Screenshots

> *(Add screenshots here)*

---

## Architecture

```
app/
└── src/main/java/app/kobuggi/hyuabot/
    ├── ui/            # Fragments & ViewModels (MVVM)
    │   ├── shuttle/
    │   ├── bus/
    │   ├── subway/
    │   ├── cafeteria/
    │   ├── readingRoom/
    │   ├── map/
    │   ├── calendar/
    │   ├── contact/
    │   ├── notice/
    │   └── setting/
    ├── service/
    │   ├── query/     # Apollo GraphQL client
    │   ├── database/  # Room entities & DAOs
    │   ├── preferences/ # DataStore
    │   └── alarm/     # Notification alarms
    └── util/          # Extension functions & helpers

watch/
└── src/main/java/app/kobuggi/hyuabot/
    ├── presentation/  # Compose UI + ViewModel
    └── tile/          # ShuttleTileService (Wear Tiles)
```

**Pattern:** MVVM · Hilt DI · Navigation Component (phone) · Jetpack Compose (watch)

---

## Tech Stack

### Core
| Category    | Library / Tool       | Version        |
|-------------|----------------------|----------------|
| Language    | Kotlin               | 2.3.21         |
| Min SDK     | Android 9.0 (API 28) | —              |
| Target SDK  | API 37               | —              |
| DI          | Hilt                 | 2.59.2         |
| Navigation  | AndroidX Navigation  | 2.9.8          |
| GraphQL     | Apollo Client        | 4.4.3          |
| HTTP        | OkHttp               | 5.3.2          |
| Database    | Room                 | 2.8.4          |
| Preferences | DataStore            | 1.2.1          |
| Reactive    | RxJava 3 + RxAndroid | 3.1.12 / 3.0.2 |

### UI
| Category        | Library                       | Version |
|-----------------|-------------------------------|---------|
| Material Design | Material 3                    | 1.13.0  |
| Pager           | ViewPager2                    | 1.1.0   |
| Calendar        | Kizitonwose Calendar          | 2.10.1  |
| Splash Screen   | AndroidX SplashScreen         | 1.2.0   |
| Maps            | Google Play Services Maps     | 20.0.0  |
| Location        | Google Play Services Location | 21.3.0  |

### Wear OS
| Category   | Library               | Version |
|------------|-----------------------|---------|
| Compose UI | Wear Compose Material | 1.6.1   |
| Tiles      | AndroidX Wear Tiles   | 1.6.0   |
| Horologist | Tile helpers          | 0.7.15  |

### Firebase
- Messaging (push notifications)
- Analytics
- Crashlytics

---

## Backend / API

All data is fetched via **GraphQL** from the HYUabot backend.

| Property | Value                                 |
|----------|---------------------------------------|
| Endpoint | `https://backend.hyuabot.app/graphql` |
| Protocol | GraphQL                               |
| Client   | Apollo Client 4.4.3                   |

Custom scalar adapters are registered for `Date` → `LocalDate`, `LocalTime` → `LocalTime`, and `DateTime` → `ZonedDateTime`.

GraphQL query files live in `app/src/main/graphql/` (shuttle, bus, subway, cafeteria, reading room, calendar, contact, building, etc.).

---

## Build & Run

### Prerequisites
- JDK 17
- Android SDK with API 37 platform
- A `local.properties` file in the project root (see below)

### `local.properties`
```properties
sdk.dir=/path/to/android/sdk

# Signing (release builds)
SIGNING_KEY_FILE=./release-key.jks
SIGNED_STORE_PASSWORD=...
SIGNED_KEY_ALIAS=...
SIGNED_KEY_PASSWORD=...

# API
API_URL=https://api.hyuabot.app/query

# Maps
GOOGLE_MAP_API_KEY=...
MAP_API_KEY=...
```

### Common commands
```bash
# Debug build & install (dev flavor)
./gradlew :app:installDebugDev

# Release bundle (AAB)
./gradlew bundleRelease

# Code style check
./gradlew ktlintCheck
```

### Build flavors
| Flavor       | Purpose            |
|--------------|--------------------|
| `dev`        | Local development  |
| `production` | Play Store release |

---

## CI/CD

Two GitHub Actions workflows run on a **self-hosted** runner:

| Workflow        | Trigger                                    | Steps                                                                                           |
|-----------------|--------------------------------------------|-------------------------------------------------------------------------------------------------|
| `build.yml`     | Push to `main`, merged PR, manual dispatch | JDK 17 setup → decode keystore & `google-services.json` → `bundleRelease` → upload AAB artifact |
| `codecheck.yml` | Push to any non-main branch, PR            | JDK 17 setup → `ktlintCheck`                                                                    |

Secrets required in the repository:
- `BASE64_KEYSTORE` — base64-encoded `.jks` signing keystore
- `SIGNED_KEY_ALIAS`, `SIGNED_KEY_PASSWORD`, `SIGNED_STORE_PASSWORD`
- `GOOGLE_SERVICES_JSON` — base64-encoded `google-services.json`
- `MAP_KEY` — Google Maps API key
- `API_URL` / `RELEASE_API_URL` — backend GraphQL URL

---

## Localization

The app ships with **Korean** (default) and **English** string resources. Language can be switched in-app from Settings.

---

## Developer

| | |
|---|---|
| **Developer** | 이정인 (Lee Jeongin) |
| **Affiliation** | Hanyang University ERICA · Software Engineering, Class of 2017 |
| **Package** | `app.kobuggi.hyuabot` |

---

## License

*License information not yet specified.*
