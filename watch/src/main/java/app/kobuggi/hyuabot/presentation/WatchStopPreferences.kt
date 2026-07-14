package app.kobuggi.hyuabot.presentation

import android.content.Context

class WatchStopPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var recentStopId: String?
        get() = preferences.getString(KEY_RECENT_STOP_ID, null)
        set(value) {
            preferences.edit().putString(KEY_RECENT_STOP_ID, value).apply()
        }

    private companion object {
        const val FILE_NAME = "watch_stop_preferences"
        const val KEY_RECENT_STOP_ID = "recent_stop_id"
    }
}
