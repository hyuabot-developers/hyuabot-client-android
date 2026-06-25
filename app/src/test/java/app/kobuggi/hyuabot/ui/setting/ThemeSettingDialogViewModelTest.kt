package app.kobuggi.hyuabot.ui.setting

import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.UserPreferencesRepositoryTestFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeSettingDialogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositoryFactory: UserPreferencesRepositoryTestFactory

    @After
    fun tearDown() {
        repositoryFactory.cleanUp()
    }

    @Test
    fun setDarkModePersistsDarkThemeWhenEnabled() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val repository = repositoryFactory.create()
        val viewModel = ThemeSettingDialogViewModel(repository)

        viewModel.setDarkMode(true)
        advanceUntilIdle()

        assertEquals("dark", repository.theme.first())
    }

    @Test
    fun setDarkModePersistsLightThemeWhenDisabled() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val repository = repositoryFactory.create()
        val viewModel = ThemeSettingDialogViewModel(repository)

        viewModel.setDarkMode(false)
        advanceUntilIdle()

        assertEquals("light", repository.theme.first())
    }

    @Test
    fun setDarkModeSystemClearsTheme() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val repository = repositoryFactory.create()
        val viewModel = ThemeSettingDialogViewModel(repository)

        viewModel.setDarkMode(true)
        viewModel.setDarkModeSystem()
        advanceUntilIdle()

        assertNull(repository.theme.first())
    }
}
