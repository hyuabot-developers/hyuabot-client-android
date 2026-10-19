package app.kobuggi.hyuabot.ui.common

import app.kobuggi.hyuabot.util.TransportMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ListPresentationTest {
    private val now = ZonedDateTime.of(2026, 10, 19, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"))

    @Test fun requestTransitionsRetainOnlyAcceptedSnapshot() {
        assertEquals(RequestUiState(true, false, false), RequestUiState().start(false))
        assertEquals(RequestUiState(false, true, false), RequestUiState().success())
        assertEquals(RequestUiState(false, true, true), RequestUiState(true, true, false).failure())
        assertEquals(RequestUiState(true, true, false), RequestUiState(true, true, true).start(true))
    }

    @Test fun skeletonAnimationHonorsSystemAnimatorScale() {
        assertEquals(true, LoadingPresentation.shouldAnimate(1f))
        assertEquals(true, LoadingPresentation.shouldAnimate(0.5f))
        assertEquals(false, LoadingPresentation.shouldAnimate(0f))
        assertEquals(false, LoadingPresentation.shouldAnimate(Float.NaN))
    }

    @Test fun initialLoadAndRefreshPreserveData() {
        assertEquals(ListPresentation.Loading, resolveListPresentation(RequestUiState(), false, 0, TransportMode.BUS, now))
        assertEquals(ListPresentation.Content, resolveListPresentation(RequestUiState(true, true), true, 2, TransportMode.BUS, now))
        assertEquals(ListPresentation.Content, resolveListPresentation(RequestUiState(false, true, true), true, 2, TransportMode.BUS, now))
    }

    @Test fun absentSourceIsEmptyAndConfiguredZeroDeparturesAreEnded() {
        assertEquals(
            ListPresentation.State(StateModel(StateKind.EMPTY)),
            resolveListPresentation(RequestUiState(false, true), false, 0, TransportMode.BUS, now),
        )
        assertEquals(
            ListPresentation.State(StateModel(StateKind.ENDED)),
            resolveListPresentation(RequestUiState(false, true), true, 0, TransportMode.BUS, now),
        )
        assertEquals(
            ListPresentation.State(StateModel(StateKind.EMPTY)),
            resolveListPresentation(RequestUiState(false, true), true, 0, null, now),
        )
    }

    @Test fun haltPrecedesEmptyAndEndedForShuttle() {
        val date = LocalDate.of(2026, 10, 19)
        assertEquals(
            ListPresentation.State(StateModel(StateKind.SUSPENDED, date)),
            resolveListPresentation(RequestUiState(false, true), false, 0, TransportMode.SHUTTLE, now, date),
        )
    }

    @Test fun requestFailureWithoutSnapshotShowsError() {
        assertEquals(
            ListPresentation.State(StateModel(StateKind.ERROR)),
            resolveListPresentation(RequestUiState(false, false, true), false, 0, TransportMode.SUBWAY, now),
        )
    }

    @Test fun retryBeforeAnySuccessNeverFlashesErrorOrEmpty() {
        val failed = RequestUiState().failure()
        assertEquals(ListPresentation.State(StateModel(StateKind.ERROR)), resolveListPresentation(failed, false, 0, TransportMode.BUS, now))
        assertEquals(ListPresentation.Loading, resolveListPresentation(failed.start(false), false, 0, TransportMode.BUS, now))
        assertEquals(RequestUiState(false, true, false), failed.start(false).success())
    }

    @Test fun emptySnapshotRefreshRetainsEmptyUntilFailureThenShowsError() {
        val accepted = RequestUiState().success()
        assertEquals(ListPresentation.State(StateModel(StateKind.EMPTY)), resolveListPresentation(accepted.start(true), false, 0, TransportMode.BUS, now))
        assertEquals(ListPresentation.State(StateModel(StateKind.ERROR)), resolveListPresentation(accepted.start(true).failure(), false, 0, TransportMode.BUS, now))
        assertEquals(ListPresentation.Loading, resolveListPresentation(accepted.start(false), false, 0, TransportMode.BUS, now))
    }

    @Test fun failureWithoutUsefulSnapshotPrecedesCachedHalt() {
        val today = now.toLocalDate()
        assertEquals(ListPresentation.State(StateModel(StateKind.ERROR)), resolveListPresentation(RequestUiState(false, false, true), false, 0, TransportMode.SHUTTLE, now, today))
        assertEquals(ListPresentation.State(StateModel(StateKind.ERROR)), resolveListPresentation(RequestUiState(false, true, true), false, 0, TransportMode.SHUTTLE, now, today))
        assertEquals(ListPresentation.State(StateModel(StateKind.SUSPENDED, today)), resolveListPresentation(RequestUiState(false, true, true), true, 0, TransportMode.SHUTTLE, now, today))
    }

    @Test fun distinctSectionsResolveIndependentlyAndUseOperationCutoff() {
        val accepted = RequestUiState().success()
        assertEquals(ListPresentation.Content, resolveListPresentation(accepted, true, 2, TransportMode.BUS, now))
        assertEquals(ListPresentation.State(StateModel(StateKind.EMPTY)), resolveListPresentation(accepted, false, 0, TransportMode.BUS, now))
        assertEquals(ListPresentation.State(StateModel(StateKind.ENDED)), resolveListPresentation(accepted, true, 2, TransportMode.BUS, now.withHour(3).withMinute(0)))
        assertEquals(ListPresentation.State(StateModel(StateKind.EMPTY)), resolveListPresentation(accepted, false, 0, TransportMode.BUS, now.withHour(3).withMinute(0)))
    }
}
