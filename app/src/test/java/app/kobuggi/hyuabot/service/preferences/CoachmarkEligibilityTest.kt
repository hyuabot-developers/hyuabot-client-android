package app.kobuggi.hyuabot.service.preferences

import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.kobuggi.hyuabot.test.UserPreferencesRepositoryTestFactory
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachmarkEligibilityTest {
    @Test
    fun `concurrent initialization returns same new user classification without marking home seen`() = runTest {
        val factory = UserPreferencesRepositoryTestFactory(this)
        try {
            val repository = factory.create()
            val first = async { repository.initializeCoachmarks() }
            val second = async { repository.initializeCoachmarks() }
            assertTrue(first.await())
            assertTrue(second.await())
            assertFalse(repository.coachmarkSeen(Coachmarks.HOME).first())
        } finally {
            factory.cleanUp()
        }
    }

    @Test
    fun `old data is classified as existing and explicit reset permits replay`() = runTest {
        val factory = UserPreferencesRepositoryTestFactory(this)
        try {
            val old = androidx.datastore.preferences.core.mutablePreferencesOf().apply {
                this[stringSetPreferencesKey("coachmarks_seen")] = emptySet()
            }
            val repository = factory.create(old, listOf(CoachmarkPreferencesMigration { false }))
            assertFalse(repository.initializeCoachmarks())
            assertTrue(repository.coachmarkSeen(Coachmarks.HOME).first())
            repository.resetCoachmarks()
            assertTrue(repository.initializeCoachmarks())
            assertFalse(repository.coachmarkSeen(Coachmarks.HOME).first())
        } finally {
            factory.cleanUp()
        }
    }
}
