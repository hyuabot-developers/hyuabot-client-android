package app.kobuggi.hyuabot.service

import android.content.Context
import app.kobuggi.hyuabot.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Singleton
class ShuttlePresenceService @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()
    private val sessionId = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).let { preferences ->
        preferences.getString(SESSION_ID_KEY, null) ?: UUID.randomUUID().toString().lowercase().also {
            preferences.edit().putString(SESSION_ID_KEY, it).apply()
        }
    }

    suspend fun heartbeat(stopId: String): Int? = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject()
                .put("stopId", stopId)
                .put("sessionId", sessionId)
                .put("platform", "android")
                .put("appVersion", BuildConfig.VERSION_NAME)
                .toString()
                .toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("${BuildConfig.API_URL}/api/v1/presence/shuttle")
                .post(payload)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val json = response.body.string().let(::JSONObject)
                if (!json.optBoolean("visible", false) || json.isNull("viewerCount")) return@use null
                json.optInt("viewerCount")
            }
        }.getOrNull()
    }

    private companion object {
        const val PREFERENCES_NAME = "shuttle_presence"
        const val SESSION_ID_KEY = "anonymous_installation_id"
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
