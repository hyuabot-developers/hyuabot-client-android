package app.kobuggi.hyuabot.service.assistant

import app.kobuggi.hyuabot.service.shuttle.ShuttleRepository
import app.kobuggi.hyuabot.service.time.CampusTime
import com.apollographql.cache.normalized.FetchPolicy
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull

sealed interface NextShuttleAnswer {
    data class Departure(
        val stopCode: String,
        val destinationCode: String,
        val departureTime: LocalTime,
        val minutesUntil: Long,
        val followingTime: LocalTime?,
    ) : NextShuttleAnswer

    data class NoMoreService(val stopCode: String, val destinationCode: String?) : NextShuttleAnswer
    data class UnknownStop(val input: String) : NextShuttleAnswer
    data class UnknownDestination(val input: String) : NextShuttleAnswer
    data object NetworkError : NextShuttleAnswer
}

class GetNextShuttleUseCase @Inject constructor(
    private val shuttleRepository: ShuttleRepository,
) {
    suspend operator fun invoke(
        stop: String?,
        destination: String?,
        now: LocalTime = CampusTime.now().toLocalTime(),
    ): NextShuttleAnswer {
        val stopCode = if (stop.isNullOrBlank()) {
            AssistantAliasResolver.DEFAULT_STOP
        } else {
            AssistantAliasResolver.resolveStop(stop) ?: return NextShuttleAnswer.UnknownStop(stop.trim())
        }
        val destinationCode = if (destination.isNullOrBlank()) {
            null
        } else {
            AssistantAliasResolver.resolveDestination(destination)
                ?: return NextShuttleAnswer.UnknownDestination(destination.trim())
        }
        val stops = withTimeoutOrNull(TIMEOUT_MS) {
            shuttleRepository.fetchDepartures(now, FetchPolicy.NetworkFirst)
        }?.getOrNull() ?: return NextShuttleAnswer.NetworkError

        val candidateStops = if (stopCode == "shuttlecock_o" || stopCode == "shuttlecock_i") {
            setOf("shuttlecock_o", "shuttlecock_i")
        } else {
            setOf(stopCode)
        }
        val departures = stops
            .filter { it.name in candidateStops }
            .flatMap { shuttleStop ->
                shuttleStop.timetable.destination
                    .filter { destinationCode == null || it.destination == destinationCode }
                    .flatMap { group ->
                        group.entries.map { entry -> DepartureEntry(shuttleStop.name, group.destination, entry.time) }
                    }
            }
            .sortedBy { CampusTime.minutesUntil(now, it.time) }

        if (departures.isEmpty()) return NextShuttleAnswer.NoMoreService(stopCode, destinationCode)

        val first = departures.first()
        val followingTime = departures.drop(1)
            .firstOrNull { it.destination == first.destination }
            ?.time
        return NextShuttleAnswer.Departure(
            first.stopCode,
            first.destination,
            first.time,
            CampusTime.minutesUntil(now, first.time),
            followingTime,
        )
    }

    private data class DepartureEntry(val stopCode: String, val destination: String, val time: LocalTime)

    companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
