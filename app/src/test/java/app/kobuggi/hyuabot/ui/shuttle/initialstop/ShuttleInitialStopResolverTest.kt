package app.kobuggi.hyuabot.ui.shuttle.initialstop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShuttleInitialStopResolverTest {
    @Test
    fun `returns highest-priority matching rule`() {
        val rules =
            listOf(
                rule(sequence = 2, stopName = "station", priority = 10),
                rule(sequence = 1, stopName = "dormitory_o", priority = 20),
            )

        assertEquals(
            "dormitory_o",
            ShuttleInitialStopResolver.resolve(latitude = 37.5, longitude = 126.5, rules = rules),
        )
    }

    @Test
    fun `uses sequence as tie breaker`() {
        val rules =
            listOf(
                rule(sequence = 2, stopName = "station", priority = 10),
                rule(sequence = 1, stopName = "terminal", priority = 10),
            )

        assertEquals(
            "terminal",
            ShuttleInitialStopResolver.resolve(latitude = 37.5, longitude = 126.5, rules = rules),
        )
    }

    @Test
    fun `treats polygon boundary as inside`() {
        assertTrue(
            ShuttleInitialStopResolver.contains(
                ShuttleGeoCoordinate(latitude = 37.0, longitude = 126.5),
                square(),
            ),
        )
    }

    @Test
    fun `returns null outside every rule or for invalid input`() {
        assertNull(
            ShuttleInitialStopResolver.resolve(
                latitude = 38.0,
                longitude = 128.0,
                rules = listOf(rule(sequence = 1, stopName = "station", priority = 10)),
            ),
        )
        assertNull(
            ShuttleInitialStopResolver.resolve(
                latitude = Double.NaN,
                longitude = 126.5,
                rules = listOf(rule(sequence = 1, stopName = "station", priority = 10)),
            ),
        )
        assertNull(
            ShuttleInitialStopResolver.resolve(
                latitude = 37.5,
                longitude = 126.5,
                rules =
                    listOf(
                        ShuttleInitialStopRuleCandidate(
                            sequence = 1,
                            stopName = "station",
                            priority = 10,
                            polygon = square().take(2),
                        ),
                    ),
            ),
        )
    }

    private fun rule(
        sequence: Int,
        stopName: String,
        priority: Int,
    ) = ShuttleInitialStopRuleCandidate(
        sequence = sequence,
        stopName = stopName,
        priority = priority,
        polygon = square(),
    )

    private fun square() =
        listOf(
            ShuttleGeoCoordinate(latitude = 37.0, longitude = 126.0),
            ShuttleGeoCoordinate(latitude = 37.0, longitude = 127.0),
            ShuttleGeoCoordinate(latitude = 38.0, longitude = 127.0),
            ShuttleGeoCoordinate(latitude = 38.0, longitude = 126.0),
        )
}
