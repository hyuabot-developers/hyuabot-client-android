package app.kobuggi.hyuabot.service.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CampusTimeTest {
    @Test
    fun nowAndTodayUseSeoulTime() {
        val clock = Clock.fixed(Instant.parse("2026-10-31T15:30:00Z"), ZoneOffset.UTC)
        assertEquals(ZonedDateTime.of(2026, 11, 1, 0, 30, 0, 0, CampusTime.ZONE), CampusTime.now(clock))
        assertEquals(LocalDate.of(2026, 11, 1), CampusTime.today(clock))
    }

    @Test
    fun minutesUntilWrapsAtMidnight() {
        assertEquals(25L, CampusTime.minutesUntil(time(10, 0), time(10, 25)))
        assertEquals(0L, CampusTime.minutesUntil(time(10, 0), time(10, 0)))
        assertEquals(20L, CampusTime.minutesUntil(time(23, 50), time(0, 10)))
    }

    @Test
    fun currentUsesMealBoundaries() {
        assertEquals(MealPeriod.BREAKFAST, MealPeriod.current(time(0, 0)))
        assertEquals(MealPeriod.BREAKFAST, MealPeriod.current(time(9, 59)))
        assertEquals(MealPeriod.LUNCH, MealPeriod.current(time(10, 0)))
        assertEquals(MealPeriod.LUNCH, MealPeriod.current(time(14, 59)))
        assertEquals(MealPeriod.DINNER, MealPeriod.current(time(15, 0)))
        assertEquals(MealPeriod.DINNER, MealPeriod.current(time(19, 59)))
        assertEquals(MealPeriod.DINNER, MealPeriod.current(time(23, 59)))
        assertEquals("조식", MealPeriod.BREAKFAST.typeString)
    }

    @Test
    fun screenDefaultUsesMealAndNextDayBoundaries() {
        assertSelection(9, 59, LocalDate.of(2026, 10, 31), MealPeriod.BREAKFAST, false)
        assertSelection(10, 0, LocalDate.of(2026, 10, 31), MealPeriod.LUNCH, false)
        assertSelection(14, 59, LocalDate.of(2026, 10, 31), MealPeriod.LUNCH, false)
        assertSelection(15, 0, LocalDate.of(2026, 10, 31), MealPeriod.DINNER, false)
        assertSelection(19, 59, LocalDate.of(2026, 10, 31), MealPeriod.DINNER, false)
        assertSelection(20, 0, LocalDate.of(2026, 11, 1), MealPeriod.BREAKFAST, true)
        assertSelection(23, 59, LocalDate.of(2026, 11, 1), MealPeriod.BREAKFAST, true)
    }

    @Test
    fun screenDefaultConvertsInputZoneToSeoul() {
        val la = ZoneId.of("America/Los_Angeles")
        val beforeMidnight = Clock.fixed(Instant.parse("2026-10-31T14:59:00Z"), la)
        assertEquals(
            MealSelection(LocalDate.of(2026, 11, 1), MealPeriod.BREAKFAST, true),
            MealPeriod.screenDefault(CampusTime.now(beforeMidnight)),
        )

        val midnight = Clock.fixed(Instant.parse("2026-10-31T15:00:00Z"), la)
        assertEquals(
            MealSelection(LocalDate.of(2026, 11, 1), MealPeriod.BREAKFAST, false),
            MealPeriod.screenDefault(CampusTime.now(midnight)),
        )
        assertEquals(LocalDate.of(2026, 11, 1), CampusTime.today(midnight))
        assertEquals(
            MealSelection(LocalDate.of(2026, 11, 1), MealPeriod.BREAKFAST, true),
            MealPeriod.screenDefault(ZonedDateTime.of(2026, 10, 31, 4, 0, 0, 0, la)),
        )
    }

    private fun assertSelection(
        hour: Int,
        minute: Int,
        date: LocalDate,
        meal: MealPeriod,
        isTomorrow: Boolean,
    ) {
        val now = ZonedDateTime.of(2026, 10, 31, hour, minute, 0, 0, CampusTime.ZONE)
        assertEquals(MealSelection(date, meal, isTomorrow), MealPeriod.screenDefault(now))
    }

    private fun time(hour: Int, minute: Int): LocalTime = LocalTime.of(hour, minute)
}
