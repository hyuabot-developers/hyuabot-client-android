import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.hiltPlugin)
    alias(libs.plugins.apollo)
    alias(libs.plugins.safeArgs)
    alias(libs.plugins.secretsGradlePlugin)
    alias(libs.plugins.googleServicesPlugin)
    alias(libs.plugins.crashlyticsPlugin)
    alias(libs.plugins.kover)
}

val props = Properties()
file("../local.properties").inputStream().use { props.load(it) }

apollo {
    service("query") {
        packageName = "app.kobuggi.hyuabot"
        introspection {
            endpointUrl.set("https://api.hyuabot.app/query")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
        }
        plugin("com.apollographql.cache:normalized-cache-apollo-compiler-plugin:1.0.3")
        pluginArgument("com.apollographql.cache.packageName", packageName.get())
        mapScalar("Date", "java.time.LocalDate", "app.kobuggi.hyuabot.service.query.LocalDateAdapter")
        mapScalar("LocalTime", "java.time.LocalTime", "app.kobuggi.hyuabot.service.query.LocalTimeAdapter")
        mapScalar("DateTime", "java.time.ZonedDateTime", "app.kobuggi.hyuabot.service.query.ZonedDateTimeAdapter")
    }
}

android {
    namespace = "app.kobuggi.hyuabot"
    compileSdk = 37

    signingConfigs {
        create("config") {
            storeFile = file(props["SIGNING_KEY_FILE"]?.toString() ?: "keystore.jks")
            storePassword = props["SIGNED_STORE_PASSWORD"]?.toString() ?: "storePassword"
            keyAlias = props["SIGNED_KEY_ALIAS"]?.toString() ?: "keyAlias"
            keyPassword = props["SIGNED_KEY_PASSWORD"]?.toString() ?: "keyPassword"
        }
    }

    defaultConfig {
        applicationId = "app.kobuggi.hyuabot"
        minSdk = 29
        targetSdk = 37
        versionCode = 515000000
        versionName = "5.1.5"
        signingConfig = signingConfigs.getByName("config")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAP_CLIENT_ID"] = props["MAP_CLIENT_ID"]?.toString() ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("config")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("config")
        }
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    flavorDimensions.add("appType")
    productFlavors {
        create("dev") {
            dimension = "appType"
            buildConfigField("String", "API_URL", "\"https://backend.hyuabot.app\"")
        }
        create("production") {
            dimension = "appType"
            buildConfigField("String", "API_URL", "\"https://backend.hyuabot.app\"")
        }
    }

    tasks.withType(JavaCompile::class.java) {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:unchecked",
                "-Xlint:deprecation",
            ),
        )
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.dynamic.features.fragment)
    // RXJava
    implementation(libs.rxJava)
    implementation(libs.rxAndroid)
    // Networking
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.apollo)
    implementation(libs.apolloNormalizedCache)
    // DataStore
    implementation(libs.dataStore)
    implementation(libs.dataStoreRx)
    // ViewPager2
    implementation(libs.viewPager2)
    // Room
    implementation(libs.room)
    implementation(libs.roomRx)
    implementation(libs.roomKtx)
    implementation(libs.roomPaging)
    ksp(libs.roomCompiler)
    // Calendar
    implementation(libs.calendar)
    // Firebase
    implementation(platform(libs.firebase))
    implementation(libs.firebaseMessaging)
    implementation(libs.firebaseAnalytics)
    implementation(libs.firebaseCrashlytics)
    // Map
    implementation(libs.naver.map)
    // SplashScreen
    implementation(libs.splashScreen)
    // SwipeRefreshLayout
    implementation(libs.swipeRefreshLayout)
    // Play Services
    implementation(libs.playServicesLocation)
    // Android LiveData
    implementation(libs.lifeCycleLiveData)
    implementation(libs.lifeCycleRuntime)
    // In-App Review
    implementation(libs.playReview)
    implementation(libs.playReviewKtx)
    // App Widget
    implementation(libs.coreRemoteViews)
}

hilt {
    enableAggregatingTask = true
}

kover {
    reports {
        variant("devDebug") {
            filters {
                includes {
                    classes(
                        "app.kobuggi.hyuabot.service.query.LocalDateAdapter",
                        "app.kobuggi.hyuabot.service.query.LocalTimeAdapter",
                        "app.kobuggi.hyuabot.service.query.ZonedDateTimeAdapter",
                        "app.kobuggi.hyuabot.ui.setting.CampusSettingDialogViewModel",
                        "app.kobuggi.hyuabot.ui.setting.LanguageSettingDialogViewModel",
                        "app.kobuggi.hyuabot.ui.setting.ThemeSettingDialogViewModel",
                        "app.kobuggi.hyuabot.util.UIUtility",
                        "app.kobuggi.hyuabot.widget.WidgetMeal",
                    )
                }
            }
            verify {
                rule("logic line coverage") {
                    minBound(100)
                }
            }
        }
    }
}
