package app.kobuggi.hyuabot.ui.common

import android.view.View
import android.view.ContextThemeWrapper
import app.kobuggi.hyuabot.util.AnalyticsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = android.app.Application::class)
class StateViewTest {
    @Test fun rebindingResetsIconAndActionAndUsesOnlyCurrentCallback() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), app.kobuggi.hyuabot.R.style.Theme_HYUabot)
        val view = StateView(context)
        var oldAction = 0
        var newAction = 0
        view.bind(StateModel(StateKind.ERROR), AnalyticsScreen.BUS_REALTIME) { oldAction++ }
        val button = view.findViewById<com.google.android.material.button.MaterialButton>(app.kobuggi.hyuabot.R.id.state_action)
        view.bind(StateModel(StateKind.EMPTY), AnalyticsScreen.BUS_REALTIME)
        assertEquals(View.GONE, view.findViewById<View>(app.kobuggi.hyuabot.R.id.state_icon).visibility)
        assertEquals(View.GONE, button.visibility)
        view.bind(StateModel(StateKind.ENDED), AnalyticsScreen.BUS_REALTIME) { newAction++ }
        button.performClick()
        assertEquals(0, oldAction)
        assertEquals(1, newAction)
        assertTrue(view.findViewById<View>(app.kobuggi.hyuabot.R.id.state_header).isScreenReaderFocusable)
        assertFalse(button.importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_NO)
    }

    @Test fun inactiveStateDoesNotCreateImpression() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), app.kobuggi.hyuabot.R.style.Theme_HYUabot)
        val view = StateView(context)
        view.setScreenActive(false)
        view.bind(StateModel(StateKind.EMPTY), AnalyticsScreen.BUS_REALTIME)
        assertEquals(View.VISIBLE, view.visibility)
    }
}
