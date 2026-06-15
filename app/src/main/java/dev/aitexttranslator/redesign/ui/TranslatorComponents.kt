package dev.aitexttranslator.redesign.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aitexttranslator.redesign.theme.TranslatorTheme

/* ------------------------------------------------------------------ */
/* 按钮                                                                */
/* ------------------------------------------------------------------ */

/** 主操作按钮：实心强调色。支持 loading 态（左侧转圈 + 文案切换）。 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String = text,
) {
    val colors = TranslatorTheme.colors
    val clickable = enabled && !loading
    val alpha by animateFloatAsState(if (clickable) 1f else 0.45f, label = "primaryAlpha")
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.accent)
            .alpha(alpha)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.onAccent,
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = if (loading) loadingText else text,
            color = colors.onAccent,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/**
 * 次级操作按钮，用于「复制 / 替换 / 重试」一排。
 * [tone] 决定是实心强调（重试）还是柔和表面（复制/替换）。
 */
enum class ActionTone { Accent, Neutral }

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ActionTone = ActionTone.Neutral,
) {
    val colors = TranslatorTheme.colors
    val container = when (tone) {
        ActionTone.Accent -> colors.accent
        ActionTone.Neutral -> colors.surfaceMuted
    }
    val content = when (tone) {
        ActionTone.Accent -> colors.onAccent
        ActionTone.Neutral -> colors.ink
    }
    val alpha by animateFloatAsState(if (enabled) 1f else 0.4f, label = "actionAlpha")
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .alpha(alpha)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = content, style = MaterialTheme.typography.labelLarge)
    }
}

/** 纯文字按钮（如「去设置」）。 */
@Composable
fun TextLinkButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TranslatorTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text = text, color = colors.accentText, style = MaterialTheme.typography.labelLarge)
    }
}

/* ------------------------------------------------------------------ */
/* 分段控件 / 标识                                                      */
/* ------------------------------------------------------------------ */

/** 顶部「翻译 / 设置」分段切换。 */
@Composable
fun SegmentedControl(
    selected: TranslatorScreen,
    onSelected: (TranslatorScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TranslatorTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceMuted)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SegmentItem("翻译", selected == TranslatorScreen.Translate) { onSelected(TranslatorScreen.Translate) }
        SegmentItem("设置", selected == TranslatorScreen.Settings) { onSelected(TranslatorScreen.Settings) }
    }
}

@Composable
private fun SegmentItem(text: String, active: Boolean, onClick: () -> Unit) {
    val colors = TranslatorTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            color = if (active) colors.ink else colors.inkMuted,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

/** 配置状态胶囊：Ready / Setup。 */
@Composable
fun StatusPill(configured: Boolean) {
    val colors = TranslatorTheme.colors
    val bg = if (configured) colors.successSoft else colors.dangerSoft
    val dot = if (configured) colors.success else colors.danger
    val fg = if (configured) colors.onSuccessSoft else colors.onDangerSoft
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (configured) "就绪" else "待配置",
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** 信息小标签（来源 / 可替换原文 等）。 */
@Composable
fun InfoChip(text: String) {
    val colors = TranslatorTheme.colors
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceMuted)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = colors.inkMuted,
        style = MaterialTheme.typography.labelMedium,
    )
}

/* ------------------------------------------------------------------ */
/* 容器 / 表单                                                          */
/* ------------------------------------------------------------------ */

/** 通用卡片：surface 底 + 发丝边框，无阴影。 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    padding: Int = 18,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TranslatorTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.hairline, RoundedCornerShape(18.dp))
            .padding(padding.dp),
        content = content,
    )
}

/** 卡片小标题（如「API」「翻译」）。 */
@Composable
fun CardLabel(text: String) {
    Text(
        text = text,
        color = TranslatorTheme.colors.inkSubtle,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    secret: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (minLines > 1) minLines + 4 else 1,
) {
    val colors = TranslatorTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = minLines == 1 && maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceMuted,
            unfocusedContainerColor = colors.surfaceMuted,
            disabledContainerColor = colors.surfaceMuted,
            focusedIndicatorColor = colors.accent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor = colors.accent,
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
            focusedLabelColor = colors.accentText,
            unfocusedLabelColor = colors.inkSubtle,
            focusedPlaceholderColor = colors.inkSubtle,
            unfocusedPlaceholderColor = colors.inkSubtle,
        ),
    )
}

/** 设置页里的开关行。 */
@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = TranslatorTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.ink, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, color = colors.inkMuted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.surface,
                uncheckedTrackColor = colors.surfaceMuted,
                uncheckedBorderColor = colors.hairline,
            ),
        )
    }
}

/* ------------------------------------------------------------------ */
/* 元信息头 + 译文结果区                                                */
/* ------------------------------------------------------------------ */

/** 翻译页 / 弹窗页共用的元信息头：方向、模型、状态、来源标签。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetaHeader(
    targetLanguage: String,
    model: String,
    configured: Boolean,
    sourceLabel: String,
    canReplace: Boolean,
    onOpenSettings: () -> Unit,
    showSettingsLink: Boolean,
) {
    val colors = TranslatorTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "自动检测  →  $targetLanguage",
                    color = colors.ink,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (configured) model.ifBlank { "未配置模型" } else "需要先配置 API",
                    color = colors.inkMuted,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showSettingsLink) {
                    TextLinkButton(text = if (configured) "设置" else "去设置", onClick = onOpenSettings)
                    Spacer(Modifier.width(4.dp))
                }
                StatusPill(configured)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoChip(sourceLabel)
            if (canReplace) InfoChip("可替换原文")
        }
    }
}

/** 译文结果区：四态切换（空 / 加载骨架 / 成功可选中 / 错误）。 */
@Composable
fun ResultPanel(status: TranslateStatus, modifier: Modifier = Modifier) {
    val colors = TranslatorTheme.colors
    SectionCard(modifier = modifier) {
        Text(
            text = "译文",
            color = colors.inkSubtle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        AnimatedContent(
            targetState = status,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "result",
        ) { state ->
            when (state) {
                TranslateStatus.Idle -> Text(
                    text = "翻译结果会显示在这里",
                    color = colors.inkSubtle,
                    style = MaterialTheme.typography.bodyLarge,
                )

                TranslateStatus.Loading -> LoadingSkeleton()

                is TranslateStatus.Success -> SelectionContainer {
                    // 阅读区：更大字号 + 高行高，长文也舒服。
                    Text(
                        text = state.text,
                        color = colors.ink,
                        fontSize = 19.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                is TranslateStatus.Error -> Text(
                    text = state.message,
                    color = colors.danger,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

@Composable
private fun LoadingSkeleton() {
    val colors = TranslatorTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == 3) 0.6f else 1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(colors.surfaceMuted),
            )
        }
    }
}

/** 「复制 / 替换 / 重试」操作排。 */
@Composable
fun TranslateActionRow(
    status: TranslateStatus,
    canReplaceSelection: Boolean,
    onCopy: () -> Unit,
    onReplace: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasResult = status is TranslateStatus.Success
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton("复制", onCopy, Modifier.weight(1f), enabled = hasResult)
        ActionButton("替换", onReplace, Modifier.weight(1f), enabled = hasResult && canReplaceSelection)
        ActionButton(
            "重试",
            onRetry,
            Modifier.weight(1f),
            enabled = status !is TranslateStatus.Loading,
            tone = ActionTone.Accent,
        )
    }
}
