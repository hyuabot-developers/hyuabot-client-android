package app.kobuggi.hyuabot.ui.common

import app.kobuggi.hyuabot.util.ListStatus

object LoadingPresentation {
    fun shouldAnimate(animatorDurationScale: Float): Boolean = animatorDurationScale > 0f
}

/** Request lifecycle for one accepted selection. */
data class RequestUiState(
    val loading: Boolean = true,
    val hasSnapshot: Boolean = false,
    val failed: Boolean = false,
) {
    fun start(retainSnapshot: Boolean): RequestUiState =
        RequestUiState(loading = true, hasSnapshot = hasSnapshot && retainSnapshot, failed = false)

    fun success(): RequestUiState = RequestUiState(loading = false, hasSnapshot = true, failed = false)

    fun failure(): RequestUiState = RequestUiState(loading = false, hasSnapshot = hasSnapshot, failed = true)
}

data class StateModel(val kind: StateKind, val noticeDate: java.time.LocalDate? = null)

enum class StateKind { EMPTY, ENDED, SUSPENDED, ERROR, PERMISSION_DENIED }

sealed interface ListPresentation {
    data object Loading : ListPresentation
    data object Content : ListPresentation
    data class State(val model: StateModel) : ListPresentation
}

/** Resolves transport states consistently while preserving visible content during refreshes. */
fun resolveListPresentation(
    request: RequestUiState,
    hasSourceData: Boolean,
    remainingCount: Int,
    mode: app.kobuggi.hyuabot.util.TransportMode?,
    now: java.time.ZonedDateTime,
    haltDate: java.time.LocalDate? = null,
): ListPresentation {
    if (request.loading && !request.hasSnapshot) return ListPresentation.Loading
    if (request.failed && (!request.hasSnapshot || !hasSourceData)) return ListPresentation.State(StateModel(StateKind.ERROR))
    if (!request.hasSnapshot) return ListPresentation.State(StateModel(StateKind.ERROR))
    if (mode == null) {
        return if (hasSourceData && remainingCount > 0) ListPresentation.Content
        else ListPresentation.State(StateModel(StateKind.EMPTY))
    }
    val status = app.kobuggi.hyuabot.util.judgeList(
        mode,
        now,
        hasError = false,
        haltDate = if (mode == app.kobuggi.hyuabot.util.TransportMode.SHUTTLE) haltDate else null,
        remainingCount = if (hasSourceData) remainingCount else 1,
    )
    return when {
        status is ListStatus.Halt -> ListPresentation.State(StateModel(StateKind.SUSPENDED, status.noticeDate))
        !hasSourceData -> ListPresentation.State(StateModel(StateKind.EMPTY))
        status is ListStatus.Error -> ListPresentation.State(StateModel(StateKind.ERROR))
        status is ListStatus.Ended -> ListPresentation.State(StateModel(StateKind.ENDED))
        remainingCount == 0 -> ListPresentation.State(StateModel(StateKind.ENDED))
        else -> ListPresentation.Content
    }
}
