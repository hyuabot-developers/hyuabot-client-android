package app.kobuggi.hyuabot.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ScreenFreshnessTest {
    private val early = Instant.parse("2026-10-18T01:00:00Z")
    private val late = Instant.parse("2026-10-18T02:00:00Z")

    @Test fun emptyList() { assertNull(screenReferenceTime(emptyList<Instant?>())) }
    @Test fun allNull() { assertNull(screenReferenceTime(listOf(null, null))) }
    @Test fun ignoresNullSections() { assertEquals(late, screenReferenceTime(listOf(null, late))) }
    @Test fun picksEarliestSectionReference() { assertEquals(early, screenReferenceTime(listOf(late, early))) }
    @Test fun singleValue() { assertEquals(late, screenReferenceTime(listOf(late))) }
}
