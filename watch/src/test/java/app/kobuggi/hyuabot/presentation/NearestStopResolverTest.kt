package app.kobuggi.hyuabot.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NearestStopResolverTest {
    @Test
    fun exactShuttlecockLocationResolvesToShuttlecock() {
        assertEquals(
            "shuttlecock",
            NearestStopResolver.resolve(37.29875417910844, 126.83784054072336, 10f),
        )
    }

    @Test
    fun inaccurateLocationDoesNotSelectAStop() {
        assertNull(
            NearestStopResolver.resolve(37.29875417910844, 126.83784054072336, 600f),
        )
    }

    @Test
    fun locationOutsideCampusDoesNotSelectAStop() {
        assertNull(NearestStopResolver.resolve(37.5665, 126.9780, 10f))
    }
}
