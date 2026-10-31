package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleLocationQuery
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import app.kobuggi.hyuabot.service.ShuttlePresenceService
import app.kobuggi.hyuabot.service.shuttle.ShuttleRepository
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.UserPreferencesRepositoryTestFactory
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.cache.normalized.FetchPolicy
import java.io.IOException
import java.time.LocalTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class ShuttleRealtimeViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositoryFactory: UserPreferencesRepositoryTestFactory

    @After
    fun tearDown() {
        if (this::repositoryFactory.isInitialized) repositoryFactory.cleanUp()
    }

    @Test
    fun fetchDataUsesOneNetworkOnlyRealtimeQueryAndAppliesSuccess() = runTest {
        val repository = FakeShuttleRepository()
        val data = realtimeData(7)
        repository.responses.add(CompletableDeferred(Result.success(data)))
        val viewModel = createViewModel(this, repository)
        advanceUntilIdle()

        viewModel.fetchData()
        advanceUntilIdle()

        assertEquals(1, repository.realtimeCalls.size)
        assertEquals(FetchPolicy.NetworkOnly, repository.realtimeCalls.single().second)
        assertEquals(
            listOf("dormitory_o", "shuttlecock_o", "station", "terminal", "jungang_stn", "shuttlecock_i"),
            repository.realtimeCalls.single().first.shuttleStops.map { it.name },
        )
        assertEquals(12, repository.realtimeCalls.single().first.alternativeInput.size)
        assertEquals(0, repository.departuresCalls)
        assertEquals(emptyList<ShuttleRealtimePageQuery.Stop>(), viewModel.result.value)
        assertSame(data, viewModel.transfer.value)
        assertNull(viewModel.queryError.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(
            BusAlternativeData(R.string.shuttle_bus_alternative_route, 7, "한대앞역", 37.31, 126.85),
            viewModel.busAlternativeStation.value,
        )
    }

    @Test
    fun fetchDataReportsRepositoryFailure() = runTest {
        val repository = FakeShuttleRepository()
        repository.responses.add(CompletableDeferred(Result.failure(IOException())))
        val viewModel = createViewModel(this, repository)
        advanceUntilIdle()

        viewModel.fetchData()
        advanceUntilIdle()

        assertEquals(QueryError.SERVER_ERROR, viewModel.queryError.value)
        assertNull(viewModel.result.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun staleResponseDoesNotReplaceNewerResponse() = runTest {
        val repository = FakeShuttleRepository()
        val first = CompletableDeferred<Result<ShuttleRealtimePageQuery.Data>>()
        val second = CompletableDeferred<Result<ShuttleRealtimePageQuery.Data>>()
        repository.responses.add(first)
        repository.responses.add(second)
        val viewModel = createViewModel(this, repository)
        advanceUntilIdle()

        viewModel.fetchData()
        viewModel.fetchData()
        val secondData = realtimeData(3)
        second.complete(Result.success(secondData))
        advanceUntilIdle()
        first.complete(Result.success(realtimeData(9)))
        advanceUntilIdle()

        assertEquals(3, viewModel.busAlternativeStation.value?.minutes)
        assertSame(secondData, viewModel.transfer.value)
    }

    private fun createViewModel(
        scope: TestScope,
        repository: FakeShuttleRepository,
    ): ShuttleRealtimeViewModel {
        repositoryFactory = UserPreferencesRepositoryTestFactory(scope)
        return ShuttleRealtimeViewModel(
            repositoryFactory.create(),
            repository,
            ShuttlePresenceService(ApplicationProvider.getApplicationContext()),
        )
    }

    private fun realtimeData(stationMinutes: Int): ShuttleRealtimePageQuery.Data =
        ShuttleRealtimePageQuery.Data(
            notices = emptyList(),
            shuttle = ShuttleRealtimePageQuery.Shuttle(__typename = "Shuttle", stops = emptyList()),
            subway = emptyList(),
            subwayTimetable = emptyList(),
            transferBus = emptyList(),
            busAlternative = listOf(
                ShuttleRealtimePageQuery.BusAlternative(
                    __typename = "BusRouteStop",
                    route = ShuttleRealtimePageQuery.Route2(__typename = "BusRoute", seq = 216000068),
                    stop = ShuttleRealtimePageQuery.Stop4(
                        __typename = "BusStop",
                        seq = 216000138,
                        name = "한대앞역",
                        latitude = 37.31,
                        longitude = 126.85,
                    ),
                    arrival = listOf(
                        ShuttleRealtimePageQuery.Arrival2(
                            __typename = "Arrival",
                            stops = null,
                            seats = null,
                            minutes = stationMinutes,
                            lowFloor = null,
                            isRealtime = true,
                            time = null,
                        ),
                    ),
                ),
            ),
        )

    private class FakeShuttleRepository : ShuttleRepository {
        val realtimeCalls = mutableListOf<Pair<ShuttleRealtimePageQuery, FetchPolicy>>()
        val responses = ArrayDeque<CompletableDeferred<Result<ShuttleRealtimePageQuery.Data>>>()
        var departuresCalls = 0

        override suspend fun fetchRealtimePage(
            query: ShuttleRealtimePageQuery,
            fetchPolicy: FetchPolicy,
        ): Result<ShuttleRealtimePageQuery.Data> {
            realtimeCalls += query to fetchPolicy
            return responses.removeFirst().await()
        }

        override suspend fun fetchDepartures(
            after: LocalTime,
            fetchPolicy: FetchPolicy,
        ): Result<List<ShuttleWidgetQuery.Stop>> {
            departuresCalls++
            return Result.failure(AssertionError("widget query must not be used"))
        }

        override suspend fun fetchLocations(
            fetchPolicy: FetchPolicy,
        ): Result<ShuttleLocationQuery.Shuttle> =
            Result.failure(IllegalStateException("offline"))
    }
}
