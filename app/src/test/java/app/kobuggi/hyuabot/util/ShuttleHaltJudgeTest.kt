package app.kobuggi.hyuabot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ShuttleHaltJudgeTest {
    private val today = LocalDate.of(2026, 10, 17)

    @Test
    fun haltForTodayReturnsThatDate() {
        val notices = listOf(ServiceNoticeInput("holiday", today, "halt"))
        assertEquals(today, haltDateFor(notices, today))
    }

    @Test
    fun haltForTomorrowReturnsNull() {
        val notices = listOf(ServiceNoticeInput("holiday", today.plusDays(1), "halt"))
        assertNull(haltDateFor(notices, today))
    }

    @Test
    fun holidayThatIsNotHaltReturnsNull() {
        val notices = listOf(ServiceNoticeInput("holiday", today, "reduced"))
        assertNull(haltDateFor(notices, today))
    }

    @Test
    fun periodKindReturnsNull() {
        val notices = listOf(ServiceNoticeInput("period", today, "halt"))
        assertNull(haltDateFor(notices, today))
    }

    @Test
    fun emptyListReturnsNull() {
        assertNull(haltDateFor(emptyList(), today))
    }
}
