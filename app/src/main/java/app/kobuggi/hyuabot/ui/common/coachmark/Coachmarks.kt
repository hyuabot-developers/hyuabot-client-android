package app.kobuggi.hyuabot.ui.common.coachmark

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

object Coachmarks {
    const val SHUTTLE = "shuttle"
    const val SHUTTLE_REALTIME_UPDATES = "shuttle_realtime_updates"
    const val SHUTTLE_TIMETABLE = "shuttle_timetable"
    const val SHUTTLE_BUS_ALTERNATIVE = "shuttle_bus_alternative"
    const val BUS = "bus"
    const val SUBWAY = "subway"
    const val CAFETERIA = "cafeteria"
    const val MENU = "menu"
    const val READING_ROOM = "reading_room"
    const val MAP = "map"
    const val SETTING = "setting"
    const val HOME = "home"

    val USER_VISIBLE_KEYS = setOf(
        SHUTTLE,
        SHUTTLE_REALTIME_UPDATES,
        SHUTTLE_TIMETABLE,
        SHUTTLE_BUS_ALTERNATIVE,
        BUS,
        SUBWAY,
        CAFETERIA,
        MENU,
        READING_ROOM,
        MAP,
        SETTING,
        HOME,
    )
}

suspend fun android.content.Context.ensureCoachmarkEligibility(repository: UserPreferencesRepository): Boolean {
    return repository.initializeCoachmarks()
}

fun Fragment.showCoachmarkOnce(
    repository: UserPreferencesRepository,
    key: String,
    onStepShown: (Int) -> Unit = {},
    onSkip: (Int) -> Unit = {},
    onComplete: (Int) -> Unit = {},
    onDismiss: () -> Unit = {},
    progress: CoachmarkProgress? = null,
    steps: () -> List<CoachmarkStep>,
) {
    val owner = viewLifecycleOwner
    owner.lifecycleScope.launch {
        try {
            requireContext().ensureCoachmarkEligibility(repository)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return@launch
        }
        if (repository.coachmarkSeen(key).first()) return@launch
        val rootView = view ?: return@launch
        rootView.post {
            if (!isAdded || view == null || !owner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                return@post
            }
            val host = activity ?: return@post
            host.lifecycleScope.launch {
                if (host is app.kobuggi.hyuabot.ui.MainActivity && !host.claimStartupCoachmark(key)) return@launch
                CoachmarkController.show(host, steps(), owner, onFinish = {
                    host.lifecycleScope.launch { repository.markCoachmarkSeen(key) }
                }, onStepShown = onStepShown, onSkip = onSkip, onComplete = onComplete, onDismiss = {
                    (host as? app.kobuggi.hyuabot.ui.MainActivity)?.releaseStartupCoachmarkClaim()
                    onDismiss()
                }, progress = progress)
            }
        }
    }
}
