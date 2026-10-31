package app.kobuggi.hyuabot.service.assistant

import android.app.Application
import android.content.Context
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class AssistantShortcutPublisherTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun buildShortcutsReturnsThreeShortcutsInOrderWithExpectedDeepLinks() {
        val shortcuts = AssistantShortcutPublisher.buildShortcuts(context)

        assertEquals(
            listOf(
                AssistantShortcutPublisher.SHORTCUT_SHUTTLE_DORMITORY,
                AssistantShortcutPublisher.SHORTCUT_SHUTTLE_STATION,
                AssistantShortcutPublisher.SHORTCUT_CAFETERIA_NOW,
            ),
            shortcuts.map { it.id },
        )
        assertEquals(
            listOf(
                "hyuabot://shuttle?stop=dormitory_o",
                "hyuabot://shuttle?stop=station",
                "hyuabot://cafeteria",
            ),
            shortcuts.map { it.intent.data.toString() },
        )
    }

    @Test
    fun publishSetsDynamicShortcutCountWithinAvailableRoom() {
        AssistantShortcutPublisher.publish(context)

        assertEquals(
            minOf(
                3,
                (ShortcutManagerCompat.getMaxShortcutCountPerActivity(context) - 4).coerceAtLeast(0),
            ),
            ShortcutManagerCompat.getDynamicShortcuts(context).size,
        )
    }
}
