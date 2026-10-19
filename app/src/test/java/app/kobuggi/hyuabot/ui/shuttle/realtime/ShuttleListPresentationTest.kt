package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.app.Activity
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentShuttleRealtimeBinding
import app.kobuggi.hyuabot.ui.common.FreshnessLabel
import app.kobuggi.hyuabot.ui.common.ListPresentation
import app.kobuggi.hyuabot.ui.common.RequestUiState
import app.kobuggi.hyuabot.ui.common.StateKind
import app.kobuggi.hyuabot.ui.common.StateModel
import app.kobuggi.hyuabot.ui.common.resolveListPresentation
import app.kobuggi.hyuabot.util.ServiceNoticeInput
import app.kobuggi.hyuabot.util.TransportMode
import app.kobuggi.hyuabot.util.haltDateFor
import app.kobuggi.hyuabot.util.setSkeletonLoading
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = android.app.Application::class)
class ShuttleListPresentationTest {
    private val now = ZonedDateTime.of(2026, 10, 19, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"))

    @Test fun missingStopAndStopWithoutDeparturesAreDifferentStates() {
        val accepted = RequestUiState().success()
        assertEquals(ListPresentation.State(StateModel(StateKind.EMPTY)), resolveListPresentation(accepted, false, 0, TransportMode.SHUTTLE, now))
        assertEquals(ListPresentation.State(StateModel(StateKind.ENDED)), resolveListPresentation(accepted, true, 0, TransportMode.SHUTTLE, now))
    }

    @Test fun onlyTodaysOperatorHaltSuspendsAndExpiresAtKstMidnight() {
        val today = now.toLocalDate()
        for (notice in listOf(
            ServiceNoticeInput("period", today, "halt"), ServiceNoticeInput("holiday", today, "normal"),
            ServiceNoticeInput("holiday", today.minusDays(1), "halt"), ServiceNoticeInput("holiday", today.plusDays(1), "halt"),
        )) assertNull(haltDateFor(listOf(notice), today))
        val notices = listOf(ServiceNoticeInput("holiday", today, "halt"))
        val halt = haltDateFor(notices, today)
        assertEquals(ListPresentation.State(StateModel(StateKind.SUSPENDED, today)), resolveListPresentation(RequestUiState().success(), false, 0, TransportMode.SHUTTLE, now, halt))
        assertNull(haltDateFor(notices, now.plusDays(1).withHour(0).toLocalDate()))
    }

    @Test fun stateContainersStayOutsideLoadingContent() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val context = ContextThemeWrapper(controller.get(), R.style.Theme_HYUabot)
            val binding = FragmentShuttleRealtimeBinding.inflate(LayoutInflater.from(context))
            controller.get().setContentView(binding.root)
            val requests = listOf(RequestUiState(), RequestUiState().failure(), RequestUiState().success(), RequestUiState(false, true, true))
            for (request in requests) {
                binding.loadingLayout.setSkeletonLoading(request.loading && !request.hasSnapshot)
                binding.viewPager.visibility = if (request.loading && !request.hasSnapshot) View.GONE else View.VISIBLE
            }
        } finally { controller.pause().stop().destroy() }
    }
}
