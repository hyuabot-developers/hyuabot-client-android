import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.hiltPlugin)
    alias(libs.plugins.apollo)
    alias(libs.plugins.safeArgs)
}

val props = Properties()
file("../local.properties").inputStream().use { props.load(it) }

android {
    namespace = "app.kobuggi.hyuabot"
    compileSdk = 34

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
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        signingConfig = signingConfigs.getByName("config")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
        it.buildConfigFields.put(
            "KAKAO_MAP_KEY",
            BuildConfigField(
                "String",
                props["KAKAO_MAP_KEY"].toString(),
                "KAKAO_MAP_KEY",
            ),
        )
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
    implementation(libs.kakaoMap)
    // SplashScreen
    implementation(libs.splashScreen)
    // SwipeRefreshLayout
    implementation(libs.swipeRefreshLayout)
    // Play Services
    implementation(libs.playServicesLocation)
}

hilt {
    enableAggregatingTask = true
}
