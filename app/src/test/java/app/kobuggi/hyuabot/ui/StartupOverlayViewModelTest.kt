package app.kobuggi.hyuabot.ui

import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.ui.common.coachmark.StartupOverlay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartupOverlayViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startupCoachmarkClaimOnlyBlocksTheClaimedKey() = runTest {
        val viewModel = StartupOverlayViewModel()
        viewModel.initialize { StartupOverlay.COACHMARK }
        advanceUntilIdle()

        assertTrue(viewModel.claimCoachmark("home"))
        assertFalse(viewModel.claimCoachmark("home"))
        assertTrue(viewModel.claimCoachmark("map"))
    }

    @Test
    fun nonCoachmarkStartupDecisionDoesNotBlockScreenCoachmarks() = runTest {
        val viewModel = StartupOverlayViewModel()
        viewModel.initialize { StartupOverlay.LANGUAGE }
        advanceUntilIdle()

        assertTrue(viewModel.claimCoachmark("home"))
        assertTrue(viewModel.claimCoachmark("map"))
    }
}
