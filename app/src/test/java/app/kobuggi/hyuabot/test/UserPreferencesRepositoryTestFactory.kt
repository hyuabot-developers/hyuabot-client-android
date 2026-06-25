package app.kobuggi.hyuabot.test

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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

    fun create(): UserPreferencesRepository {
        val file = File.createTempFile("user-preferences", ".preferences_pb")
        file.delete()
        files += file
        val dataStoreScope = CoroutineScope(testScope.coroutineContext + Job())
        dataStoreScopes += dataStoreScope
        return UserPreferencesRepository(
            PreferenceDataStoreFactory.create(
                scope = dataStoreScope,
                produceFile = { file },
            ),
        )
    }

    fun cleanUp() {
        dataStoreScopes.forEach { it.cancel() }
        files.forEach { it.delete() }
    }
}
