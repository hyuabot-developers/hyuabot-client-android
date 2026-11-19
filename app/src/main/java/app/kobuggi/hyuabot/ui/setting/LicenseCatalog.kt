package app.kobuggi.hyuabot.ui.setting

enum class LicenseKind(val title: String) {
    Apache("Apache License 2.0"),
    Mit("MIT License"),
    Terms("Service terms"),
}

/** 앱이 직접 의존하는 라이브러리. 버전은 gradle/libs.versions.toml과 맞춰 둔다. */
data class LibraryLicense(
    val name: String,
    val version: String,
    val kind: LicenseKind,
    val holder: String,
    val url: String,
)

object LicenseCatalog {
    private const val GOOGLE = "Google LLC"
    private const val ANDROIDX = "The Android Open Source Project"
    private const val ANDROIDX_URL = "https://developer.android.com/jetpack/androidx"

    val libraries: List<LibraryLicense> = listOf(
        // Jetpack
        LibraryLicense("AndroidX Core KTX", "1.19.0", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("AndroidX AppCompat", "1.7.1", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("AndroidX Browser", "1.9.0", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("AndroidX Compose (BOM)", "2026.06.01", LicenseKind.Apache, ANDROIDX, "https://developer.android.com/jetpack/compose"),
        LibraryLicense("AndroidX Compose Material 3", "1.4.0", LicenseKind.Apache, ANDROIDX, "https://developer.android.com/jetpack/androidx/releases/compose-material3"),
        LibraryLicense("AndroidX Core RemoteViews", "1.1.0", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("AndroidX AppFunctions", "1.0.0-beta01", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("AndroidX DataStore", "1.2.1", LicenseKind.Apache, ANDROIDX, "https://developer.android.com/jetpack/androidx/releases/datastore"),
        LibraryLicense("AndroidX Lifecycle", "2.11.0", LicenseKind.Apache, ANDROIDX, "https://developer.android.com/jetpack/androidx/releases/lifecycle"),
        LibraryLicense("AndroidX Navigation", "2.9.8", LicenseKind.Apache, ANDROIDX, "https://developer.android.com/jetpack/androidx/releases/navigation"),
        LibraryLicense("AndroidX Room", "2.8.4", LicenseKind.Apache, ANDROIDX, "https://developer.android.com/jetpack/androidx/releases/room"),
        LibraryLicense("AndroidX SplashScreen", "1.2.0", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("AndroidX SwipeRefreshLayout", "1.2.0", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("AndroidX ViewPager2", "1.1.0", LicenseKind.Apache, ANDROIDX, ANDROIDX_URL),
        LibraryLicense("Material Components for Android", "1.14.0", LicenseKind.Apache, GOOGLE, "https://github.com/material-components/material-components-android"),
        // Kotlin · DI · 네트워크
        LibraryLicense("Kotlin Standard Library", "2.3.21", LicenseKind.Apache, "JetBrains s.r.o.", "https://github.com/JetBrains/kotlin"),
        LibraryLicense("Hilt (Dagger)", "2.59.2", LicenseKind.Apache, GOOGLE, "https://github.com/google/dagger"),
        LibraryLicense("OkHttp", "5.4.0", LicenseKind.Apache, "Square, Inc.", "https://github.com/square/okhttp"),
        LibraryLicense("Gson", "2.14.0", LicenseKind.Apache, GOOGLE, "https://github.com/google/gson"),
        LibraryLicense("RxJava", "3.1.12", LicenseKind.Apache, "RxJava Contributors", "https://github.com/ReactiveX/RxJava"),
        LibraryLicense("RxAndroid", "3.0.2", LicenseKind.Apache, "The RxAndroid authors", "https://github.com/ReactiveX/RxAndroid"),
        LibraryLicense("Apollo Kotlin", "5.0.0", LicenseKind.Mit, "Apollo Graph, Inc.", "https://github.com/apollographql/apollo-kotlin"),
        LibraryLicense("Apollo Normalized Cache", "1.0.4", LicenseKind.Mit, "Apollo Graph, Inc.", "https://github.com/apollographql/apollo-kotlin-normalized-cache"),
        // UI 라이브러리
        LibraryLicense("Calendar (kizitonwose)", "2.10.1", LicenseKind.Mit, "Kizito Nwose", "https://github.com/kizitonwose/Calendar"),
        LibraryLicense("SkeletonLayout", "6.0.0", LicenseKind.Apache, "Faltenreich", "https://github.com/Faltenreich/SkeletonLayout"),
        // Firebase
        LibraryLicense("Firebase Android SDK", "34.16.0", LicenseKind.Apache, GOOGLE, "https://github.com/firebase/firebase-android-sdk"),
        // 서비스 약관이 적용되는 SDK
        LibraryLicense("Google Play services Location", "21.3.0", LicenseKind.Terms, GOOGLE, "https://developer.android.com/studio/terms"),
        LibraryLicense("Google Play In-App Review", "2.0.2", LicenseKind.Terms, GOOGLE, "https://developer.android.com/studio/terms"),
        LibraryLicense("ML Kit Translate", "17.0.3", LicenseKind.Terms, GOOGLE, "https://developers.google.com/ml-kit/terms"),
        LibraryLicense("NAVER Maps SDK for Android", "3.24.0", LicenseKind.Terms, "NAVER Cloud Corp.", "https://navermaps.github.io/android-map-sdk/guide-ko/"),
    ).sortedBy { it.name.lowercase() }

    private const val APACHE_BODY = """Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License."""

    private const val MIT_BODY = """Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE."""

    /** 라이선스 전문. 서비스 약관은 전문 대신 링크로 안내한다. */
    fun text(library: LibraryLicense): String? = when (library.kind) {
        LicenseKind.Apache -> "Copyright ${library.holder}\n\n$APACHE_BODY"
        LicenseKind.Mit -> "Copyright (c) ${library.holder}\n\n$MIT_BODY"
        LicenseKind.Terms -> null
    }
}
