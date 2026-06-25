package app.kobuggi.hyuabot.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.kobuggi.hyuabot.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchNavigationSmokeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainStopListRenders() {
        composeTestRule.onNodeWithText(text(R.string.stop_dormitory)).assertIsDisplayed()
        composeTestRule.onNodeWithText(text(R.string.stop_shuttlecock)).assertIsDisplayed()
        composeTestRule.onNodeWithText(text(R.string.stop_station)).assertIsDisplayed()
    }

    @Test
    fun stopDetailRendersAfterStopClick() {
        composeTestRule.onNodeWithText(text(R.string.stop_dormitory)).performClick()

        composeTestRule.onNodeWithText(text(R.string.stop_station)).assertIsDisplayed()
        composeTestRule.onNodeWithText(text(R.string.stop_terminal)).assertIsDisplayed()
        composeTestRule.onNodeWithText(text(R.string.stop_jungang)).assertIsDisplayed()
    }

    private fun text(resId: Int): String = composeTestRule.activity.getString(resId)
}
