package dev.aitexttranslator

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "translator_settings")

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            baseUrl = preferences[BASE_URL] ?: "https://api.openai.com/v1",
            apiKey = preferences[API_KEY] ?: "",
            model = preferences[MODEL] ?: "gpt-4o-mini",
            targetLanguage = preferences[TARGET_LANGUAGE] ?: "中文",
            promptTemplate = normalizePromptTemplate(preferences[PROMPT_TEMPLATE]),
            streamOutput = preferences[STREAM_OUTPUT] ?: false,
        )
    }

    suspend fun save(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[BASE_URL] = settings.baseUrl.trim().trimEnd('/')
            preferences[API_KEY] = settings.apiKey.trim()
            preferences[MODEL] = settings.model.trim()
            preferences[TARGET_LANGUAGE] = settings.targetLanguage.trim().ifBlank { "中文" }
            preferences[PROMPT_TEMPLATE] = settings.promptTemplate.trim().ifBlank {
                DEFAULT_PROMPT_TEMPLATE
            }
            preferences[STREAM_OUTPUT] = settings.streamOutput
        }
    }

    private fun normalizePromptTemplate(savedPrompt: String?): String {
        val prompt = savedPrompt?.trim().orEmpty()
        return when {
            prompt.isBlank() -> DEFAULT_PROMPT_TEMPLATE
            prompt == LEGACY_DEFAULT_PROMPT_TEMPLATE -> DEFAULT_PROMPT_TEMPLATE
            else -> prompt
        }
    }

    private companion object {
        val BASE_URL = stringPreferencesKey("base_url")
        val API_KEY = stringPreferencesKey("api_key")
        val MODEL = stringPreferencesKey("model")
        val TARGET_LANGUAGE = stringPreferencesKey("target_language")
        val PROMPT_TEMPLATE = stringPreferencesKey("prompt_template")
        val STREAM_OUTPUT = booleanPreferencesKey("stream_output")
    }
}
