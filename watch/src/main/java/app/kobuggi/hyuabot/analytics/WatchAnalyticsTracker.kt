package app.kobuggi.hyuabot.analytics

import android.content.Context
import app.kobuggi.hyuabot.BuildConfig
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID

class WatchAnalyticsTracker(
    context: Context,
    private val client: OkHttpClient = OkHttpClient(),
) {
    enum class EntryPoint(val value: String) {
        APP("app"),
        TILE("tile"),
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val installationId =
        preferences.getString(INSTALLATION_ID_KEY, null) ?: UUID.randomUUID().toString().also {
            preferences.edit().putString(INSTALLATION_ID_KEY, it).apply()
        }

    fun trackAppOpen(entryPoint: EntryPoint) {
        send(
            WatchAnalyticsEvent.appOpen(
                installationId = installationId,
                appVersion = BuildConfig.VERSION_NAME,
                entryPoint = entryPoint.value,
            ),
        )
    }

    fun trackStopSelected(stopId: String, entryPoint: EntryPoint) {
        send(
            WatchAnalyticsEvent.stopSelected(
                installationId = installationId,
                appVersion = BuildConfig.VERSION_NAME,
                entryPoint = entryPoint.value,
                stopId = stopId,
            ),
        )
    }

    private fun send(event: WatchAnalyticsEvent) {
        val request =
            Request.Builder()
                .url("${BuildConfig.API_URL}/api/v1/analytics/watch/events")
                .post(GSON.toJson(event).toRequestBody(JSON_MEDIA_TYPE))
                .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) = Unit

                override fun onResponse(call: Call, response: Response) {
                    response.close()
                }
            },
        )
    }

    companion object {
        private const val PREFERENCES_NAME = "watch_analytics"
        private const val INSTALLATION_ID_KEY = "installation_id"
        private val GSON = Gson()
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
