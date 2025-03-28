import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.kotlinKsp) apply false
    alias(libs.plugins.hiltPlugin) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.secretsGradlePlugin) apply false
    alias(libs.plugins.crashlyticsPlugin) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.apollo) apply false
}

allprojects {}

apply(plugin = "org.jlleitschuh.gradle.ktlint")
configure<KtlintExtension> {
    debug.set(true)
    reporters {
        reporter(ReporterType.JSON)
    }
    filter {
        exclude { element -> element.file.name.contains("generated/") }
    }
}
