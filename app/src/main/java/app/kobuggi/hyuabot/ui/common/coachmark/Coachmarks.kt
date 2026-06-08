package app.kobuggi.hyuabot.ui.common.coachmark

import android.content.Context
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository

object Coachmarks {
    const val SHUTTLE = "shuttle"
    const val SHUTTLE_TIMETABLE = "shuttle_timetable"

    val EXISTING_FEATURE_KEYS = setOf(
        SHUTTLE,
        SHUTTLE_TIMETABLE,
    )
}

suspend fun Context.ensureCoachmarkBaseline(repository: UserPreferencesRepository) {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val isFreshInstall = packageInfo.firstInstallTime == packageInfo.lastUpdateTime
    repository.initCoachmarkBaselineIfNeeded(isFreshInstall, Coachmarks.EXISTING_FEATURE_KEYS)
}
