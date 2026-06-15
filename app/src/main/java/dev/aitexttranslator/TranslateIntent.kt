package dev.aitexttranslator

import android.content.Intent
import android.os.Build

data class TranslateIntentPayload(
    val text: String,
    val canReplaceSelection: Boolean,
    val source: Source,
) {
    enum class Source {
        Launcher,
        Translate,
        ProcessText,
        Share,
        Unknown,
    }
}

fun Intent.extractTranslatePayload(): TranslateIntentPayload {
    val source = when (action) {
        Intent.ACTION_MAIN -> TranslateIntentPayload.Source.Launcher
        Intent.ACTION_TRANSLATE -> TranslateIntentPayload.Source.Translate
        Intent.ACTION_PROCESS_TEXT -> TranslateIntentPayload.Source.ProcessText
        Intent.ACTION_SEND -> TranslateIntentPayload.Source.Share
        else -> TranslateIntentPayload.Source.Unknown
    }

    val selectedText = when (action) {
        Intent.ACTION_TRANSLATE -> getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        Intent.ACTION_PROCESS_TEXT -> getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        Intent.ACTION_SEND -> getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        else -> getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
    }.orEmpty()

    val readOnly = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
    } else {
        true
    }

    return TranslateIntentPayload(
        text = selectedText.trim(),
        canReplaceSelection = action == Intent.ACTION_PROCESS_TEXT && !readOnly,
        source = source,
    )
}
