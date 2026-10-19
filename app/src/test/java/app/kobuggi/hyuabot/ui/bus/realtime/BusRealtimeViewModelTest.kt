package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.RequestLifecycleFixture
import app.kobuggi.hyuabot.ui.common.RequestUiState
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
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = android.app.Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class BusRealtimeViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val liveData = InstantTaskExecutorRule()

    private val bus = BusRealtimePageQuery.Bus(
        "Bus", BusRealtimePageQuery.Route("Route", 216000068, "10-1"),
        BusRealtimePageQuery.Stop("Stop", 216000379, 37.0, 127.0), 1,
        emptyList(), emptyList(), emptyList(), emptyList(),
    )

    @Test fun firstFailureRetryAndSuccessfulEmptyCompleteLoading() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = f.track(BusRealtimeViewModel(f.client, f.preferences))
            vm.fetchData()
            assertEquals(RequestUiState(), vm.requestState.value)
            runCurrent()
            f.transport.next().fail()
            runCurrent()
            assertEquals(RequestUiState(false, false, true), vm.requestState.value)
            assertFalse(vm.isLoading.value!!)
            vm.fetchData()
            assertEquals(RequestUiState(), vm.requestState.value)
            runCurrent()
            f.transport.next().succeed(BusRealtimePageQuery.Data(emptyList(), emptyList()))
            runCurrent()
            assertEquals(RequestUiState(false, true, false), vm.requestState.value)
            assertTrue(vm.result.value!!.isEmpty())
            assertNull(vm.queryError.value)
        }
    }

    @Test fun refreshFailureRetainsSnapshotAndChangedStopResetsWithoutNull() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = f.track(BusRealtimeViewModel(f.client, f.preferences))
            vm.fetchData()
            runCurrent()
            f.transport.next().succeed(BusRealtimePageQuery.Data(emptyList(), listOf(bus)))
            runCurrent()
            vm.fetchData()
            assertEquals(RequestUiState(true, true, false), vm.requestState.value)
            runCurrent()
            f.transport.next().throwException()
            runCurrent()
            assertEquals(listOf(bus), vm.result.value)
            assertEquals(RequestUiState(false, true, true), vm.requestState.value)
            val emissions = mutableListOf<List<BusRealtimePageQuery.Bus>?>()
            val observer = Observer<List<BusRealtimePageQuery.Bus>> { emissions += it }
            vm.result.observeForever(observer)
            try {
                vm.setSelectedStopID(R.string.bus_stop_cluster)
                vm.fetchData()
                assertEquals(RequestUiState(), vm.requestState.value)
                assertTrue(emissions.last()!!.isEmpty())
                assertFalse(emissions.contains(null))
                runCurrent()
                f.transport.next().succeed(BusRealtimePageQuery.Data(emptyList(), emptyList()))
                runCurrent()
                assertEquals(RequestUiState(false, true, false), vm.requestState.value)
            } finally { vm.result.removeObserver(observer) }
        }
    }

    @Test fun olderResponseCannotReplaceNewSelection() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = f.track(BusRealtimeViewModel(f.client, f.preferences))
            vm.fetchData()
            runCurrent()
            val old = f.transport.next()
            vm.setSelectedStopID(R.string.bus_stop_cluster)
            vm.fetchData()
            runCurrent()
            f.transport.next().succeed(BusRealtimePageQuery.Data(emptyList(), emptyList()))
            runCurrent()
            old.succeed(BusRealtimePageQuery.Data(emptyList(), listOf(bus)))
            runCurrent()
            assertTrue(vm.result.value!!.isEmpty())
            assertEquals(RequestUiState(false, true, false), vm.requestState.value)
        }
    }
}
