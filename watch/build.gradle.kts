import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.apollo)
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
    defaultConfig {
        applicationId = "app.kobuggi.hyuabot"
        minSdk = 33
        targetSdk = 34
        versionCode = 400100003
        versionName = "4.0.0"

    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
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

    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.splashScreen)
    implementation(libs.androidx.tiles)
    implementation(libs.androidx.tiles.material)
    implementation(libs.androidx.tiles.tooling.preview)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.androidx.watchface.complications.data.source.ktx)
    // SplashScreen
    implementation(libs.splashScreen)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Material
    implementation(libs.materialCompose)
    // Networking
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.apollo)
    // RXJava
    implementation(libs.rxJava)
    implementation(libs.rxAndroid)
    implementation(libs.androidx.runtime.livedata)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.tooling)
}
