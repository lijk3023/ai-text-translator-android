package dev.aitexttranslator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguageDetectionTest {
    @Test
    fun detectsChineseSentence() {
        assertTrue(looksLikeChineseText("你好，你是谁？"))
        assertEquals(
            "English",
            resolveTargetLanguageForText("你好，你是谁？", defaultTargetLanguage = "中文"),
        )
    }

    @Test
    fun doesNotTreatJapaneseKanaMixedTextAsChinese() {
        assertFalse(looksLikeChineseText("こんにちは、あなたは誰ですか？"))
        assertFalse(looksLikeChineseText("今日はいい天気ですね"))
        assertEquals(
            "中文",
            resolveTargetLanguageForText("今日はいい天気ですね", defaultTargetLanguage = "中文"),
        )
    }

    @Test
    fun keepsConfiguredTargetForEnglishText() {
        assertFalse(looksLikeChineseText("Hello, who are you?"))
        assertEquals(
            "中文",
            resolveTargetLanguageForText("Hello, who are you?", defaultTargetLanguage = "中文"),
        )
    }

    @Test
    fun toleratesMixedChineseAndEnglishText() {
        assertTrue(looksLikeChineseText("请 review 这段 Kotlin code"))
    }
}
