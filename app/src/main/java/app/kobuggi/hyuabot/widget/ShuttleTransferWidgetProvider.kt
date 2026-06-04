package app.kobuggi.hyuabot.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleTransferQuery
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import app.kobuggi.hyuabot.ui.MainActivity
import app.kobuggi.hyuabot.util.TransitRow
import app.kobuggi.hyuabot.util.buildTransitRows
import app.kobuggi.hyuabot.util.currentShuttleWeekday
import app.kobuggi.hyuabot.util.localizedContext
import com.apollographql.apollo.api.Optional
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneId

class ShuttleTransferWidgetProvider : AppWidgetProvider() {
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
                val appContext = context.applicationContext
                val data = loadData(appContext)
                appWidgetIds.forEach {
                    appWidgetManager.updateAppWidget(it, buildWidget(appContext, data))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun loadData(context: Context): TransferWidgetData {
        if (!ShuttleWidgetSupport.hasLocationPermission(context)) {
            return TransferWidgetData(ShuttleError.NO_PERMISSION, null, null, emptyList(), emptyList())
        }
        val location = ShuttleWidgetSupport.getLocation(context)
            ?: return TransferWidgetData(ShuttleError.NO_LOCATION, null, null, emptyList(), emptyList())

        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            ShuttleWidgetEntryPoint::class.java
        )
        val ctx = localizedContext(context)
        return try {
            val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
            val shuttleResponse = entryPoint.apolloClient()
                .query(ShuttleWidgetQuery(Optional.present(now)))
                .execute()
            val stops = shuttleResponse.data?.shuttle?.stops
            if (stops.isNullOrEmpty()) {
                return TransferWidgetData(ShuttleError.NO_DATA, null, null, emptyList(), emptyList())
            }
            val nearest = stops.minByOrNull { ShuttleWidgetSupport.distanceTo(it, location) }
                ?: return TransferWidgetData(ShuttleError.NO_DATA, null, null, emptyList(), emptyList())

            var groups = ShuttleWidgetSupport.makeGroups(ctx, nearest.timetable)
            var stopName = ShuttleWidgetSupport.stopDisplayName(ctx, nearest.name)
            if (nearest.name == "shuttlecock_o" || nearest.name == "shuttlecock_i") {
                val companion = if (nearest.name == "shuttlecock_o") "shuttlecock_i" else "shuttlecock_o"
                stops.firstOrNull { it.name == companion }?.let {
                    groups = groups + ShuttleWidgetSupport.makeGroups(ctx, it.timetable)
                }
                stopName = ctx.getString(R.string.shuttle_tab_shuttlecock_out)
            }

            val transitData = runCatching {
                entryPoint.apolloClient()
                    .query(ShuttleTransferQuery(currentShuttleWeekday()))
                    .execute()
                    .data
            }.getOrNull()
            val transit = transitData?.let { buildTransitRows(ctx, nearest.name, it) } ?: emptyList()

            if (groups.isEmpty() && transit.isEmpty()) {
                TransferWidgetData(ShuttleError.NO_DATA, stopName, nearest.name, emptyList(), emptyList())
            } else {
                TransferWidgetData(ShuttleError.NONE, stopName, nearest.name, groups, transit)
            }
        } catch (_: Exception) {
            TransferWidgetData(ShuttleError.NO_DATA, null, null, emptyList(), emptyList())
        }
    }

    private fun buildWidget(context: Context, data: TransferWidgetData): RemoteViews {
        val ctx = localizedContext(context)
        val views = RemoteViews(context.packageName, R.layout.widget_shuttle_transfer)
        views.setTextViewText(R.id.widget_transfer_title, ctx.getString(R.string.widget_shuttle_transfer_name))
        views.setTextViewText(R.id.widget_transfer_section_title, ctx.getString(R.string.shuttle_transfer_section_title))
        views.setTextViewText(
            R.id.widget_transfer_stop,
            data.stopName?.let { "· $it" } ?: ""
        )

        views.removeAllViews(R.id.widget_transfer_shuttle_container)
        views.removeAllViews(R.id.widget_transfer_transit_container)

        if (data.error != ShuttleError.NONE) {
            views.setViewVisibility(R.id.widget_transfer_empty, View.VISIBLE)
            views.setTextViewText(
                R.id.widget_transfer_empty,
                ctx.getString(
                    when (data.error) {
                        ShuttleError.NO_PERMISSION -> R.string.widget_shuttle_permission_required
                        ShuttleError.NO_LOCATION -> R.string.widget_shuttle_no_location
                        else -> R.string.widget_shuttle_no_data
                    }
                )
            )
            views.setViewVisibility(R.id.widget_transfer_shuttle_container, View.GONE)
            setSectionVisibility(views, dividerVisible = false, transitVisible = false)
        } else {
            views.setViewVisibility(R.id.widget_transfer_empty, View.GONE)
            val hasShuttle = data.groups.isNotEmpty()
            val hasTransit = data.transit.isNotEmpty()
            views.setViewVisibility(
                R.id.widget_transfer_shuttle_container,
                if (hasShuttle) View.VISIBLE else View.GONE
            )
            val shuttleGroups = data.groups.take(MAX_SHUTTLE_GROUPS)
            shuttleGroups.forEachIndexed { index, group ->
                views.addView(
                    R.id.widget_transfer_shuttle_container,
                    shuttleItemView(context, group, isLast = index == shuttleGroups.lastIndex)
                )
            }
            setSectionVisibility(views, dividerVisible = hasShuttle && hasTransit, transitVisible = hasTransit)
            data.transit.forEach { row ->
                views.addView(R.id.widget_transfer_transit_container, transitItemView(context, row))
            }
        }

        val launchIntent = Intent(
            Intent.ACTION_VIEW,
            (if (data.stopCode != null) "hyuabot://shuttle?stop=${data.stopCode}" else "hyuabot://shuttle").toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        views.setOnClickPendingIntent(
            R.id.widget_transfer_root,
            PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        val refreshIntent = Intent(context, javaClass).apply { action = ACTION_REFRESH }
        views.setOnClickPendingIntent(
            R.id.widget_transfer_refresh,
            PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        return views
    }

    private fun setSectionVisibility(views: RemoteViews, dividerVisible: Boolean, transitVisible: Boolean) {
        views.setViewVisibility(
            R.id.widget_transfer_section_divider,
            if (dividerVisible) View.VISIBLE else View.GONE
        )
        val transit = if (transitVisible) View.VISIBLE else View.GONE
        views.setViewVisibility(R.id.widget_transfer_section_title, transit)
        views.setViewVisibility(R.id.widget_transfer_transit_container, transit)
    }

    private fun shuttleItemView(context: Context, group: ShuttleGroup, isLast: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_shuttle_item)
        views.setTextViewText(R.id.widget_shuttle_destination, group.destination)
        views.setTextViewText(
            R.id.widget_shuttle_times,
            group.times.take(MAX_SHUTTLE_TIMES).joinToString("  ")
        )
        views.setViewVisibility(
            R.id.widget_shuttle_item_divider,
            if (isLast) View.GONE else View.VISIBLE
        )
        return views
    }

    private fun transitItemView(context: Context, row: TransitRow): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_transfer_item)
        views.setTextViewText(R.id.widget_transfer_name, row.name)
        views.setInt(
            R.id.widget_transfer_name,
            "setBackgroundColor",
            ContextCompat.getColor(context, row.colorRes)
        )
        views.setTextViewText(R.id.widget_transfer_detail, row.detail)
        return views
    }

    companion object {
        const val ACTION_REFRESH = "app.kobuggi.hyuabot.widget.ACTION_REFRESH_SHUTTLE_TRANSFER"
        private const val MAX_SHUTTLE_GROUPS = 4
        private const val MAX_SHUTTLE_TIMES = 4
    }
}

private data class TransferWidgetData(
    val error: ShuttleError,
    val stopName: String?,
    val stopCode: String?,
    val groups: List<ShuttleGroup>,
    val transit: List<TransitRow>,
)
