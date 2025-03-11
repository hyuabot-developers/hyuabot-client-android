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

    apollo {
        service("query") {
            packageName = "app.kobuggi.hyuabot"
            introspection {
                endpointUrl.set("https://api.hyuabot.app/query")
                schemaFile.set(file("src/main/graphql/schema.graphqls"))
            }
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
        minSdk = 26
        targetSdk = 35
        versionCode = 410000000
        versionName = "4.1.0"
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

androidComponents {
    onVariants {
        it.buildConfigFields.put("API_URL", BuildConfigField("String", props["API_URL"].toString(), "API_URL"))
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
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
    implementation(libs.playServicesMaps)
    implementation(libs.mapsUtils)
    // SplashScreen
    implementation(libs.splashScreen)
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
