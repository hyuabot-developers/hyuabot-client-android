package app.kobuggi.hyuabot.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreshnessStaleSessionGuardTest {
    @Test
    fun eachDomainIsReportedOnlyOncePerSession() {
        val guard = FreshnessStaleSessionGuard()

        assertTrue(guard.shouldLog("bus"))
        assertFalse(guard.shouldLog("bus"))
        assertTrue(guard.shouldLog("subway"))
        assertTrue(guard.shouldLog("readingRoom"))
    }
}
