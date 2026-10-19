package app.kobuggi.hyuabot.ui.common

import app.kobuggi.hyuabot.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatePresentationTest {
    @Test fun everyStateHasStableCopyActionIconAndAnalyticsKind() {
        assertEquals(StateContent(R.string.state_empty_title, R.string.state_empty_body, null, null, "empty"), StatePresentation.forKind(StateKind.EMPTY))
        assertEquals(StateContent(R.string.state_ended_title, R.string.state_ended_body, R.string.state_timetable_action, null, "ended"), StatePresentation.forKind(StateKind.ENDED))
        assertEquals(StateContent(R.string.state_suspended_title, R.string.state_suspended_body, R.string.state_timetable_action, null, "suspended"), StatePresentation.forKind(StateKind.SUSPENDED))
        assertEquals(StateContent(R.string.state_error_title, R.string.state_error_body, R.string.state_retry_action, R.drawable.ic_state_warning, "error"), StatePresentation.forKind(StateKind.ERROR))
        assertEquals(StateContent(R.string.state_permission_title, R.string.state_permission_body, R.string.state_settings_action, R.drawable.ic_state_notifications_off, "permissionDenied"), StatePresentation.forKind(StateKind.PERMISSION_DENIED))
    }

    @Test fun impressionsOnlyFireWhenVisibleKindChanges() {
        val model = StateModel(StateKind.EMPTY)
        val tracker = StateImpressionTracker()
        assertNull(tracker.update(false, model))
        assertEquals("empty", tracker.update(true, model))
        assertNull(tracker.update(true, model))
        assertEquals("ended", tracker.update(true, StateModel(StateKind.ENDED)))
        assertNull(tracker.update(true, StateModel(StateKind.ENDED)))
        assertNull(tracker.update(false, null))
        assertEquals("empty", tracker.update(true, model))
    }

    @Test fun analyticsPayloadContainsOnlyKindAndSourceScreen() {
        val event = emptyStateEvent("ended", "bus_realtime")
        assertEquals("empty_state_shown", event.name)
        assertEquals(mapOf("kind" to "ended", "source_screen" to "bus_realtime"), event.parameters())
    }
}
