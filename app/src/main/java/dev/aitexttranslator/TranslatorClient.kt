package dev.aitexttranslator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class TranslatorClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    suspend fun translate(
        text: String,
        settings: AppSettings,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(text.isNotBlank()) { "没有可翻译的文本" }
            require(settings.isConfigured) { "请先在设置里填写 API Key、Base URL 和模型" }
            val targetLanguage = resolveTargetLanguageForText(
                text = text,
                defaultTargetLanguage = settings.targetLanguage,
            )

            val body = JSONObject()
                .put("model", settings.model)
                .put(
                    "messages",
                    JSONArray()
                        .put(
                            JSONObject()
                                .put("role", "system")
                                .put(
                                    "content",
                                    settings.promptTemplate.replace(
                                        "{{target_language}}",
                                        targetLanguage
                                    )
                                )
                        )
                        .put(JSONObject().put("role", "user").put("content", text))
                )
                .put("temperature", 0.2)
                .put("stream", false)
                .toString()

            val request = Request.Builder()
                .url("${settings.baseUrl.trimEnd('/')}/chat/completions")
                .addHeader("Authorization", "Bearer ${settings.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = parseErrorMessage(responseText)
                    throw IOException("HTTP ${response.code}: $message")
                }
                parseTranslation(responseText)
            }
        }
    }

    private fun parseTranslation(responseText: String): String {
        val json = JSONObject(responseText)
        val choices = json.optJSONArray("choices") ?: JSONArray()
        val first = choices.optJSONObject(0)
        val message = first?.optJSONObject("message")
        val content = message?.optString("content").orEmpty().trim()
        if (content.isBlank()) {
            throw IOException("接口返回为空")
        }
        return content
    }

    private fun parseErrorMessage(responseText: String): String {
        if (responseText.isBlank()) return "接口没有返回错误正文"
        return runCatching {
            JSONObject(responseText)
                .optJSONObject("error")
                ?.optString("message")
                ?.takeIf { it.isNotBlank() }
                ?: responseText.take(400)
        }.getOrDefault(responseText.take(400))
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
