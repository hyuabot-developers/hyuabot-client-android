package app.kobuggi.hyuabot.service.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachmarkPreferencesMigrationTest {
    @Test
    fun `old preference data migrates all visible keys including empty seen set`() = runTest {
        val migration = CoachmarkPreferencesMigration { false }
        val old = mutablePreferencesOf().apply { this[stringSetPreferencesKey("coachmarks_seen")] = emptySet() }

        assertTrue(migration.shouldMigrate(old))
        val migrated = migration.migrate(old)
        assertTrue(migrated[booleanPreferencesKey("coachmark_initialized")] == true)
        assertEquals(Coachmarks.USER_VISIBLE_KEYS, migrated[stringSetPreferencesKey("coachmarks_seen")])
        assertTrue(migrated[booleanPreferencesKey("coachmark_eligibility_migrated_v3")] == true)
    }

    @Test
    fun `explicit reset keys survive migration and a second migration is unchanged`() = runTest {
        val migration = CoachmarkPreferencesMigration { false }
        val old = mutablePreferencesOf().apply {
            this[stringSetPreferencesKey("coachmarks_seen")] = emptySet()
            this[stringSetPreferencesKey("coachmark_reset_keys")] = setOf(Coachmarks.HOME)
        }

        val migrated = migration.migrate(old)
        assertFalse(migrated[stringSetPreferencesKey("coachmarks_seen")]!!.contains(Coachmarks.HOME))
        assertTrue(migrated[stringSetPreferencesKey("coachmark_reset_keys")]!!.contains(Coachmarks.HOME))
        assertEquals(migrated, migration.migrate(migrated))
        assertFalse(migration.shouldMigrate(migrated))
    }

    @Test
    fun `legacy shared preferences trigger migration`() = runTest {
        val migration = CoachmarkPreferencesMigration { true }
        val migrated = migration.migrate(mutablePreferencesOf())
        assertTrue(migrated[booleanPreferencesKey("coachmark_initialized")] == true)
        assertEquals(Coachmarks.USER_VISIBLE_KEYS, migrated[stringSetPreferencesKey("coachmarks_seen")])
    }
}
