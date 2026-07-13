package app.kobuggi.hyuabot.util

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class AnalyticsConfigurationTest {
    @Test
    fun `automatic screen reporting is disabled`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)

        assertFalse(applicationInfo.metaData.getBoolean("google_analytics_automatic_screen_reporting_enabled", true))
    }

    @Test
    fun `analytics identifiers are unique and reportable`() {
        assertTrue(AnalyticsScreen.entries.map { it.id }.all(::isReportable))
        assertTrue(AnalyticsContentType.entries.map { it.id }.all(::isReportable))
        assertTrue(AnalyticsItem.entries.map { it.id }.all(::isReportable))
        assertEquals(AnalyticsScreen.entries.size, AnalyticsScreen.entries.map { it.id }.toSet().size)
        assertEquals(AnalyticsContentType.entries.size, AnalyticsContentType.entries.map { it.id }.toSet().size)
        assertEquals(AnalyticsItem.entries.size, AnalyticsItem.entries.map { it.id }.toSet().size)
    }

    @Test
    fun `home uses dedicated cross platform identifiers`() {
        assertEquals("home", AnalyticsScreen.HOME.id)
        assertEquals("tab_home", AnalyticsItem.TAB_HOME.id)
        assertEquals("home_open_shuttle_detail", AnalyticsItem.HOME_OPEN_SHUTTLE_DETAIL.id)
        assertEquals("home_select_destination", AnalyticsItem.HOME_SELECT_DESTINATION.id)
    }

    private fun isReportable(id: String): Boolean =
        id.isNotEmpty() && id.length <= 40 && id.first().isLetter() && id.all { it.isLowerCase() || it.isDigit() || it == '_' }
}
