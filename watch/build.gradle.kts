import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.hiltPlugin)
    alias(libs.plugins.apollo)
    alias(libs.plugins.safeArgs)
    alias(libs.plugins.secretsGradlePlugin)
    alias(libs.plugins.googleServicesPlugin)
    alias(libs.plugins.crashlyticsPlugin)
}

val props = Properties()
file("../local.properties").inputStream().use { props.load(it) }

android {
    namespace = "app.kobuggi.hyuabot"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.kobuggi.hyuabot"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

    }

    apollo {
        service("query") {
            packageName = "app.kobuggi.hyuabot"
        }
    }

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
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        signingConfig = signingConfigs.getByName("config")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = props["GOOGLE_MAP_API_KEY"]?.toString() ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("config")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("config")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    tasks.withType(JavaCompile::class.java) {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:unchecked",
                "-Xlint:deprecation",
            ),
        )
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
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
    // DataStore
    implementation(libs.dataStore)
    implementation(libs.dataStoreRx)
    // Firebase
    implementation(platform(libs.firebase))
    implementation(libs.firebaseMessaging)
    implementation(libs.firebaseAnalytics)
    implementation(libs.firebaseCrashlytics)
    // SwipeRefreshLayout
    implementation(libs.swipeRefreshLayout)
    // Play Services
    implementation(libs.playServicesLocation)
    // Android LiveData
    implementation(libs.lifeCycleLiveData)
}

hilt {
    enableAggregatingTask = true
}
