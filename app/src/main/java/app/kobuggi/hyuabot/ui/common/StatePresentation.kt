package app.kobuggi.hyuabot.ui.common

import app.kobuggi.hyuabot.R

data class StateContent(
    val titleRes: Int,
    val bodyRes: Int,
    val actionRes: Int?,
    val iconRes: Int?,
    val analyticsKind: String,
)

object StatePresentation {
    fun forKind(kind: StateKind): StateContent = when (kind) {
        StateKind.EMPTY -> StateContent(R.string.state_empty_title, R.string.state_empty_body, null, null, "empty")
        StateKind.ENDED -> StateContent(R.string.state_ended_title, R.string.state_ended_body, R.string.state_timetable_action, null, "ended")
        StateKind.SUSPENDED -> StateContent(R.string.state_suspended_title, R.string.state_suspended_body, R.string.state_timetable_action, null, "suspended")
        StateKind.ERROR -> StateContent(R.string.state_error_title, R.string.state_error_body, R.string.state_retry_action, R.drawable.ic_state_warning, "error")
        StateKind.PERMISSION_DENIED -> StateContent(R.string.state_permission_title, R.string.state_permission_body, R.string.state_settings_action, R.drawable.ic_state_notifications_off, "permissionDenied")
    }
}

class StateImpressionTracker {
    private var visibleKind: String? = null

    @Synchronized
    fun update(active: Boolean, model: StateModel?): String? {
        if (!active || model == null) {
            visibleKind = null
            return null
        }
        val kind = StatePresentation.forKind(model.kind).analyticsKind
        if (kind == visibleKind) return null
        visibleKind = kind
        return kind
    }
}

data class EmptyStateEvent(val name: String, val kind: String, val sourceScreen: String) {
    fun parameters() = mapOf("kind" to kind, "source_screen" to sourceScreen)
}

fun emptyStateEvent(kind: String, sourceScreen: String) =
    EmptyStateEvent("empty_state_shown", kind, sourceScreen)
