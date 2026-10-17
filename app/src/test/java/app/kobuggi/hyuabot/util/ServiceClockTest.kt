package app.kobuggi.hyuabot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ServiceClockTest {
    @Test
    fun serviceDateBeforeFourIsPreviousDay() {
        inBothDefaultZones {
            assertEquals(LocalDate.of(2026, 10, 16), ServiceClock.serviceDate(kst(2026, 10, 17, 3, 59)))
            assertEquals(LocalDate.of(2026, 10, 17), ServiceClock.serviceDate(kst(2026, 10, 17, 4, 0)))
        }
    }

    @Test
    fun lateNightCutoffIsTwoToFiveKst() {
        inBothDefaultZones {
            assertFalse(ServiceClock.isLateNightCutoff(kst(2026, 10, 17, 1, 59)))
            assertTrue(ServiceClock.isLateNightCutoff(kst(2026, 10, 17, 2, 0)))
            assertTrue(ServiceClock.isLateNightCutoff(kst(2026, 10, 17, 3, 59)))
            assertTrue(ServiceClock.isLateNightCutoff(kst(2026, 10, 17, 4, 59)))
            assertFalse(ServiceClock.isLateNightCutoff(kst(2026, 10, 17, 5, 0)))
        }
    }

    @Test
    fun serviceSecondOfDayWrapsEarlyMorning() {
        inBothDefaultZones {
            assertTrue(
                ServiceClock.serviceSecondOfDay(LocalTime.of(1, 50)) >
                    ServiceClock.serviceSecondOfDay(LocalTime.of(23, 0)),
            )
            assertEquals(86_400 + 1 * 3600 + 50 * 60, ServiceClock.serviceSecondOfDay(LocalTime.of(1, 50)))
            assertEquals(4 * 3600, ServiceClock.serviceSecondOfDay(LocalTime.of(4, 0)))
        }
    }

    @Test
    fun remainingMinutesIsFlooredAndCanBeNegative() {
        inBothDefaultZones {
            val now = kst(2026, 10, 17, 10, 0)
            assertEquals(5, ServiceClock.remainingMinutes(LocalTime.of(10, 5), now))
            assertEquals(0, ServiceClock.remainingMinutes(LocalTime.of(10, 0), now))
            assertEquals(-1, ServiceClock.remainingMinutes(LocalTime.of(9, 59), now))
        }
    }
}
