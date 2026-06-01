package app.kobuggi.hyuabot.widget

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.RemoteViewsCompat
import androidx.core.widget.RemoteViewsCompat.RemoteCollectionItems
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import app.kobuggi.hyuabot.ui.MainActivity
import com.apollographql.apollo.api.Optional
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

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
                val appContext = context.applicationContext
                val data = loadData(appContext)
                val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                appWidgetIds.forEach {
                    appWidgetManager.updateAppWidget(it, buildWidget(appContext, it, now, data))
                }
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
        val views = RemoteViews(context.packageName, R.layout.widget_shuttle)
        views.setTextViewText(R.id.widget_shuttle_date, now.format(TIME_FORMAT))
        applyHeader(context, views, data)

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
        views.setEmptyView(R.id.widget_shuttle_list, R.id.widget_shuttle_empty)

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
        val views = RemoteViews(context.packageName, R.layout.widget_shuttle_small)
        applyHeader(context, views, data)

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

    private suspend fun loadData(context: Context): ShuttleWidgetData {
        if (!hasLocationPermission(context)) {
            return ShuttleWidgetData(ShuttleError.NO_PERMISSION, null, null, emptyList())
        }
        val location = getLocation(context)
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
            val nearest = stops.minByOrNull { distanceTo(it, location) }
                ?: return ShuttleWidgetData(ShuttleError.NO_DATA, null, null, emptyList())

            var groups = makeGroups(context, nearest.timetable)
            var stopName = stopDisplayName(context, nearest.name)
            if (nearest.name == "shuttlecock_o" || nearest.name == "shuttlecock_i") {
                val companion = if (nearest.name == "shuttlecock_o") "shuttlecock_i" else "shuttlecock_o"
                stops.firstOrNull { it.name == companion }?.let {
                    groups = groups + makeGroups(context, it.timetable)
                }
                stopName = context.getString(R.string.shuttle_tab_shuttlecock_out)
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

    private fun makeGroups(
        context: Context,
        timetable: ShuttleWidgetQuery.Timetable,
    ): List<ShuttleGroup> = timetable.destination.mapNotNull { group ->
        val times = group.entries.take(5).map { it.time.format(TIME_FORMAT) }
        if (times.isEmpty()) {
            null
        } else {
            ShuttleGroup(destinationDisplayName(context, group.destination), times)
        }
    }

    private fun distanceTo(stop: ShuttleWidgetQuery.Stop, location: Location): Double {
        val dLat = stop.latitude - location.latitude
        val dLng = stop.longitude - location.longitude
        return dLat * dLat + dLng * dLng
    }

    private fun hasLocationPermission(context: Context): Boolean {
        val foreground =
            ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!foreground) {
            return false
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }
        return ContextCompat.checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val last = withTimeoutOrNull(2000) { awaitTask(client.lastLocation) }
        if (last != null && isFresh(last)) return last
        val tokenSource = CancellationTokenSource()
        val current = withTimeoutOrNull(6000) {
            awaitTask(client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token))
        }
        return current ?: last
    }

    private fun isFresh(location: Location): Boolean {
        val ageMillis = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000
        return ageMillis in 0..LOCATION_MAX_AGE_MILLIS
    }

    private suspend fun <T> awaitTask(task: Task<T>): T? =
        suspendCancellableCoroutine { cont ->
            task.addOnSuccessListener { cont.resume(it) }
            task.addOnFailureListener { cont.resume(null) }
            task.addOnCanceledListener { cont.resume(null) }
        }

    private fun stopDisplayName(context: Context, name: String): String = when (name) {
        "dormitory_o" -> context.getString(R.string.shuttle_tab_dormitory_out)
        "shuttlecock_o" -> context.getString(R.string.shuttle_tab_shuttlecock_out)
        "station" -> context.getString(R.string.shuttle_tab_station)
        "terminal" -> context.getString(R.string.shuttle_tab_terminal)
        "jungang_stn" -> context.getString(R.string.shuttle_tab_jungang_station)
        "shuttlecock_i" -> context.getString(R.string.shuttle_tab_shuttlecock_in)
        else -> name
    }

    private fun destinationDisplayName(context: Context, code: String): String = when (code) {
        "STATION" -> context.getString(R.string.shuttle_bound_for_station)
        "TERMINAL" -> context.getString(R.string.shuttle_bound_for_terminal)
        "JUNGANG" -> context.getString(R.string.shuttle_bound_for_jungang_station)
        "CAMPUS" -> context.getString(R.string.shuttle_bound_for_dormitory)
        else -> code
    }

    companion object {
        const val ACTION_REFRESH = "app.kobuggi.hyuabot.widget.ACTION_REFRESH_SHUTTLE"
        private const val MAX_COMPACT_GROUPS = 2
        private const val MAX_COMPACT_TIMES = 2
        private const val LOCATION_MAX_AGE_MILLIS = 60_000L
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

        val providerClasses: List<Class<out ShuttleWidgetProvider>> = listOf(
            ShuttleWidgetSmallProvider::class.java,
            ShuttleWidgetMediumProvider::class.java,
            ShuttleWidgetLargeProvider::class.java,
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

private enum class ShuttleError { NONE, NO_PERMISSION, NO_LOCATION, NO_DATA }

private data class ShuttleGroup(val destination: String, val times: List<String>)

private data class ShuttleWidgetData(
    val error: ShuttleError,
    val stopName: String?,
    val stopCode: String?,
    val groups: List<ShuttleGroup>,
)
