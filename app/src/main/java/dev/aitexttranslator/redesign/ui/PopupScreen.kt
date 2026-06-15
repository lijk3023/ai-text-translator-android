package dev.aitexttranslator.redesign.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.aitexttranslator.AppSettings
import dev.aitexttranslator.TranslateIntentPayload
import dev.aitexttranslator.resolveTargetLanguageForText
import dev.aitexttranslator.redesign.theme.TranslatorTheme

/**
 * 选中文字弹窗页：紧凑、快速、少滚动（参考 Raycast 的命令面板气质）。
 * 一张卡片塞下「头 + 原文 + 译文 + 操作」，外层是半透明遮罩。
 */
@Composable
fun PopupScreen(
    sourceText: String,
    onSourceTextChange: (String) -> Unit,
    status: TranslateStatus,
    settings: AppSettings,
    payload: TranslateIntentPayload,
    onCopy: () -> Unit,
    onReplace: () -> Unit,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TranslatorTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.hairline, RoundedCornerShape(20.dp))
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val targetLanguage = resolveTargetLanguageForText(
                text = sourceText,
                defaultTargetLanguage = settings.targetLanguage,
            )

            MetaHeader(
                targetLanguage = targetLanguage,
                model = settings.model,
                configured = settings.isConfigured,
                sourceLabel = payload.source.label(),
                canReplace = payload.canReplaceSelection,
                onOpenSettings = onOpenSettings,
                // 弹窗里只在未配置时给「去设置」入口，配置好就保持紧凑。
                showSettingsLink = !settings.isConfigured,
            )

            LabeledTextField(
                label = "原文",
                value = sourceText,
                onValueChange = onSourceTextChange,
                placeholder = "选中的文本",
                minLines = 2,
                maxLines = 4,
            )

            ResultPanel(status = status, modifier = Modifier.fillMaxWidth())

            TranslateActionRow(
                status = status,
                canReplaceSelection = payload.canReplaceSelection,
                onCopy = onCopy,
                onReplace = onReplace,
                onRetry = onRetry,
            )
        }
    }
}
