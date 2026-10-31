package app.kobuggi.hyuabot.service.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.kobuggi.hyuabot.R

object AssistantShortcutPublisher {
    const val SHORTCUT_SHUTTLE_DORMITORY = "assistant_shuttle_dormitory"
    const val SHORTCUT_SHUTTLE_STATION = "assistant_shuttle_station"
    const val SHORTCUT_CAFETERIA_NOW = "assistant_cafeteria_now"
    const val STATIC_SHORTCUT_COUNT = 4

    fun buildShortcuts(context: Context): List<ShortcutInfoCompat> = listOf(
        buildShortcut(
            context = context,
            id = SHORTCUT_SHUTTLE_DORMITORY,
            shortLabel = R.string.assistant_shortcut_shuttle_dormitory_short,
            longLabel = R.string.assistant_shortcut_shuttle_dormitory_long,
            icon = R.drawable.ic_widget_shuttle,
            uri = "hyuabot://shuttle?stop=dormitory_o",
        ),
        buildShortcut(
            context = context,
            id = SHORTCUT_SHUTTLE_STATION,
            shortLabel = R.string.assistant_shortcut_shuttle_station_short,
            longLabel = R.string.assistant_shortcut_shuttle_station_long,
            icon = R.drawable.ic_widget_shuttle,
            uri = "hyuabot://shuttle?stop=station",
        ),
        buildShortcut(
            context = context,
            id = SHORTCUT_CAFETERIA_NOW,
            shortLabel = R.string.assistant_shortcut_cafeteria_short,
            longLabel = R.string.assistant_shortcut_cafeteria_long,
            icon = R.drawable.ic_meal_lunch,
            uri = "hyuabot://cafeteria",
        ),
    )

    fun publish(context: Context) {
        runCatching {
            val room = (ShortcutManagerCompat.getMaxShortcutCountPerActivity(context) - STATIC_SHORTCUT_COUNT)
                .coerceAtLeast(0)
            ShortcutManagerCompat.setDynamicShortcuts(context, buildShortcuts(context).take(room))
        }
    }

    private fun buildShortcut(
        context: Context,
        id: String,
        shortLabel: Int,
        longLabel: Int,
        icon: Int,
        uri: String,
    ): ShortcutInfoCompat = ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(context.getString(shortLabel))
        .setLongLabel(context.getString(longLabel))
        .setIcon(IconCompat.createWithResource(context, icon))
        .setIntent(
            Intent(Intent.ACTION_VIEW, Uri.parse(uri)).setPackage(context.packageName),
        )
        .build()
}
