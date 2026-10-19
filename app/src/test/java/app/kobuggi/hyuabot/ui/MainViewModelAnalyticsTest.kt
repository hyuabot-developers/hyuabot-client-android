package app.kobuggi.hyuabot.ui

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.UserPreferencesRepositoryTestFactory
import app.kobuggi.hyuabot.util.AnalyticsScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelAnalyticsTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val liveData = InstantTaskExecutorRule()

    @Test
    fun `rotation suppresses the restored screen once and preserves return navigation`() = runTest {
        val preferencesFactory = UserPreferencesRepositoryTestFactory(this)
        try {
            val preferences = preferencesFactory.create()
            val factory = viewModelFactory { initializer { MainViewModel(preferences) } }
            val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
            val viewModel = ViewModelProvider(controller.get(), factory)[MainViewModel::class.java]
            val screens = mutableListOf<AnalyticsScreen>()
            val dispatcher = viewModel.createAnalyticsScreenDispatcher(screens::add)
            dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
            dispatcher.onResumed()
            dispatcher.onPaused()

            controller.recreate()
            val retained = ViewModelProvider(controller.get(), factory)[MainViewModel::class.java]
            assertSame(viewModel, retained)
            val restored = retained.createAnalyticsScreenDispatcher(screens::add)
            restored.onDestinationChanged(AnalyticsScreen.HOME)
            restored.onResumed()
            assertEquals(listOf(AnalyticsScreen.HOME), screens)

            restored.onDestinationChanged(null)
            restored.onDestinationChanged(AnalyticsScreen.HOME)
            assertEquals(listOf(AnalyticsScreen.HOME, AnalyticsScreen.HOME), screens)
            controller.pause().stop().destroy()
        } finally {
            preferencesFactory.cleanUp()
        }
    }

    @Test
    fun `restoring saved state with a new view model logs the screen again`() = runTest {
        val preferencesFactory = UserPreferencesRepositoryTestFactory(this)
        try {
            val preferences = preferencesFactory.create()
            val factory = viewModelFactory { initializer { MainViewModel(preferences) } }
            val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
            val viewModel = ViewModelProvider(controller.get(), factory)[MainViewModel::class.java]
            val screens = mutableListOf<AnalyticsScreen>()
            val dispatcher = viewModel.createAnalyticsScreenDispatcher(screens::add)
            dispatcher.onDestinationChanged(AnalyticsScreen.HOME)
            dispatcher.onResumed()
            dispatcher.onPaused()
            val savedState = Bundle()
            controller.pause().saveInstanceState(savedState).stop().destroy()

            val restoredController = Robolectric.buildActivity(FragmentActivity::class.java).setup(savedState)
            val replacement = ViewModelProvider(restoredController.get(), factory)[MainViewModel::class.java]
            assertNotSame(viewModel, replacement)
            val restored = replacement.createAnalyticsScreenDispatcher(screens::add)
            restored.onDestinationChanged(AnalyticsScreen.HOME)
            restored.onResumed()
            assertEquals(listOf(AnalyticsScreen.HOME, AnalyticsScreen.HOME), screens)
            restoredController.pause().stop().destroy()
        } finally {
            preferencesFactory.cleanUp()
        }
    }
}
