import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinKsp) apply false
    alias(libs.plugins.hiltPlugin) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.secretsGradlePlugin) apply false
    alias(libs.plugins.googleServicesPlugin) apply false
    alias(libs.plugins.crashlyticsPlugin) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.composeCompiler) apply false
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
