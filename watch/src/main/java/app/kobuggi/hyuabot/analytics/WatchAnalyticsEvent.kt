package app.kobuggi.hyuabot.analytics

internal data class WatchAnalyticsEvent(
    val event: String,
    val platform: String,
    val installationId: String,
    val appVersion: String,
    val entryPoint: String,
    val stopId: String? = null,
) {
    companion object {
        fun appOpen(
            installationId: String,
            appVersion: String,
            entryPoint: String,
        ) = WatchAnalyticsEvent(
            event = "watch_app_open",
            platform = "wear_os",
            installationId = installationId,
            appVersion = appVersion,
            entryPoint = entryPoint,
        )

        fun stopSelected(
            installationId: String,
            appVersion: String,
            entryPoint: String,
            stopId: String,
        ) = WatchAnalyticsEvent(
            event = "watch_stop_selected",
            platform = "wear_os",
            installationId = installationId,
            appVersion = appVersion,
            entryPoint = entryPoint,
            stopId = stopId,
        )
    }
}
