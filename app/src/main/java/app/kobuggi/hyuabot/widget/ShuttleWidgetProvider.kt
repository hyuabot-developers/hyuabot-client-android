package app.kobuggi.hyuabot.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import androidx.core.widget.RemoteViewsCompat
import androidx.core.widget.RemoteViewsCompat.RemoteCollectionItems
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import app.kobuggi.hyuabot.ui.MainActivity
import app.kobuggi.hyuabot.util.localizedContext
import com.apollographql.apollo.api.Optional
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

abstract class ShuttleWidgetProvider : AppWidgetProvider() {
    protected abstract val size: ShuttleWidgetSize
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, javaClass))
            updateWidgets(context, appWidgetManager, ids)
        }
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                withTimeout(WIDGET_TIMEOUT_MS) {
                    val appContext = context.applicationContext
                    val localizedAppContext = localizedContext(appContext)
                    val data = loadData(appContext, localizedAppContext)
                    val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    appWidgetIds.forEach {
                        appWidgetManager.updateAppWidget(it, buildWidget(appContext, it, now, data))
                    }
                }
            } catch (_: TimeoutCancellationException) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun buildWidget(
        context: Context,
        appWidgetId: Int,
        now: ZonedDateTime,
        data: ShuttleWidgetData,
    ): RemoteViews = when (size) {
        ShuttleWidgetSize.SMALL -> buildCompactWidget(context, data)
        ShuttleWidgetSize.MEDIUM, ShuttleWidgetSize.LARGE -> buildListWidget(context, appWidgetId, now, data)
    }

    private fun buildListWidget(
        context: Context,
        appWidgetId: Int,
        now: ZonedDateTime,
        data: ShuttleWidgetData,
    ): RemoteViews {
        val textContext = localizedContext(context)
        val views = RemoteViews(context.packageName, R.layout.widget_shuttle)
        views.setTextViewText(R.id.widget_shuttle_date, now.format(ShuttleWidgetSupport.TIME_FORMAT))
        applyHeader(textContext, views, data)

        if (data.groups.isEmpty()) {
            views.setViewVisibility(R.id.widget_shuttle_list, View.GONE)
            views.setViewVisibility(R.id.widget_shuttle_empty, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_shuttle_empty, View.GONE)
            views.setViewVisibility(R.id.widget_shuttle_list, View.VISIBLE)
            val collection = RemoteCollectionItems.Builder()
                .setViewTypeCount(1)
                .setHasStableIds(true)
                .apply {
                    data.groups.forEachIndexed { index, group ->
                        addItem(index.toLong(), buildItemView(context, group, inCollection = true))
                    }
                }
                .build()
            RemoteViewsCompat.setRemoteAdapter(
                context,
                views,
                appWidgetId,
                R.id.widget_shuttle_list,
                collection
            )
        }

        val launchIntent = launchIntent(context, data.stopCode)
        views.setOnClickPendingIntent(R.id.widget_shuttle_root, openAppIntent(context, launchIntent))
        views.setOnClickPendingIntent(R.id.widget_shuttle_title, openAppIntent(context, launchIntent))
        views.setPendingIntentTemplate(
            R.id.widget_shuttle_list,
            PendingIntent.getActivity(
                context,
                1,
                launchIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        views.setOnClickPendingIntent(R.id.widget_shuttle_refresh, refreshIntent(context))
        return views
    }

    private fun buildCompactWidget(
        context: Context,
        data: ShuttleWidgetData,
    ): RemoteViews {
        val textContext = localizedContext(context)
        val views = RemoteViews(context.packageName, R.layout.widget_shuttle_small)
        applyHeader(textContext, views, data)

        views.removeAllViews(R.id.widget_shuttle_small_container)
        val compact = data.groups.take(MAX_COMPACT_GROUPS)
        if (compact.isEmpty()) {
            views.setViewVisibility(R.id.widget_shuttle_empty, View.VISIBLE)
            views.setViewVisibility(R.id.widget_shuttle_small_container, View.GONE)
        } else {
            views.setViewVisibility(R.id.widget_shuttle_empty, View.GONE)
            views.setViewVisibility(R.id.widget_shuttle_small_container, View.VISIBLE)
            compact.forEach { group ->
                views.addView(
                    R.id.widget_shuttle_small_container,
                    buildItemView(context, group, maxTimes = MAX_COMPACT_TIMES, inCollection = false)
                )
            }
        }

        val launchIntent = launchIntent(context, data.stopCode)
        views.setOnClickPendingIntent(R.id.widget_shuttle_root, openAppIntent(context, launchIntent))
        views.setOnClickPendingIntent(R.id.widget_shuttle_refresh, refreshIntent(context))
        return views
    }

    private fun applyHeader(context: Context, views: RemoteViews, data: ShuttleWidgetData) {
        views.setTextViewText(R.id.widget_shuttle_title, context.getString(R.string.shuttle_bus))
        views.setTextViewText(
            R.id.widget_shuttle_stop,
            data.stopName?.let { "· $it" } ?: ""
        )
        views.setTextViewText(
            R.id.widget_shuttle_empty,
            context.getString(
                when (data.error) {
                    ShuttleError.NO_PERMISSION -> R.string.widget_shuttle_permission_required
                    ShuttleError.NO_LOCATION -> R.string.widget_shuttle_no_location
                    else -> R.string.widget_shuttle_no_data
                }
            )
        )
    }

    private fun buildItemView(
        context: Context,
        group: ShuttleGroup,
        maxTimes: Int = Int.MAX_VALUE,
        inCollection: Boolean,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_shuttle_item)
        views.setTextViewText(R.id.widget_shuttle_destination, group.destination)
        views.setTextViewText(R.id.widget_shuttle_times, group.times.take(maxTimes).joinToString("  "))
        if (inCollection) {
            views.setOnClickFillInIntent(R.id.widget_shuttle_item_root, Intent())
        }
        return views
    }

    private fun launchIntent(context: Context, stopCode: String?): Intent {
        val uri = if (stopCode != null) "hyuabot://shuttle?stop=$stopCode" else "hyuabot://shuttle"
        return Intent(
            Intent.ACTION_VIEW,
            uri.toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    private fun openAppIntent(context: Context, launchIntent: Intent): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun refreshIntent(context: Context): PendingIntent {
        val intent = Intent(context, javaClass).apply { action = ACTION_REFRESH }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private suspend fun loadData(context: Context, textContext: Context): ShuttleWidgetData {
        if (!ShuttleWidgetSupport.hasLocationPermission(context)) {
            return ShuttleWidgetData(ShuttleError.NO_PERMISSION, null, null, emptyList())
        }
        val location = ShuttleWidgetSupport.getLocation(context)
            ?: return ShuttleWidgetData(ShuttleError.NO_LOCATION, null, null, emptyList())

        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            ShuttleWidgetEntryPoint::class.java
        )
        return try {
            val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
            val response = entryPoint.apolloClient()
                .query(ShuttleWidgetQuery(Optional.present(now)))
                .execute()
            val stops = response.data?.shuttle?.stops
            if (stops.isNullOrEmpty()) {
                return ShuttleWidgetData(ShuttleError.NO_DATA, null, null, emptyList())
            }
            val nearest = stops.minByOrNull { ShuttleWidgetSupport.distanceTo(it, location) }
                ?: return ShuttleWidgetData(ShuttleError.NO_DATA, null, null, emptyList())

            var groups = ShuttleWidgetSupport.makeGroups(textContext, nearest.timetable)
            var stopName = ShuttleWidgetSupport.stopDisplayName(textContext, nearest.name)
            if (nearest.name == "shuttlecock_o" || nearest.name == "shuttlecock_i") {
                val companion = if (nearest.name == "shuttlecock_o") "shuttlecock_i" else "shuttlecock_o"
                stops.firstOrNull { it.name == companion }?.let {
                    groups = groups + ShuttleWidgetSupport.makeGroups(textContext, it.timetable)
                }
                stopName = textContext.getString(R.string.shuttle_tab_shuttlecock_out)
            }

            if (groups.isEmpty()) {
                ShuttleWidgetData(ShuttleError.NO_DATA, stopName, nearest.name, emptyList())
            } else {
                ShuttleWidgetData(ShuttleError.NONE, stopName, nearest.name, groups)
            }
        } catch (_: Exception) {
            ShuttleWidgetData(ShuttleError.NO_DATA, null, null, emptyList())
        }
    }

    companion object {
        const val ACTION_REFRESH = "app.kobuggi.hyuabot.widget.ACTION_REFRESH_SHUTTLE"
        private const val WIDGET_TIMEOUT_MS = 8_000L
        private const val MAX_COMPACT_GROUPS = 2
        private const val MAX_COMPACT_TIMES = 2

        val providerClasses: List<Class<out AppWidgetProvider>> = listOf(
            ShuttleWidgetSmallProvider::class.java,
            ShuttleWidgetMediumProvider::class.java,
            ShuttleWidgetLargeProvider::class.java,
            ShuttleTransferWidgetProvider::class.java,
        )
    }
}

enum class ShuttleWidgetSize { SMALL, MEDIUM, LARGE }

class ShuttleWidgetSmallProvider : ShuttleWidgetProvider() {
    override val size = ShuttleWidgetSize.SMALL
}

class ShuttleWidgetMediumProvider : ShuttleWidgetProvider() {
    override val size = ShuttleWidgetSize.MEDIUM
}

class ShuttleWidgetLargeProvider : ShuttleWidgetProvider() {
    override val size = ShuttleWidgetSize.LARGE
}

internal data class ShuttleWidgetData(
    val error: ShuttleError,
    val stopName: String?,
    val stopCode: String?,
    val groups: List<ShuttleGroup>,
)
