package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BusStopSequenceTest {
    @Test
    fun `stored display resources map to server stop identifiers`() {
        val expected = mapOf(
            R.string.bus_stop_convention to 216000379,
            R.string.bus_stop_cluster to 216000381,
            R.string.bus_stop_dormitory to 216000383,
            R.string.bus_stop_main_gate to 216000719,
            R.string.bus_stop_seocho to 121000060,
            R.string.bus_stop_gyodae to 121000929,
            R.string.bus_stop_gangnam to 121000974,
            R.string.bus_stop_yangjae to 121000970,
            R.string.bus_stop_yangjae_forest to 121000220,
            R.string.bus_stop_entrance to 216000070,
            R.string.bus_stop_suwon_station to 202000106,
        )
        expected.forEach { (resource, sequence) -> assertEquals(sequence, busStopSequence(resource)) }
        assertNull(busStopSequence(null))
        assertNull(busStopSequence(-1))
    }
}
