package app.kobuggi.hyuabot.ui.home

import android.app.Application
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.ShuttlePresenceService
import app.kobuggi.hyuabot.service.alarm.ShuttleServiceNoticeScheduler
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.RequestLifecycleFixture
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkController
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkOverlayView
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
class HomeCoachmarkRotationTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val liveData = InstantTaskExecutorRule()

    @Test
    fun `each step survives activity recreation without repeating views or finishing`() = runTest {
        RequestLifecycleFixture(this).use { fixture ->
            val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
            val factory = viewModelFactory {
                initializer {
                    val context = controller.get().applicationContext
                    fixture.track(HomeViewModel(fixture.client, fixture.preferences, ShuttleServiceNoticeScheduler(context, fixture.client), ShuttlePresenceService(context)))
                }
            }
            val originalViewModel = ViewModelProvider(controller.get(), factory)[HomeViewModel::class.java]
            val shownSteps = mutableListOf<Int>()
            val completed = mutableListOf<Int>()
            val skipped = mutableListOf<Int>()
            var finished = 0

            fun showOverlay(): CoachmarkOverlayView {
                val activity = controller.get()
                activity.setTheme(R.style.Theme_HYUabot)
                val content = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
                val targets = List(3) { View(activity).also { content.addView(it, LinearLayout.LayoutParams(400, 120)) } }
                activity.setContentView(content)
                val viewModel = ViewModelProvider(activity, factory)[HomeViewModel::class.java]
                assertSame(originalViewModel, viewModel)
                val steps = listOf(
                    CoachmarkStep({ targets[0] }, R.string.coachmark_home_destination_title, R.string.coachmark_home_destination_desc),
                    CoachmarkStep({ targets[1] }, R.string.coachmark_home_movement_title, R.string.coachmark_home_movement_desc),
                    CoachmarkStep({ targets[2] }, R.string.coachmark_home_legacy_title, R.string.coachmark_home_legacy_desc),
                )
                CoachmarkController.show(
                    activity, steps, activity,
                    onFinish = { finished++ }, onStepShown = shownSteps::add,
                    onComplete = completed::add, onSkip = skipped::add,
                    progress = viewModel.coachmarkProgress,
                )
                layout(activity)
                return activity.findViewById<ViewGroup>(android.R.id.content).findViewWithTag("coachmark_overlay")
            }

            var overlay = showOverlay()
            for (step in 1..3) {
                assertEquals("$step/3", overlay.findViewById<TextView>(R.id.coachmark_step).text.toString())
                assertEquals((1..step).toList(), shownSteps)
                controller.recreate()
                overlay = showOverlay()
                assertEquals("$step/3", overlay.findViewById<TextView>(R.id.coachmark_step).text.toString())
                assertEquals((1..step).toList(), shownSteps)
                assertEquals(0, finished)
                assertEquals(emptyList<Int>(), completed)
                assertEquals(emptyList<Int>(), skipped)
                overlay.findViewById<View>(R.id.coachmark_next).performClick()
                layout(controller.get())
            }
            assertEquals(listOf(1, 2, 3), shownSteps)
            assertEquals(listOf(3), completed)
            assertEquals(emptyList<Int>(), skipped)
            assertEquals(1, finished)
            controller.pause().stop().destroy()
        }
    }

    private fun layout(activity: FragmentActivity) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content)
        repeat(3) {
            forceLayout(root)
            root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY))
            root.layout(0, 0, 1080, 1920)
        }
    }

    private fun forceLayout(view: View) {
        view.forceLayout()
        if (view is ViewGroup) (0 until view.childCount).forEach { forceLayout(view.getChildAt(it)) }
    }
}
