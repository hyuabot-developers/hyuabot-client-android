package app.kobuggi.hyuabot.ui.setting

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.kobuggi.hyuabot.service.database.AppDatabase
import app.kobuggi.hyuabot.service.database.entity.Contact
import app.kobuggi.hyuabot.service.database.entity.Event
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
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class LanguageSettingDialogViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var repositoryFactory: UserPreferencesRepositoryTestFactory

    @After
    fun tearDown() {
        if (this::database.isInitialized) {
            database.close()
        }
        if (this::repositoryFactory.isInitialized) {
            repositoryFactory.cleanUp()
        }
    }

    @Test
    fun setLocaleCodeClearsLocalizedCachesAndUpdatesLocaleCode() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val repository = repositoryFactory.create()
        val viewModel = LanguageSettingDialogViewModel(database, repository)

        repository.setContactVersion("contacts-v1")
        repository.setCalendarVersion("calendar-v1")
        database.contactDao().insertAll(Contact(1, 2, "연락처", "031-000-0000"))
        database.calendarDao().insertAll(Event(1, "행사", "", "2026-01-01", "2026-01-01", "전체"))

        viewModel.setLocaleCode("ko")
        advanceUntilIdle()

        assertEquals("ko", viewModel.localeCode.value)
        assertNull(repository.contactVersion.first())
        assertNull(repository.calendarVersion.first())
        assertEquals(emptyList<Contact>(), database.contactDao().getAll().first())
        assertEquals(emptyList<Event>(), database.calendarDao().getAll().first())
    }
}
