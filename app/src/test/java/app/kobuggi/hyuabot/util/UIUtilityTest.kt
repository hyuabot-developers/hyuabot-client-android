package app.kobuggi.hyuabot.util

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class UIUtilityTest {
    @Test
    fun isDarkModeOnReturnsTrueForNightMode() {
        assertTrue(UIUtility.isDarkModeOn(resources(Configuration.UI_MODE_NIGHT_YES)))
    }

    @Test
    fun isDarkModeOnReturnsFalseForNonNightModes() {
        assertFalse(UIUtility.isDarkModeOn(resources(Configuration.UI_MODE_NIGHT_NO)))
        assertFalse(UIUtility.isDarkModeOn(resources(Configuration.UI_MODE_NIGHT_UNDEFINED)))
    }

    private fun resources(nightMode: Int): Resources {
        val resources = Resources.getSystem()
        resources.configuration.uiMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        return resources
    }
}
