package app.kobuggi.hyuabot.test

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope

class UserPreferencesRepositoryTestFactory(
    private val testScope: TestScope,
) {
    private val dataStoreScopes = mutableListOf<CoroutineScope>()
    private val files = mutableListOf<File>()

    fun create(
        initialPreferences: Preferences = emptyPreferences(),
        migrations: List<DataMigration<Preferences>> = emptyList(),
    ): UserPreferencesRepository {
        val file = File.createTempFile("user-preferences", ".preferences_pb")
        file.delete()
        files += file
        val dataStoreScope = CoroutineScope(testScope.coroutineContext + Job())
        dataStoreScopes += dataStoreScope
        return UserPreferencesRepository(
            PreferenceDataStoreFactory.create(
                scope = dataStoreScope,
                produceFile = { file },
                migrations = buildList {
                    if (initialPreferences.asMap().isNotEmpty()) add(object : DataMigration<Preferences> {
                        override suspend fun shouldMigrate(currentData: Preferences) = currentData.asMap().isEmpty()
                        override suspend fun migrate(currentData: Preferences): Preferences =
                            mutablePreferencesOf().also { target -> initialPreferences.asMap().forEach { (key, value) ->
                                @Suppress("UNCHECKED_CAST")
                                target[key as Preferences.Key<Any>] = value
                            } }
                        override suspend fun cleanUp() = Unit
                    })
                    addAll(migrations)
                },
            ),
        )
    }

    fun cleanUp() {
        dataStoreScopes.forEach { it.cancel() }
        files.forEach { it.delete() }
    }
}
