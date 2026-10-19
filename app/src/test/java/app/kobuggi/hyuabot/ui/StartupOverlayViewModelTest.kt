package app.kobuggi.hyuabot.ui

import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.ui.common.coachmark.StartupOverlay
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun cancelledActivityInitializationCanRestartWithoutRetainingItsDecisionWork() = runTest {
        val viewModel = StartupOverlayViewModel()
        val started = CompletableDeferred<Unit>()
        var cancelled = false
        val activityJob = launch {
            viewModel.initialize {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
        }
        started.await()
        activityJob.cancelAndJoin()

        assertTrue(cancelled)
        assertNull(viewModel.decision.value)

        var decisions = 0
        viewModel.initialize {
            decisions++
            StartupOverlay.REVIEW
        }
        viewModel.initialize {
            decisions++
            StartupOverlay.COACHMARK
        }

        assertEquals(1, decisions)
        assertEquals(StartupOverlay.REVIEW, viewModel.decision.value)
        assertTrue(viewModel.claimReviewRequest())
        assertFalse(viewModel.claimReviewRequest())
    }

    @Test
    fun initializationFailureResolvesWithoutAnOverlay() = runTest {
        val viewModel = StartupOverlayViewModel()

        viewModel.initialize { error("unavailable preferences") }

        assertEquals(StartupOverlay.NONE, viewModel.decision.value)
    }

    @Test
    fun replacementActivityWaitsForCancelledInitializationBeforeRetrying() = runTest {
        val viewModel = StartupOverlayViewModel()
        val started = CompletableDeferred<Unit>()
        val activityJob = launch {
            viewModel.initialize {
                started.complete(Unit)
                awaitCancellation()
            }
        }
        started.await()
        val replacementActivityJob = launch {
            viewModel.initialize { StartupOverlay.LANGUAGE }
        }
        runCurrent()
        assertNull(viewModel.decision.value)

        activityJob.cancelAndJoin()
        replacementActivityJob.join()

        assertEquals(StartupOverlay.LANGUAGE, viewModel.decision.value)
    }
}
