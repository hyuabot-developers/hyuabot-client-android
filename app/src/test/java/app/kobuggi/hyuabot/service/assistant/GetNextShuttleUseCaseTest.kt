package app.kobuggi.hyuabot.service.assistant

import app.kobuggi.hyuabot.ShuttleLocationQuery
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import app.kobuggi.hyuabot.service.shuttle.ShuttleRepository
import com.apollographql.cache.normalized.FetchPolicy
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetNextShuttleUseCaseTest {
    private val now = LocalTime.of(10, 0)

    @Test
    fun defaultsToDormitoryAndFindsEarliestWithFollowingSameDestination() = runTest {
        val useCase = GetNextShuttleUseCase(
            FakeShuttleRepository(Result.success(listOf(
                stop("dormitory_o", destination("STATION", 10, 11), destination("TERMINAL", 10, 12)),
            ))),
        )

        assertEquals(
            NextShuttleAnswer.Departure("dormitory_o", "STATION", LocalTime.of(10, 0), 0, LocalTime.of(11, 0)),
            useCase(null, null, now),
        )
    }

    @Test
    fun resolvesStopAndDestinationAliases() = runTest {
        val useCase = GetNextShuttleUseCase(
            FakeShuttleRepository(Result.success(listOf(
                stop("station", destination("TERMINAL", 10), destination("STATION", 10)),
            ))),
        )

        assertEquals(
            NextShuttleAnswer.Departure("station", "TERMINAL", LocalTime.of(10, 0), 0, null),
            useCase("한대앞", "예술인행", now),
        )
    }

    @Test
    fun searchesBothShuttlecockStops() = runTest {
        val useCase = GetNextShuttleUseCase(
            FakeShuttleRepository(Result.success(listOf(
                stop("shuttlecock_o", destination("STATION", 10)),
                stop("shuttlecock_i", destination("CAMPUS", 10)),
            ))),
        )

        assertEquals(
            NextShuttleAnswer.Departure("shuttlecock_i", "CAMPUS", LocalTime.of(10, 0), 0, null),
            useCase("셔틀콕", "기숙사", now),
        )
    }

    @Test
    fun emptyStopScheduleReturnsNoMoreService() = runTest {
        val useCase = GetNextShuttleUseCase(FakeShuttleRepository(Result.success(listOf(stop("dormitory_o")))))

        assertEquals(NextShuttleAnswer.NoMoreService("dormitory_o", null), useCase(null, null, now))
    }

    @Test
    fun unmatchedDestinationReturnsNoMoreService() = runTest {
        val useCase = GetNextShuttleUseCase(
            FakeShuttleRepository(Result.success(listOf(stop("station", destination("STATION", 10))))),
        )

        assertEquals(NextShuttleAnswer.NoMoreService("station", "JUNGANG"), useCase("한대앞", "중앙역", now))
    }

    @Test
    fun unknownStopDoesNotCallRepository() = runTest {
        val repository = FakeShuttleRepository(Result.success(emptyList()))

        assertEquals(NextShuttleAnswer.UnknownStop("없는곳"), GetNextShuttleUseCase(repository)("없는곳", null, now))
        assertEquals(0, repository.departureCalls)
    }

    @Test
    fun unknownDestinationIsReturned() = runTest {
        val useCase = GetNextShuttleUseCase(FakeShuttleRepository(Result.success(emptyList())))

        assertEquals(NextShuttleAnswer.UnknownDestination("부산"), useCase(null, "부산", now))
    }

    @Test
    fun repositoryFailureReturnsNetworkError() = runTest {
        val useCase = GetNextShuttleUseCase(FakeShuttleRepository(Result.failure(IllegalStateException())))

        assertEquals(NextShuttleAnswer.NetworkError, useCase(null, null, now))
    }

    @Test
    fun repositoryTimeoutReturnsNetworkError() = runTest {
        val useCase = GetNextShuttleUseCase(FakeShuttleRepository(Result.success(emptyList()), delayMs = 20_000))

        assertEquals(NextShuttleAnswer.NetworkError, useCase(null, null, now))
    }

    private fun stop(name: String, vararg destinations: ShuttleWidgetQuery.Destination) =
        ShuttleWidgetQuery.Stop(
            __typename = "ShuttleStop",
            latitude = 0.0,
            longitude = 0.0,
            name = name,
            timetable = ShuttleWidgetQuery.Timetable(
                __typename = "ShuttleTimetable",
                destination = destinations.toList(),
            ),
        )

    private fun destination(code: String, vararg hours: Int) = ShuttleWidgetQuery.Destination(
        __typename = "ShuttleDestination",
        destination = code,
        entries = hours.map {
            ShuttleWidgetQuery.Entry(__typename = "ShuttleTimetableEntry", time = LocalTime.of(it, 0))
        },
    )

    private class FakeShuttleRepository(
        private val result: Result<List<ShuttleWidgetQuery.Stop>>,
        private val delayMs: Long = 0,
    ) : ShuttleRepository {
        var departureCalls = 0
            private set

        override suspend fun fetchDepartures(
            after: LocalTime,
            fetchPolicy: FetchPolicy,
        ): Result<List<ShuttleWidgetQuery.Stop>> {
            departureCalls++
            if (delayMs > 0) delay(delayMs)
            return result
        }

        override suspend fun fetchRealtimePage(
            query: ShuttleRealtimePageQuery,
            fetchPolicy: FetchPolicy,
        ): Result<ShuttleRealtimePageQuery.Data> = error("unused")

        override suspend fun fetchLocations(fetchPolicy: FetchPolicy): Result<ShuttleLocationQuery.Shuttle> =
            error("unused")
    }
}
