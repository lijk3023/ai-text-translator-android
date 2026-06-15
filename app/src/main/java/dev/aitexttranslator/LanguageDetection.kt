package dev.aitexttranslator

fun resolveTargetLanguageForText(
    text: String,
    defaultTargetLanguage: String,
): String {
    return if (looksLikeChineseText(text)) {
        "English"
    } else {
        defaultTargetLanguage
    }
}

fun looksLikeChineseText(text: String): Boolean {
    val counts = text.scriptSignalCounts()
    if (counts.han < MIN_HAN_FOR_CHINESE) return false

    val cjkSignals = counts.han + counts.kana
    if (cjkSignals <= 0) return false

    val kanaRatio = counts.kana.toFloat() / cjkSignals
    if (counts.kana >= MIN_KANA_FOR_JAPANESE && kanaRatio >= JAPANESE_KANA_CJK_RATIO) {
        return false
    }

    val hanCjkRatio = counts.han.toFloat() / cjkSignals
    return hanCjkRatio >= CHINESE_HAN_CJK_RATIO
}

private data class ScriptSignalCounts(
    val han: Int = 0,
    val kana: Int = 0,
    val latin: Int = 0,
    val hangul: Int = 0,
)

private fun String.scriptSignalCounts(): ScriptSignalCounts {
    var han = 0
    var kana = 0
    var latin = 0
    var hangul = 0

    var index = 0
    while (index < length) {
        val codePoint = Character.codePointAt(this, index)
        when {
            codePoint.isHanCodePoint() -> han += 1
            codePoint.isKanaCodePoint() -> kana += 1
            codePoint.isLatinLetterCodePoint() -> latin += 1
            codePoint.isHangulCodePoint() -> hangul += 1
        }
        index += Character.charCount(codePoint)
    }

    return ScriptSignalCounts(
        han = han,
        kana = kana,
        latin = latin,
        hangul = hangul,
    )
}

private fun Int.isHanCodePoint(): Boolean {
    return this in 0x3400..0x4DBF ||
        this in 0x4E00..0x9FFF ||
        this in 0xF900..0xFAFF ||
        this in 0x20000..0x2A6DF ||
        this in 0x2A700..0x2B73F ||
        this in 0x2B740..0x2B81F ||
        this in 0x2B820..0x2CEAF ||
        this in 0x2CEB0..0x2EBEF ||
        this in 0x30000..0x3134F
}

private fun Int.isKanaCodePoint(): Boolean {
    return this in 0x3040..0x309F ||
        this in 0x30A0..0x30FF ||
        this in 0x31F0..0x31FF ||
        this in 0x1B000..0x1B16F
}

private fun Int.isLatinLetterCodePoint(): Boolean {
    return this in 0x0041..0x005A ||
        this in 0x0061..0x007A ||
        this in 0x00C0..0x024F
}

private fun Int.isHangulCodePoint(): Boolean {
    return this in 0x1100..0x11FF ||
        this in 0x3130..0x318F ||
        this in 0xA960..0xA97F ||
        this in 0xAC00..0xD7AF ||
        this in 0xD7B0..0xD7FF
}

private const val MIN_HAN_FOR_CHINESE = 2
private const val MIN_KANA_FOR_JAPANESE = 2
private const val JAPANESE_KANA_CJK_RATIO = 0.12f
private const val CHINESE_HAN_CJK_RATIO = 0.72f
