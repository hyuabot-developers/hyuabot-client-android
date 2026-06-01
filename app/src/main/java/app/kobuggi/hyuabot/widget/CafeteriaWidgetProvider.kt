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
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CafeteriaWidgetProvider : AppWidgetProvider() {
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
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CafeteriaWidgetProvider::class.java)
            )
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
                val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                val meal = WidgetMeal.current(now.toLocalTime())
                val items = loadItems(appContext, meal, now.toLocalDate())
                appWidgetIds.forEach {
                    appWidgetManager.updateAppWidget(it, buildWidget(appContext, it, meal, now, items))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun buildWidget(
        context: Context,
        appWidgetId: Int,
        meal: WidgetMeal,
        now: ZonedDateTime,
        items: List<CafeteriaWidgetItem>,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_cafeteria)
        views.setImageViewResource(R.id.widget_meal_icon, meal.iconRes)
        views.setTextViewText(R.id.widget_meal_title, context.getString(meal.titleRes))
        views.setTextViewText(
            R.id.widget_date,
            now.format(DateTimeFormatter.ofPattern("M/d HH:mm"))
        )

        if (items.isEmpty()) {
            views.setViewVisibility(R.id.widget_cafeteria_list, View.GONE)
            views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_empty, View.GONE)
            views.setViewVisibility(R.id.widget_cafeteria_list, View.VISIBLE)
            val collection = RemoteCollectionItems.Builder()
                .setViewTypeCount(1)
                .setHasStableIds(true)
                .apply {
                    items.forEachIndexed { index, item ->
                        addItem(index.toLong(), buildItemView(context, item))
                    }
                }
                .build()
            RemoteViewsCompat.setRemoteAdapter(
                context,
                views,
                appWidgetId,
                R.id.widget_cafeteria_list,
                collection
            )
        }

        val launchIntent = Intent(
            Intent.ACTION_VIEW,
            "hyuabot://cafeteria?tab=${meal.tab}".toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        views.setOnClickPendingIntent(
            R.id.widget_meal_title,
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        views.setPendingIntentTemplate(
            R.id.widget_cafeteria_list,
            PendingIntent.getActivity(
                context,
                1,
                launchIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        val refreshIntent = Intent(context, CafeteriaWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        views.setOnClickPendingIntent(
            R.id.widget_refresh,
            PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        return views
    }

    private fun buildItemView(context: Context, item: CafeteriaWidgetItem): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_cafeteria_item)
        views.setTextViewText(R.id.widget_item_name, item.name)
        if (item.runningTime.isNullOrBlank()) {
            views.setViewVisibility(R.id.widget_item_running_time, View.GONE)
        } else {
            views.setViewVisibility(R.id.widget_item_running_time, View.VISIBLE)
            views.setTextViewText(
                R.id.widget_item_running_time,
                context.getString(R.string.cafeteria_running_time_format, item.runningTime)
            )
        }

        views.removeAllViews(R.id.widget_menu_container)
        item.menus.forEach { menu ->
            val row = RemoteViews(context.packageName, R.layout.widget_cafeteria_menu_row)
            row.setTextViewText(R.id.widget_menu_food, menu.food)
            if (menu.price.isBlank()) {
                row.setViewVisibility(R.id.widget_menu_price, View.GONE)
            } else {
                row.setViewVisibility(R.id.widget_menu_price, View.VISIBLE)
                row.setTextViewText(
                    R.id.widget_menu_price,
                    context.getString(R.string.cafeteria_price_format, menu.price)
                )
            }
            views.addView(R.id.widget_menu_container, row)
        }

        views.setOnClickFillInIntent(R.id.widget_item_root, Intent())
        return views
    }

    private suspend fun loadItems(
        context: Context,
        meal: WidgetMeal,
        date: java.time.LocalDate,
    ): List<CafeteriaWidgetItem> {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            CafeteriaWidgetEntryPoint::class.java
        )
        val campusID = entryPoint.userPreferencesRepository().campusID.first()
        return try {
            val response = entryPoint.apolloClient()
                .query(CafeteriaPageQuery(date, campusID))
                .execute()
            val cafeteria = response.data?.cafeteria ?: return emptyList()
            cafeteria
                .filter { c -> c.menus.any { it.type.contains(meal.typeString) } }
                .sortedBy { it.seq }
                .mapNotNull { c ->
                    val menus = c.menus
                        .filter { it.type.contains(meal.typeString) }
                        .distinctBy { it.food }
                    if (menus.isEmpty()) return@mapNotNull null
                    val runningTime = when (meal) {
                        WidgetMeal.BREAKFAST, WidgetMeal.CLOSED -> c.runningTime.breakfast
                        WidgetMeal.LUNCH -> c.runningTime.lunch
                        WidgetMeal.DINNER -> c.runningTime.dinner
                    }
                    CafeteriaWidgetItem(
                        name = cafeteriaName(context, c.seq),
                        runningTime = runningTime,
                        menus = menus.map { CafeteriaWidgetMenu(it.food, formatPrice(it.price)) },
                    )
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun formatPrice(price: String): String = price.removeSuffix("원").trim()

    private fun cafeteriaName(context: Context, seq: Int): String = context.getString(
        when (seq) {
            1 -> R.string.cafeteria_1
            2 -> R.string.cafeteria_2
            4 -> R.string.cafeteria_4
            6 -> R.string.cafeteria_6
            7 -> R.string.cafeteria_7
            8 -> R.string.cafeteria_8
            11 -> R.string.cafeteria_11
            12 -> R.string.cafeteria_12
            13 -> R.string.cafeteria_13
            14 -> R.string.cafeteria_14
            15 -> R.string.cafeteria_15
            else -> R.string.cafeteria_1
        }
    )

    companion object {
        const val ACTION_REFRESH = "app.kobuggi.hyuabot.widget.ACTION_REFRESH_CAFETERIA"
    }
}

data class CafeteriaWidgetMenu(val food: String, val price: String)

data class CafeteriaWidgetItem(
    val name: String,
    val runningTime: String?,
    val menus: List<CafeteriaWidgetMenu>,
)
