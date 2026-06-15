package dev.aitexttranslator.redesign.ui

/** 顶层页面切换。 */
enum class TranslatorScreen { Translate, Settings }

/** 翻译请求的四个状态。UI 层只关心这四种，业务结果由外部 [Result] 折叠进来。 */
sealed interface TranslateStatus {
    data object Idle : TranslateStatus
    data object Loading : TranslateStatus
    data class Success(val text: String) : TranslateStatus
    data class Error(val message: String) : TranslateStatus
}

val TranslateStatus.successText: String
    get() = (this as? TranslateStatus.Success)?.text.orEmpty()
