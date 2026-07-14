package app.kobuggi.hyuabot.presentation

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.kobuggi.hyuabot.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchNavigationSmokeTest {
    private val startIntent = Intent(
        ApplicationProvider.getApplicationContext(),
        MainActivity::class.java,
    ).putExtra("stopID", "dormitory")

    @get:Rule
    val composeTestRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>(
        activityRule = ActivityScenarioRule<MainActivity>(startIntent),
        activityProvider = { rule ->
            var activity: MainActivity? = null
            rule.scenario.onActivity { activity = it }
            checkNotNull(activity)
        },
    )

    @Test
    fun stopDetailRendersForExplicitStop() {
        composeTestRule.onNodeWithText(text(R.string.stop_station)).assertIsDisplayed()
        composeTestRule.onNodeWithText(text(R.string.stop_terminal)).assertIsDisplayed()
    }

    @Test
    fun otherStopsReturnsFromDetailToStopList() {
        composeTestRule.onNode(hasScrollAction()).performScrollToIndex(5)
        composeTestRule.onNodeWithText(text(R.string.other_stops))
            .performClick()

        composeTestRule.onNodeWithText(text(R.string.stop_dormitory)).assertIsDisplayed()
        composeTestRule.onNodeWithText(text(R.string.stop_shuttlecock)).assertIsDisplayed()
        composeTestRule.onNodeWithText(text(R.string.stop_station)).assertIsDisplayed()
    }

    private fun text(resId: Int): String = composeTestRule.activity.getString(resId)
}
