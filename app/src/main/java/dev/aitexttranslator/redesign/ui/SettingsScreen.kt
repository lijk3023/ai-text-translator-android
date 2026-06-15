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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.aitexttranslator.AppSettings
import dev.aitexttranslator.DEFAULT_PROMPT_TEMPLATE

/**
 * 设置页：清晰的配置表单，按「连接」和「翻译」两组分卡。
 * 草稿态本地编辑，点「保存设置」整体提交。
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(settings) { mutableStateOf(settings) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionCard {
            CardLabel("连接")
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LabeledTextField(
                    label = "Base URL",
                    value = draft.baseUrl,
                    onValueChange = { draft = draft.copy(baseUrl = it) },
                    placeholder = "https://api.openai.com/v1",
                )
                LabeledTextField(
                    label = "API Key",
                    value = draft.apiKey,
                    onValueChange = { draft = draft.copy(apiKey = it) },
                    placeholder = "sk-...",
                    secret = true,
                )
                LabeledTextField(
                    label = "Model",
                    value = draft.model,
                    onValueChange = { draft = draft.copy(model = it) },
                    placeholder = "gpt-4o-mini",
                )
            }
        }

        SectionCard {
            CardLabel("翻译")
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LabeledTextField(
                    label = "目标语言",
                    value = draft.targetLanguage,
                    onValueChange = { draft = draft.copy(targetLanguage = it) },
                    placeholder = "中文",
                )
                LabeledTextField(
                    label = "Prompt 模板",
                    value = draft.promptTemplate,
                    onValueChange = { draft = draft.copy(promptTemplate = it) },
                    placeholder = DEFAULT_PROMPT_TEMPLATE,
                    minLines = 4,
                )
                SwitchRow(
                    title = "流式输出",
                    subtitle = "预留开关，当前接口按非流式请求",
                    checked = draft.streamOutput,
                    onCheckedChange = { draft = draft.copy(streamOutput = it) },
                )
            }
        }

        PrimaryButton(
            text = "保存设置",
            onClick = { onSaveSettings(draft) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))
    }
}
