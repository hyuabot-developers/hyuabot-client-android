package app.kobuggi.hyuabot.ui.common.coachmark

import android.content.Context
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository

object Coachmarks {
    const val SHUTTLE = "shuttle"

    val EXISTING_FEATURE_KEYS = setOf(
        SHUTTLE,
    )
}

suspend fun Context.ensureCoachmarkBaseline(repository: UserPreferencesRepository) {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val isFreshInstall = packageInfo.firstInstallTime == packageInfo.lastUpdateTime
    repository.initCoachmarkBaselineIfNeeded(isFreshInstall, Coachmarks.EXISTING_FEATURE_KEYS)
}
