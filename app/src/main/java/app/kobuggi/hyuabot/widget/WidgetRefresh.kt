package app.kobuggi.hyuabot.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

fun refreshHyuabotWidgets(context: Context) {
    val appContext = context.applicationContext
    val appWidgetManager = AppWidgetManager.getInstance(appContext)
    val providers = listOf(CafeteriaWidgetProvider::class.java) + ShuttleWidgetProvider.providerClasses
    providers.forEach { provider ->
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(appContext, provider))
        if (ids.isNotEmpty()) {
            appContext.sendBroadcast(
                Intent(appContext, provider).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }
    }
}
