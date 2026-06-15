package dev.aitexttranslator.redesign.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.aitexttranslator.AppSettings
import dev.aitexttranslator.TranslateIntentPayload
import dev.aitexttranslator.resolveTargetLanguageForText

/**
 * 主翻译页。
 *
 * 信息架构（自上而下，按用户视线优先级排）：
 *  1. 元信息头：方向 / 模型 / 配置状态 / 来源标签
 *  2. 原文输入
 *  3. 「翻译」主按钮
 *  4. 译文结果区（四态）
 *
 * 「复制 / 替换 / 重试」由外层放在底部操作栏（见 [TranslateActionRow]），
 * 这样滚动长译文时操作始终触手可及。
 */
@Composable
fun TranslateScreen(
    sourceText: String,
    onSourceTextChange: (String) -> Unit,
    status: TranslateStatus,
    settings: AppSettings,
    payload: TranslateIntentPayload,
    onTranslate: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val targetLanguage = resolveTargetLanguageForText(
            text = sourceText,
            defaultTargetLanguage = settings.targetLanguage,
        )

        SectionCard {
            MetaHeader(
                targetLanguage = targetLanguage,
                model = settings.model,
                configured = settings.isConfigured,
                sourceLabel = payload.source.label(),
                canReplace = payload.canReplaceSelection,
                onOpenSettings = onOpenSettings,
                showSettingsLink = settings.isConfigured,
            )
        }

        LabeledTextField(
            label = "原文",
            value = sourceText,
            onValueChange = onSourceTextChange,
            placeholder = "选中文字后从系统菜单调用，或在这里输入文本",
            minLines = 4,
            maxLines = 9,
        )

        PrimaryButton(
            text = "翻译",
            onClick = onTranslate,
            modifier = Modifier.fillMaxWidth(),
            enabled = sourceText.isNotBlank(),
            loading = status is TranslateStatus.Loading,
            loadingText = "翻译中",
        )

        ResultPanel(status = status, modifier = Modifier.fillMaxWidth())

        // 给底部操作栏留出空间。
        Spacer(Modifier.height(80.dp))
    }
}

/** Intent 来源 → 中文标签。 */
fun TranslateIntentPayload.Source.label(): String = when (this) {
    TranslateIntentPayload.Source.Launcher -> "手动输入"
    TranslateIntentPayload.Source.Translate -> "系统翻译"
    TranslateIntentPayload.Source.ProcessText -> "选中文本"
    TranslateIntentPayload.Source.Share -> "分享文本"
    TranslateIntentPayload.Source.Unknown -> "未知入口"
}
