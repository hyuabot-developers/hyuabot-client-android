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
import org.json.JSONArray
import org.json.JSONObject

data class InquiryThread(
    val id: String,
    val status: String,
    val subject: String?,
    val entryScreen: String?,
    val entryScreenName: String?,
    val lastMessageAt: String?,
    val createdAt: String?,
)

data class InquiryMessage(
    val id: Long,
    val senderType: String,
    val body: String,
    val readAt: String?,
    val createdAt: String?,
)

@Singleton
class InquiryService @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()
    private val installationId = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).let { preferences ->
        preferences.getString(SESSION_ID_KEY, null) ?: UUID.randomUUID().toString().lowercase().also {
            preferences.edit().putString(SESSION_ID_KEY, it).apply()
        }
    }

    private fun Request.Builder.withCommonHeaders(): Request.Builder = this
        .header("X-Installation-Id", installationId)
        .header("X-App-Platform", "android")
        .header("X-App-Version", BuildConfig.VERSION_NAME)
        .header("Content-Type", "application/json")

    private fun parseThread(json: JSONObject): InquiryThread = InquiryThread(
        id = json.getString("id"),
        status = json.optString("status"),
        subject = json.optStringOrNull("subject"),
        entryScreen = json.optStringOrNull("entryScreen"),
        entryScreenName = json.optStringOrNull("entryScreenName"),
        lastMessageAt = json.optStringOrNull("lastMessageAt"),
        createdAt = json.optStringOrNull("createdAt"),
    )

    private fun parseMessage(json: JSONObject): InquiryMessage = InquiryMessage(
        id = json.getLong("id"),
        senderType = json.optString("senderType"),
        body = json.optString("body"),
        readAt = json.optStringOrNull("readAt"),
        createdAt = json.optStringOrNull("createdAt"),
    )

    suspend fun openThread(
        subject: String?,
        entryScreen: String?,
        entryScreenName: String?,
    ): InquiryThread? = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject()
                .put("subject", subject ?: JSONObject.NULL)
                .put("entryScreen", entryScreen ?: JSONObject.NULL)
                .put("entryScreenName", entryScreenName ?: JSONObject.NULL)
                .toString()
                .toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("${BuildConfig.API_URL}/api/v1/inquiry/threads")
                .withCommonHeaders()
                .post(payload)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val json = response.body.string().let(::JSONObject)
                parseThread(json.optJSONObject("result") ?: json)
            }
        }.getOrNull()
    }

    suspend fun activeThread(): InquiryThread? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("${BuildConfig.API_URL}/api/v1/inquiry/threads/me")
                .withCommonHeaders()
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (response.code == 204 || !response.isSuccessful) return@use null
                val json = response.body.string().let(::JSONObject)
                parseThread(json.optJSONObject("result") ?: json)
            }
        }.getOrNull()
    }

    suspend fun messages(threadId: String): List<InquiryMessage> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("${BuildConfig.API_URL}/api/v1/inquiry/threads/$threadId/messages")
                .withCommonHeaders()
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList()
                val json = response.body.string().let(::JSONObject)
                val array = json.optJSONArray("result") ?: JSONArray()
                (0 until array.length()).map { parseMessage(array.getJSONObject(it)) }
            }
        }.getOrElse { emptyList() }
    }

    suspend fun send(threadId: String, body: String): InquiryMessage? = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject()
                .put("body", body)
                .toString()
                .toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("${BuildConfig.API_URL}/api/v1/inquiry/threads/$threadId/messages")
                .withCommonHeaders()
                .post(payload)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val json = response.body.string().let(::JSONObject)
                parseMessage(json.optJSONObject("result") ?: json)
            }
        }.getOrNull()
    }

    suspend fun markRead(threadId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = "{}".toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url("${BuildConfig.API_URL}/api/v1/inquiry/threads/$threadId/read")
                    .withCommonHeaders()
                    .post(payload)
                    .build()
                client.newCall(request).execute().use { }
            }
        }
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key) || !has(key)) null else optString(key)

    private companion object {
        const val PREFERENCES_NAME = "shuttle_presence"
        const val SESSION_ID_KEY = "anonymous_installation_id"
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
