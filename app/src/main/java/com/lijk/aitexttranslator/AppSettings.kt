package com.lijk.aitexttranslator

data class AppSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4o-mini",
    val targetLanguage: String = "中文",
    val promptTemplate: String = DEFAULT_PROMPT_TEMPLATE,
    val streamOutput: Boolean = false,
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()
}

const val DEFAULT_PROMPT_TEMPLATE =
    "Translate the selected text into {{target_language}}. Keep terminology accurate, preserve line breaks, and return only the translation."
