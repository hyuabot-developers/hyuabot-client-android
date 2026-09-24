package app.kobuggi.hyuabot.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeBusInputTest {
    @Test
    fun `arrival selection excludes unrelated location probes and alternatives`() {
        assertEquals(
            setOf(216000061 to 216000379, 216000096 to 216000719),
            HomeViewModel.homeBusPairsForTest(HomeBusGroup.CAMPUS, BusHomeDestination.GANGNAM),
        )
        assertEquals(emptySet<Pair<Int, Int>>(), HomeViewModel.homeBusPairsForTest(null, BusHomeDestination.GANGNAM))
        HomeBusGroup.entries.forEach { group ->
            BusHomeDestination.entries.forEach { destination ->
                val pairs = HomeViewModel.homeBusPairsForTest(group, destination)
                assertTrue(pairs.size in 1..4)
                if (group.isSeoul) assertTrue(pairs.all { it.second == group.stopSeq })
            }
        }
    }

    @Test
    fun `destination stop IDs match the selected route and stop`() {
        assertEquals(
            listOf(216000138),
            HomeViewModel.homeBusDestinationStopsForTest(216000068, 216000383),
        )
        assertEquals(
            listOf(121000060, 121000929, 121000974, 121000970, 121000220),
            HomeViewModel.homeBusDestinationStopsForTest(216000061, 216000381),
        )
        assertEquals(
            listOf(226000042),
            HomeViewModel.homeBusDestinationStopsForTest(216000026, 216000719),
        )
    }
}
