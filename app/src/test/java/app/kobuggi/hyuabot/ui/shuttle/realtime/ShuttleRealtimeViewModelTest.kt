package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.service.ShuttlePresenceService
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.RequestLifecycleFixture
import app.kobuggi.hyuabot.ui.common.RequestUiState
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = android.app.Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ShuttleRealtimeViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val liveData = InstantTaskExecutorRule()

    private val stop = ShuttleRealtimePageQuery.Stop(
        "Stop", 37.0, 127.0, "dormitory_o", ShuttleRealtimePageQuery.Timetable("Timetable", emptyList(), emptyList()),
    )

    private fun data(notices: List<ShuttleRealtimePageQuery.ServiceNotice> = emptyList(), stops: List<ShuttleRealtimePageQuery.Stop> = listOf(stop)) =
        ShuttleRealtimePageQuery.Data(
            emptyList(), ShuttleRealtimePageQuery.Shuttle("Shuttle", notices, stops),
            emptyList(), emptyList(), emptyList(), emptyList(),
        )

    private fun viewModel(f: RequestLifecycleFixture) = f.track(
        ShuttleRealtimeViewModel(f.preferences, f.client, ShuttlePresenceService(RuntimeEnvironment.getApplication())),
    )

    @Test fun firstFailureRetryAndSuccessfulEmptyAreDistinct() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = viewModel(f)
            vm.fetchData()
            runCurrent()
            assertEquals(RequestUiState(), vm.requestState.value)
            f.transport.next().fail()
            runCurrent()
            assertEquals(RequestUiState(false, false, true), vm.requestState.value)
            assertFalse(vm.isLoading.value!!)
            vm.fetchData()
            assertEquals(RequestUiState(), vm.requestState.value)
            runCurrent()
            f.transport.next().succeed(data(stops = emptyList()))
            runCurrent()
            assertEquals(RequestUiState(false, true, false), vm.requestState.value)
            assertTrue(vm.result.value!!.isEmpty())
        }
    }

    @Test fun refreshFailureRetainsStopsAndHaltWhileSelectionResetIsNonNull() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = viewModel(f)
            val today = LocalDate.of(2026, 10, 19)
            val notice = ShuttleRealtimePageQuery.ServiceNotice("ServiceNotice", "holiday", today, ShuttleRealtimePageQuery.Holiday("Holiday", "halt"))
            val acceptedHalts = mutableListOf<LocalDate?>()
            val stateObserver = Observer<RequestUiState> { request ->
                if (request.hasSnapshot && !request.loading && !request.failed) acceptedHalts += vm.haltDate(today)
            }
            vm.requestState.observeForever(stateObserver)
            try {
                vm.fetchData()
                runCurrent()
                f.transport.next().succeed(data(listOf(notice)))
                runCurrent()
                assertEquals(listOf(today), acceptedHalts)
            } finally { vm.requestState.removeObserver(stateObserver) }
            assertEquals(today, vm.haltDate(today))
            vm.fetchData()
            runCurrent()
            f.transport.next().throwException()
            runCurrent()
            assertEquals(listOf(stop), vm.result.value)
            assertEquals(today, vm.haltDate(today))
            assertEquals(RequestUiState(false, true, true), vm.requestState.value)
            val emissions = mutableListOf<List<ShuttleRealtimePageQuery.Stop>?>()
            val observer = Observer<List<ShuttleRealtimePageQuery.Stop>> { emissions += it }
            vm.result.observeForever(observer)
            try {
                vm.setShowByDestination(true)
                assertTrue(emissions.last()!!.isEmpty())
                assertFalse(emissions.contains(null))
                assertEquals(RequestUiState(), vm.requestState.value)
                assertNull(vm.haltDate(today))
                runCurrent()
                f.transport.next().succeed(data())
                runCurrent()
                assertEquals(RequestUiState(false, true, false), vm.requestState.value)
            } finally { vm.result.removeObserver(observer) }
        }
    }

    @Test fun oldResponseAndOtherNoticeKindsCannotDeclareTodaysSuspension() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = viewModel(f)
            val today = LocalDate.of(2026, 10, 19)
            vm.fetchData()
            runCurrent()
            val old = f.transport.next()
            vm.setShowByDestination(true)
            runCurrent()
            val wrongNotices = listOf(
                ShuttleRealtimePageQuery.ServiceNotice("Notice", "holiday", today.minusDays(1), ShuttleRealtimePageQuery.Holiday("Holiday", "halt")),
                ShuttleRealtimePageQuery.ServiceNotice("Notice", "holiday", today.plusDays(1), ShuttleRealtimePageQuery.Holiday("Holiday", "halt")),
                ShuttleRealtimePageQuery.ServiceNotice("Notice", "period", today, ShuttleRealtimePageQuery.Holiday("Holiday", "halt")),
                ShuttleRealtimePageQuery.ServiceNotice("Notice", "holiday", today, ShuttleRealtimePageQuery.Holiday("Holiday", "normal")),
            )
            f.transport.next().succeed(data(wrongNotices, emptyList()))
            runCurrent()
            old.succeed(data())
            runCurrent()
            assertTrue(vm.result.value!!.isEmpty())
            assertNull(vm.haltDate(today))
            assertEquals(RequestUiState(false, true, false), vm.requestState.value)
        }
    }
}
