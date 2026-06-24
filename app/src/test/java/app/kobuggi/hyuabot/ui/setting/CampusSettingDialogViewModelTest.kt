package app.kobuggi.hyuabot.ui.setting

import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.UserPreferencesRepositoryTestFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CampusSettingDialogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositoryFactory: UserPreferencesRepositoryTestFactory

    @After
    fun tearDown() {
        repositoryFactory.cleanUp()
    }

    @Test
    fun setCampusIDPersistsCampusID() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val repository = repositoryFactory.create()
        val viewModel = CampusSettingDialogViewModel(repository)

        viewModel.setCampusID(1)
        advanceUntilIdle()

        assertEquals(1, repository.campusID.first())
    }
}
