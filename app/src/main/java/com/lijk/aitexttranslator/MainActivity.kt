package com.lijk.aitexttranslator

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

open class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository
    private val translatorClient = TranslatorClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)
        val popupEntry = this is SelectionTranslateActivity
        val startScreen = when (intent.getStringExtra(EXTRA_START_SCREEN)) {
            Screen.Settings.name -> Screen.Settings
            else -> Screen.Translate
        }
        if (popupEntry) {
            setFinishOnTouchOutside(true)
            window.setGravity(Gravity.CENTER)
            window.setDimAmount(0.28f)
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.88f).toInt(),
                (resources.displayMetrics.heightPixels * 0.68f).toInt(),
            )
        } else {
            enableEdgeToEdge()
        }
        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = AppSettings())
            val initialPayload = remember(intent) { intent.extractTranslatePayload() }
            AiTextTranslatorTheme {
                if (popupEntry) {
                    PopupTranslatorApp(
                        initialPayload = initialPayload,
                        settings = settings,
                        onTranslate = { text, activeSettings ->
                            translatorClient.translate(text, activeSettings)
                        },
                        onReplaceSelection = { translatedText ->
                            setResult(
                                Activity.RESULT_OK,
                                Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, translatedText)
                            )
                            finish()
                        },
                        onOpenSettings = { openMainSettings() },
                    )
                } else {
                    TranslatorApp(
                        initialPayload = initialPayload,
                        settings = settings,
                        initialScreen = startScreen,
                        onSaveSettings = { newSettings ->
                            settingsRepository.save(newSettings)
                        },
                        onTranslate = { text, activeSettings ->
                            translatorClient.translate(text, activeSettings)
                        },
                        onReplaceSelection = { translatedText ->
                            setResult(
                                Activity.RESULT_OK,
                                Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, translatedText)
                            )
                            finish()
                        },
                    )
                }
            }
        }
    }
}

private enum class Screen {
    Translate,
    Settings,
}

private sealed interface TranslateStatus {
    data object Idle : TranslateStatus
    data object Loading : TranslateStatus
    data class Success(val text: String) : TranslateStatus
    data class Error(val message: String) : TranslateStatus
}

@Composable
private fun TranslatorApp(
    initialPayload: TranslateIntentPayload,
    settings: AppSettings,
    initialScreen: Screen,
    onSaveSettings: suspend (AppSettings) -> Unit,
    onTranslate: suspend (String, AppSettings) -> Result<String>,
    onReplaceSelection: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(initialScreen) }
    var sourceText by rememberSaveable { mutableStateOf(initialPayload.text) }
    var status by remember { mutableStateOf<TranslateStatus>(TranslateStatus.Idle) }

    fun runTranslation() {
        val text = sourceText.trim()
        if (text.isBlank()) {
            status = TranslateStatus.Error("没有可翻译的文本")
            return
        }
        scope.launch {
            status = TranslateStatus.Loading
            status = onTranslate(text, settings).fold(
                onSuccess = { TranslateStatus.Success(it) },
                onFailure = { TranslateStatus.Error(it.message ?: "翻译失败") },
            )
        }
    }

    LaunchedEffect(initialPayload.text, settings.isConfigured) {
        if (initialPayload.text.isNotBlank() && settings.isConfigured) {
            runTranslation()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(IosBackground),
        containerColor = IosBackground,
        topBar = {
            TranslatorTopBar(
                screen = screen,
                onScreenChange = { screen = it },
            )
        },
        bottomBar = {
            if (screen == Screen.Translate) {
                TranslateBottomBar(
                    status = status,
                    canReplaceSelection = initialPayload.canReplaceSelection,
                    onCopy = {
                        val translated = (status as? TranslateStatus.Success)?.text.orEmpty()
                        copyToClipboard(context, translated)
                    },
                    onReplace = {
                        val translated = (status as? TranslateStatus.Success)?.text.orEmpty()
                        onReplaceSelection(translated)
                    },
                    onRetry = { runTranslation() },
                )
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = screen,
            label = "screen",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { target ->
            when (target) {
                Screen.Translate -> TranslateScreen(
                    sourceText = sourceText,
                    onSourceTextChange = { sourceText = it },
                    status = status,
                    settings = settings,
                    payload = initialPayload,
                    popupMode = false,
                    onTranslate = { runTranslation() },
                    onOpenSettings = { screen = Screen.Settings },
                )

                Screen.Settings -> SettingsScreen(
                    settings = settings,
                    onSaveSettings = { newSettings ->
                        scope.launch {
                            onSaveSettings(newSettings)
                            Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                            screen = Screen.Translate
                        }
                    },
                )
            }
        }
    }
}

class SelectionTranslateActivity : MainActivity()

@Composable
private fun PopupTranslatorApp(
    initialPayload: TranslateIntentPayload,
    settings: AppSettings,
    onTranslate: suspend (String, AppSettings) -> Result<String>,
    onReplaceSelection: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sourceText by rememberSaveable(initialPayload.text) { mutableStateOf(initialPayload.text) }
    var status by remember { mutableStateOf<TranslateStatus>(TranslateStatus.Idle) }

    fun runTranslation() {
        val text = sourceText.trim()
        if (text.isBlank()) {
            status = TranslateStatus.Error("没有可翻译的文本")
            return
        }
        scope.launch {
            status = TranslateStatus.Loading
            status = onTranslate(text, settings).fold(
                onSuccess = { TranslateStatus.Success(it) },
                onFailure = { TranslateStatus.Error(it.message ?: "翻译失败") },
            )
        }
    }

    LaunchedEffect(initialPayload.text, settings.isConfigured) {
        if (initialPayload.text.isNotBlank() && settings.isConfigured) {
            runTranslation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PopupHeader(
                    settings = settings,
                    payload = initialPayload,
                    onOpenSettings = onOpenSettings,
                )
                SourceAndResult(
                    sourceText = sourceText,
                    onSourceTextChange = { sourceText = it },
                    status = status,
                    onTranslate = { runTranslation() },
                    popupMode = true,
                )
                PopupActions(
                    status = status,
                    canReplaceSelection = initialPayload.canReplaceSelection,
                    onCopy = {
                        val translated = (status as? TranslateStatus.Success)?.text.orEmpty()
                        copyToClipboard(context, translated)
                    },
                    onReplace = {
                        val translated = (status as? TranslateStatus.Success)?.text.orEmpty()
                        onReplaceSelection(translated)
                    },
                    onRetry = { runTranslation() },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopupHeader(
    settings: AppSettings,
    payload: TranslateIntentPayload,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "翻译为 ${settings.targetLanguage}",
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (settings.isConfigured) settings.model else "需要先配置 API",
                    color = SecondaryInk,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!settings.isConfigured) {
                TextButton(onClick = onOpenSettings) {
                    Text("去设置")
                }
            }
            StatusPill(configured = settings.isConfigured)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoChip(text = payload.source.displayName())
            if (payload.canReplaceSelection) {
                InfoChip(text = "可替换原文")
            }
        }
    }
}

@Composable
private fun PopupActions(
    status: TranslateStatus,
    canReplaceSelection: Boolean,
    onCopy: () -> Unit,
    onReplace: () -> Unit,
    onRetry: () -> Unit,
) {
    val hasResult = status is TranslateStatus.Success
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomAction(
            label = "复制",
            enabled = hasResult,
            onClick = onCopy,
            modifier = Modifier.weight(1f),
        )
        BottomAction(
            label = "替换",
            enabled = hasResult && canReplaceSelection,
            onClick = onReplace,
            modifier = Modifier.weight(1f),
        )
        BottomAction(
            label = "重试",
            enabled = status !is TranslateStatus.Loading,
            onClick = onRetry,
            modifier = Modifier.weight(1f),
            primary = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslatorTopBar(
    screen: Screen,
    onScreenChange: (Screen) -> Unit,
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Ink,
            actionIconContentColor = Ink,
        ),
        title = {
            Column {
                Text(
                    text = "AI Translate",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                )
                Text(
                    text = if (screen == Screen.Translate) "Selected text assistant" else "Connection settings",
                    color = SecondaryInk,
                    fontSize = 12.sp,
                    letterSpacing = 0.sp,
                )
            }
        },
        actions = {
            SegmentControl(
                selected = screen,
                onSelected = onScreenChange,
            )
        },
    )
}

@Composable
private fun SegmentControl(
    selected: Screen,
    onSelected: (Screen) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(end = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE9E9EE))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SegmentButton(
            text = "翻译",
            selected = selected == Screen.Translate,
            onClick = { onSelected(Screen.Translate) },
        )
        SegmentButton(
            text = "设置",
            selected = selected == Screen.Settings,
            onClick = { onSelected(Screen.Settings) },
        )
    }
}

@Composable
private fun SegmentButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 6.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) Color.White else Color.Transparent,
            contentColor = Ink,
        ),
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TranslateScreen(
    sourceText: String,
    onSourceTextChange: (String) -> Unit,
    status: TranslateStatus,
    settings: AppSettings,
    payload: TranslateIntentPayload,
    popupMode: Boolean,
    onTranslate: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val contentModifier = if (popupMode) {
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    } else {
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    }

    Column(
        modifier = contentModifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (popupMode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    HeaderPanel(
                        settings = settings,
                        payload = payload,
                        onOpenSettings = onOpenSettings,
                        popupMode = true,
                    )
                    SourceAndResult(
                        sourceText = sourceText,
                        onSourceTextChange = onSourceTextChange,
                        status = status,
                        onTranslate = onTranslate,
                        popupMode = true,
                    )
                }
            }
        } else {
            HeaderPanel(
                settings = settings,
                payload = payload,
                onOpenSettings = onOpenSettings,
                popupMode = false,
            )
            SourceAndResult(
                sourceText = sourceText,
                onSourceTextChange = onSourceTextChange,
                status = status,
                onTranslate = onTranslate,
                popupMode = false,
            )
        }
    }
}

@Composable
private fun SourceAndResult(
    sourceText: String,
    onSourceTextChange: (String) -> Unit,
    status: TranslateStatus,
    onTranslate: () -> Unit,
    popupMode: Boolean,
) {
    OutlinedTextField(
        value = sourceText,
        onValueChange = onSourceTextChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = if (popupMode) 2 else 4,
        maxLines = if (popupMode) 4 else 9,
        shape = RoundedCornerShape(if (popupMode) 18.dp else 24.dp),
        label = { Text("原文") },
        placeholder = { Text("选中文字后从系统菜单调用，或在这里输入文本") },
    )

    Button(
        onClick = onTranslate,
        enabled = sourceText.isNotBlank() && status !is TranslateStatus.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
    ) {
        if (status is TranslateStatus.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = if (status is TranslateStatus.Loading) "翻译中" else "翻译",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
    }

    ResultPanel(status = status)

    Spacer(modifier = Modifier.height(if (popupMode) 2.dp else 90.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderPanel(
    settings: AppSettings,
    payload: TranslateIntentPayload,
    onOpenSettings: () -> Unit,
    popupMode: Boolean,
) {
    Surface(
        color = Glass,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.92f),
                            Color(0xFFF7F8FA).copy(alpha = 0.86f),
                        )
                    )
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "自动检测 -> ${settings.targetLanguage}",
                            color = Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = settings.model.ifBlank { "未配置模型" },
                            color = SecondaryInk,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!popupMode && settings.isConfigured) {
                            TextButton(onClick = onOpenSettings) {
                                Text("设置")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        } else if (popupMode && !settings.isConfigured) {
                            TextButton(onClick = onOpenSettings) {
                                Text("去设置")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        StatusPill(configured = settings.isConfigured)
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoChip(text = payload.source.displayName())
                    if (payload.canReplaceSelection) {
                        InfoChip(text = "可替换原文")
                    }
                    if (!settings.isConfigured && !popupMode) {
                        FilterChip(
                            selected = false,
                            onClick = onOpenSettings,
                            label = { Text("补全设置") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(configured: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (configured) Color(0xFFE7F7EE) else Color(0xFFFFECE8))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (configured) Color(0xFF34C759) else Color(0xFFFF3B30))
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = if (configured) "Ready" else "Setup",
            color = if (configured) Color(0xFF1E7B3E) else Color(0xFFC7352E),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun InfoChip(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEDEEF2))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        color = SecondaryInk,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun ResultPanel(status: TranslateStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "译文",
                color = SecondaryInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            AnimatedContent(
                targetState = status,
                label = "result",
            ) { state ->
                when (state) {
                    TranslateStatus.Idle -> EmptyResult()
                    TranslateStatus.Loading -> LoadingResult()
                    is TranslateStatus.Success -> SelectionContainer {
                        Text(
                            text = state.text,
                            color = Ink,
                            fontSize = 20.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    is TranslateStatus.Error -> Text(
                        text = state.message,
                        color = Color(0xFFC7352E),
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyResult() {
    Text(
        text = "等待文本输入",
        color = TertiaryInk,
        fontSize = 16.sp,
    )
}

@Composable
private fun LoadingResult() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == 3) 0.62f else 1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE9EAF0))
            )
        }
    }
}

@Composable
private fun TranslateBottomBar(
    status: TranslateStatus,
    canReplaceSelection: Boolean,
    onCopy: () -> Unit,
    onReplace: () -> Unit,
    onRetry: () -> Unit,
) {
    val hasResult = status is TranslateStatus.Success
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomAction(
                label = "复制",
                enabled = hasResult,
                onClick = onCopy,
                modifier = Modifier.weight(1f),
            )
            BottomAction(
                label = "替换",
                enabled = hasResult && canReplaceSelection,
                onClick = onReplace,
                modifier = Modifier.weight(1f),
            )
            BottomAction(
                label = "重试",
                enabled = status !is TranslateStatus.Loading,
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                primary = true,
            )
        }
    }
}

@Composable
private fun BottomAction(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) AccentBlue else Color(0xFFEDEEF2),
            contentColor = if (primary) Color.White else Ink,
            disabledContainerColor = Color(0xFFEDEEF2),
            disabledContentColor = TertiaryInk,
        ),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
) {
    var draft by remember(settings) { mutableStateOf(settings) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsCard(title = "API") {
            SettingsField(
                label = "Base URL",
                value = draft.baseUrl,
                onValueChange = { draft = draft.copy(baseUrl = it) },
                placeholder = "https://api.openai.com/v1",
            )
            SettingsField(
                label = "API Key",
                value = draft.apiKey,
                onValueChange = { draft = draft.copy(apiKey = it) },
                placeholder = "sk-...",
                secret = true,
            )
            SettingsField(
                label = "Model",
                value = draft.model,
                onValueChange = { draft = draft.copy(model = it) },
                placeholder = "gpt-4o-mini",
            )
        }

        SettingsCard(title = "翻译") {
            SettingsField(
                label = "目标语言",
                value = draft.targetLanguage,
                onValueChange = { draft = draft.copy(targetLanguage = it) },
                placeholder = "中文",
            )
            SettingsField(
                label = "Prompt 模板",
                value = draft.promptTemplate,
                onValueChange = { draft = draft.copy(promptTemplate = it) },
                placeholder = DEFAULT_PROMPT_TEMPLATE,
                minLines = 4,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stream Output", color = Ink, fontWeight = FontWeight.Medium)
                    Text("预留开关，当前接口按非流式请求", color = SecondaryInk, fontSize = 12.sp)
                }
                Switch(
                    checked = draft.streamOutput,
                    onCheckedChange = { draft = draft.copy(streamOutput = it) },
                )
            }
        }

        Button(
            onClick = { onSaveSettings(draft) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        ) {
            Text("保存设置", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                color = SecondaryInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    secret: Boolean = false,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        minLines = minLines,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
    )
}

@Composable
private fun AiTextTranslatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AccentBlue,
            onPrimary = Color.White,
            secondary = Color(0xFF30D158),
            tertiary = Color(0xFFFF9F0A),
            background = IosBackground,
            surface = Color.White,
            onSurface = Ink,
            error = Color(0xFFFF3B30),
        ),
        typography = MaterialTheme.typography,
        content = content,
    )
}

private fun copyToClipboard(context: Context, text: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("translation", text))
    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
}

private fun Activity.openMainSettings() {
    startActivity(
        Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_START_SCREEN, Screen.Settings.name)
    )
    finish()
}

private fun TranslateIntentPayload.Source.displayName(): String {
    return when (this) {
        TranslateIntentPayload.Source.Launcher -> "手动输入"
        TranslateIntentPayload.Source.Translate -> "系统翻译"
        TranslateIntentPayload.Source.ProcessText -> "选中文本"
        TranslateIntentPayload.Source.Share -> "分享文本"
        TranslateIntentPayload.Source.Unknown -> "未知入口"
    }
}

private val IosBackground = Color(0xFFF5F5F7)
private val Glass = Color(0xF2FFFFFF)
private val Ink = Color(0xFF17181C)
private val SecondaryInk = Color(0xFF6B7280)
private val TertiaryInk = Color(0xFF9CA3AF)
private val AccentBlue = Color(0xFF0A84FF)

private const val EXTRA_START_SCREEN = "start_screen"
