package app.kobuggi.hyuabot.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.kobuggi.hyuabot.HomePageQuery
import app.kobuggi.hyuabot.service.ShuttlePresenceService
import app.kobuggi.hyuabot.service.alarm.ShuttleServiceNoticeScheduler
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.RequestLifecycleFixture
import app.kobuggi.hyuabot.ui.common.RequestUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = android.app.Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val liveData = InstantTaskExecutorRule()

    private val response = HomePageQuery.Data(
        null, emptyList(), HomePageQuery.Shuttle("Shuttle", emptyList(), emptyList()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
    )

    @Test fun acceptedTimeAndCardsSurviveRefreshFailureAndClearOnSelectionChange() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val context = RuntimeEnvironment.getApplication()
            val vm = f.track(HomeViewModel(f.client, f.preferences, ShuttleServiceNoticeScheduler(context, f.client), ShuttlePresenceService(context)))
            vm.fetchData()
            runCurrent()
            f.transport.next().succeed(response)
            runCurrent()
            val acceptedAt = vm.lastAcceptedAt.value
            assertNotNull(acceptedAt)
            assertSame(response, vm.data.value)
            vm.fetchData()
            runCurrent()
            f.transport.next().fail()
            runCurrent()
            assertEquals(RequestUiState(false, true, true), vm.requestState.value)
            assertSame(response, vm.data.value)
            assertEquals(acceptedAt, vm.lastAcceptedAt.value)
            vm.fetchData()
            runCurrent()
            f.transport.next().succeed(response)
            runCurrent()
            assertFalse(vm.requestState.value!!.failed)
            vm.setRequestSelection(HomeRequestSelection(stop = "station", destination = "CAMPUS"))
            runCurrent()
            assertNull(vm.data.value)
            assertNull(vm.lastAcceptedAt.value)
            assertEquals(RequestUiState(), vm.requestState.value)
            f.transport.next().succeed(response)
            runCurrent()
            assertNotNull(vm.lastAcceptedAt.value)
            assertEquals(RequestUiState(false, true, false), vm.requestState.value)
        }
    }
}
