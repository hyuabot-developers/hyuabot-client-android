package app.kobuggi.hyuabot.service.translation

import android.content.res.Resources
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import app.kobuggi.hyuabot.R
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object DynamicTextTranslator {
    private const val TAG = "DynamicTextTranslator"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val translatedTextCache = ConcurrentHashMap<CacheKey, String>()
    private val translators = ConcurrentHashMap<TranslatorKey, Translator>()
    private val downloadConditions = DownloadConditions.Builder().build()

    fun bind(textView: TextView, text: String, loadingText: String? = null) {
        textView.setTag(R.id.dynamic_translation_source_text, text)
        textView.text = text
        val targetLanguage = targetLanguage(textView.resources) ?: return
        val sourceLanguage = sourceLanguage(text) ?: return
        if (sourceLanguage == targetLanguage) return

        val cacheKey = CacheKey(sourceLanguage, targetLanguage, text)
        translatedTextCache[cacheKey]?.let {
            if (textView.getTag(R.id.dynamic_translation_source_text) == text) {
                textView.text = it
            }
            return
        }

        val pendingText = loadingText ?: text
        if (loadingText != null) {
            textView.text = pendingText
        }

        scope.launch {
            runCatching { translate(text, sourceLanguage, targetLanguage) }
                .onSuccess { translated ->
                    translatedTextCache[cacheKey] = translated
                    if (textView.getTag(R.id.dynamic_translation_source_text) == text) {
                        textView.text = translated
                    }
                }
                .onFailure {
                    Log.w(TAG, "Failed to translate text from $sourceLanguage to $targetLanguage", it)
                    if (
                        textView.getTag(R.id.dynamic_translation_source_text) == text &&
                        textView.text.toString() == pendingText
                    ) {
                        textView.text = text
                    }
                }
        }
    }

    fun translateText(resources: Resources, text: String, onTranslated: (String) -> Unit) {
        val targetLanguage = targetLanguage(resources) ?: return
        val sourceLanguage = sourceLanguage(text) ?: return
        if (sourceLanguage == targetLanguage) return

        val cacheKey = CacheKey(sourceLanguage, targetLanguage, text)
        translatedTextCache[cacheKey]?.let(onTranslated) ?: scope.launch {
            runCatching { translate(text, sourceLanguage, targetLanguage) }
                .onSuccess { translated ->
                    translatedTextCache[cacheKey] = translated
                    onTranslated(translated)
                }
                .onFailure { Log.w(TAG, "Failed to translate text from $sourceLanguage to $targetLanguage", it) }
        }
    }

    fun cachedOrOriginal(resources: Resources, text: String): String {
        val targetLanguage = targetLanguage(resources) ?: return text
        val sourceLanguage = sourceLanguage(text) ?: return text
        if (sourceLanguage == targetLanguage) return text
        return translatedTextCache[CacheKey(sourceLanguage, targetLanguage, text)] ?: text
    }

    suspend fun translateForCurrentAppLocale(text: String): String {
        val targetLanguage = currentAppTargetLanguage() ?: return text
        val sourceLanguage = sourceLanguage(text) ?: return text
        if (sourceLanguage == targetLanguage) return text

        val cacheKey = CacheKey(sourceLanguage, targetLanguage, text)
        translatedTextCache[cacheKey]?.let { return it }

        return runCatching { translate(text, sourceLanguage, targetLanguage) }
            .onSuccess { translatedTextCache[cacheKey] = it }
            .onFailure { Log.w(TAG, "Failed to translate text from $sourceLanguage to $targetLanguage", it) }
            .getOrDefault(text)
    }

    fun currentAppLanguageTag(): String {
        val appLocale = AppCompatDelegate.getApplicationLocales().get(0)
        return appLocale?.toLanguageTag() ?: Locale.getDefault().toLanguageTag()
    }

    private suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): String {
        val translator = translator(sourceLanguage, targetLanguage)
        translator.downloadModelIfNeeded(downloadConditions).await()
        return translator.translate(text).await()
    }

    private fun translator(sourceLanguage: String, targetLanguage: String): Translator {
        val key = TranslatorKey(sourceLanguage, targetLanguage)
        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
            Translation.getClient(options)
        }
    }

    private fun targetLanguage(resources: Resources): String? {
        return when (resources.configuration.locales[0].language) {
            Locale.ENGLISH.language -> TranslateLanguage.ENGLISH
            Locale.JAPANESE.language -> TranslateLanguage.JAPANESE
            Locale.CHINESE.language -> TranslateLanguage.CHINESE
            else -> null
        }
    }

    private fun currentAppTargetLanguage(): String? {
        val appLocale = AppCompatDelegate.getApplicationLocales().get(0)
        return when (appLocale?.language ?: Locale.getDefault().language) {
            Locale.ENGLISH.language -> TranslateLanguage.ENGLISH
            Locale.JAPANESE.language -> TranslateLanguage.JAPANESE
            Locale.CHINESE.language -> TranslateLanguage.CHINESE
            else -> null
        }
    }

    private fun sourceLanguage(text: String): String? {
        if (text.isBlank()) return null
        return if (text.any { it in '\uAC00'..'\uD7A3' }) {
            TranslateLanguage.KOREAN
        } else if (text.any { it.isLetter() }) {
            TranslateLanguage.ENGLISH
        } else {
            null
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
        addOnCanceledListener { continuation.cancel() }
    }

    private data class CacheKey(val sourceLanguage: String, val targetLanguage: String, val text: String)
    private data class TranslatorKey(val sourceLanguage: String, val targetLanguage: String)
}
