package app.kobuggi.hyuabot.ui.readingRoom

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.RequestLifecycleFixture
import app.kobuggi.hyuabot.ui.common.RequestUiState
import java.time.ZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingRoomViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val liveData = InstantTaskExecutorRule()

    private val room = ReadingRoomPageQuery.ReadingRoom(
        "ReadingRoom", 1, "Library", 2, ReadingRoomPageQuery.Seats("Seats", 10, 20, 10),
        ZonedDateTime.parse("2026-10-19T10:00:00+09:00[Asia/Seoul]"),
    )

    @Test fun initialFailureRetryEmptyAndRecoveryAreDistinct() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = f.track(ReadingRoomViewModel(f.client, f.preferences))
            vm.fetchRooms()
            runCurrent()
            assertEquals(RequestUiState(), vm.requestState.value)
            f.transport.next().fail()
            runCurrent()
            assertEquals(RequestUiState(false, false, true), vm.requestState.value)
            vm.fetchRooms()
            assertEquals(RequestUiState(), vm.requestState.value)
            runCurrent()
            f.transport.next().succeed(ReadingRoomPageQuery.Data(emptyList()))
            runCurrent()
            assertEquals(emptyList<ReadingRoomPageQuery.ReadingRoom>(), vm.rooms.value)
            assertEquals(RequestUiState(false, true, false), vm.requestState.value)
            vm.fetchRooms()
            runCurrent()
            f.transport.next().succeed(ReadingRoomPageQuery.Data(listOf(room)))
            runCurrent()
            assertEquals(listOf(room), vm.rooms.value)
        }
    }

    @Test fun cachedRoomsSurviveFailureAndCampusChangeEmitsNonNullEmpty() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = f.track(ReadingRoomViewModel(f.client, f.preferences))
            vm.fetchRooms()
            runCurrent()
            f.transport.next().succeed(ReadingRoomPageQuery.Data(listOf(room)))
            runCurrent()
            vm.fetchRooms()
            runCurrent()
            f.transport.next().throwException()
            runCurrent()
            assertEquals(listOf(room), vm.rooms.value)
            assertEquals(RequestUiState(false, true, true), vm.requestState.value)
            val emissions = mutableListOf<List<ReadingRoomPageQuery.ReadingRoom>?>()
            val observer = Observer<List<ReadingRoomPageQuery.ReadingRoom>> { emissions += it }
            vm.rooms.observeForever(observer)
            try {
                vm.fetchRooms(1)
                assertEquals(emptyList<ReadingRoomPageQuery.ReadingRoom>(), emissions.last())
                assertFalse(emissions.contains(null))
                assertEquals(RequestUiState(), vm.requestState.value)
                runCurrent()
                f.transport.next().succeed(ReadingRoomPageQuery.Data(listOf(room)))
                runCurrent()
                assertTrue(vm.rooms.value!!.isEmpty())
                assertEquals(RequestUiState(false, true, false), vm.requestState.value)
            } finally { vm.rooms.removeObserver(observer) }
        }
    }

    @Test fun obsoleteCampusResponseCannotReplaceNewerResult() = runTest {
        RequestLifecycleFixture(this).use { f ->
            val vm = f.track(ReadingRoomViewModel(f.client, f.preferences))
            vm.fetchRooms(2)
            runCurrent()
            val old = f.transport.next()
            vm.fetchRooms(1)
            runCurrent()
            f.transport.next().succeed(ReadingRoomPageQuery.Data(emptyList()))
            runCurrent()
            old.succeed(ReadingRoomPageQuery.Data(listOf(room)))
            runCurrent()
            assertTrue(vm.rooms.value!!.isEmpty())
            assertEquals(RequestUiState(false, true, false), vm.requestState.value)
        }
    }
}
