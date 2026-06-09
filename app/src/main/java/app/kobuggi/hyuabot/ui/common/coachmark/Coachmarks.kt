package app.kobuggi.hyuabot.ui.common.coachmark

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Coachmarks {
    const val SHUTTLE = "shuttle"
    const val SHUTTLE_TIMETABLE = "shuttle_timetable"
    const val BUS = "bus"
    const val SUBWAY = "subway"
    const val CAFETERIA = "cafeteria"
    const val MENU = "menu"
    const val READING_ROOM = "reading_room"
    const val MAP = "map"
    const val SETTING = "setting"

    val EXISTING_FEATURE_KEYS = setOf(
        SHUTTLE,
        SHUTTLE_TIMETABLE,
        BUS,
        SUBWAY,
        CAFETERIA,
        MENU,
        READING_ROOM,
        MAP,
        SETTING,
    )
}

suspend fun Context.ensureCoachmarkBaseline(repository: UserPreferencesRepository) {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val isFreshInstall = packageInfo.firstInstallTime == packageInfo.lastUpdateTime
    repository.initCoachmarkBaselineIfNeeded(isFreshInstall, Coachmarks.EXISTING_FEATURE_KEYS)
}

fun Fragment.showCoachmarkOnce(
    repository: UserPreferencesRepository,
    key: String,
    steps: () -> List<CoachmarkStep>,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        requireContext().ensureCoachmarkBaseline(repository)
        if (repository.coachmarkSeen(key).first()) return@launch
        view?.post {
            if (!isAdded) return@post
            CoachmarkController.show(requireActivity(), steps()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.markCoachmarkSeen(key)
                }
            }
        }
    }
}
