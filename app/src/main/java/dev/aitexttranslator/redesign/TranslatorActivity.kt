package dev.aitexttranslator.redesign

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.aitexttranslator.AppSettings
import dev.aitexttranslator.SettingsRepository
import dev.aitexttranslator.TranslateIntentPayload
import dev.aitexttranslator.TranslatorClient
import dev.aitexttranslator.extractTranslatePayload
import dev.aitexttranslator.redesign.theme.TranslatorTheme
import dev.aitexttranslator.redesign.ui.PopupScreen
import dev.aitexttranslator.redesign.ui.SegmentedControl
import dev.aitexttranslator.redesign.ui.SettingsScreen
import dev.aitexttranslator.redesign.ui.TranslateActionRow
import dev.aitexttranslator.redesign.ui.TranslateScreen
import dev.aitexttranslator.redesign.ui.TranslateStatus
import dev.aitexttranslator.redesign.ui.TranslatorScreen
import dev.aitexttranslator.redesign.ui.successText
import kotlinx.coroutines.launch

/**
 * 重写版入口 Activity。
 *
 * 与现有 `MainActivity` 行为对齐（同样区分主界面 / 弹窗，处理 ACTION_TRANSLATE /
 * ACTION_PROCESS_TEXT 等），但 UI 全部换成 redesign 包里的全新组件，并默认跟随系统深色模式。
 *
 * 适配方接入方式见 ui-redesign/README.md —— 简单说就是用本类替换原 MainActivity，
 * 或把原 MainActivity 的 setContent 内容换成这里的 [TranslatorApp] / [PopupTranslatorApp]。
 */
open class TranslatorActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository
    private val translatorClient = TranslatorClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)
        val popupEntry = this is PopupTranslatorActivity
        val startScreen = when (intent.getStringExtra(TranslatorActivity.EXTRA_START_SCREEN)) {
            TranslatorScreen.Settings.name -> TranslatorScreen.Settings
            else -> TranslatorScreen.Translate
        }
        if (popupEntry) {
            setFinishOnTouchOutside(true)
            window.setGravity(Gravity.CENTER)
            window.setDimAmount(0.32f)
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.88f).toInt(),
                (resources.displayMetrics.heightPixels * 0.7f).toInt(),
            )
        } else {
            enableEdgeToEdge()
        }
        val translate: suspend (String, AppSettings) -> Result<String> = { text, active ->
            translatorClient.translate(text, active)
        }
        val replaceSelection: (String) -> Unit = { translatedText ->
            replaceSelectionAndFinish(translatedText)
        }
        val openSettings: () -> Unit = {
            openMainSettings()
        }

        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = AppSettings())
            val initialPayload = remember(intent) { intent.extractTranslatePayload() }
            TranslatorTheme {
                if (popupEntry) {
                    PopupTranslatorApp(
                        initialPayload = initialPayload,
                        settings = settings,
                        onTranslate = translate,
                        onReplaceSelection = replaceSelection,
                        onOpenSettings = openSettings,
                    )
                } else {
                    TranslatorApp(
                        initialPayload = initialPayload,
                        settings = settings,
                        initialScreen = startScreen,
                        onSaveSettings = { settingsRepository.save(it) },
                        onTranslate = translate,
                        onReplaceSelection = replaceSelection,
                    )
                }
            }
        }
    }

    private fun replaceSelectionAndFinish(translatedText: String) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, translatedText),
        )
        finish()
    }

    private fun openMainSettings() {
        startActivity(
            Intent(this, TranslatorActivity::class.java)
                .putExtra(EXTRA_START_SCREEN, TranslatorScreen.Settings.name),
        )
        finish()
    }

    companion object {
        const val EXTRA_START_SCREEN = "start_screen"
    }
}

/** 弹窗入口（对应原 `SelectionTranslateActivity`）。 */
class PopupTranslatorActivity : TranslatorActivity()

/* ------------------------------------------------------------------ */
/* 主界面宿主                                                          */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslatorApp(
    initialPayload: TranslateIntentPayload,
    settings: AppSettings,
    initialScreen: TranslatorScreen,
    onSaveSettings: suspend (AppSettings) -> Unit,
    onTranslate: suspend (String, AppSettings) -> Result<String>,
    onReplaceSelection: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = TranslatorTheme.colors

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
        if (initialPayload.text.isNotBlank() && settings.isConfigured) runTranslation()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(colors.canvas),
        containerColor = colors.canvas,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.ink,
                ),
                title = {
                    Column {
                        Text(
                            text = "AI 翻译",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.ink,
                        )
                        Text(
                            text = if (screen == TranslatorScreen.Translate) "选中文字助手" else "连接与翻译设置",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkMuted,
                        )
                    }
                },
                actions = {
                    SegmentedControl(
                        selected = screen,
                        onSelected = { screen = it },
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
        },
        bottomBar = {
            if (screen == TranslatorScreen.Translate) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.canvas)
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    TranslateActionRow(
                        status = status,
                        canReplaceSelection = initialPayload.canReplaceSelection,
                        onCopy = { copyToClipboard(context, status.successText) },
                        onReplace = { onReplaceSelection(status.successText) },
                        onRetry = { runTranslation() },
                    )
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = screen,
            label = "screen",
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { target ->
            when (target) {
                TranslatorScreen.Translate -> TranslateScreen(
                    sourceText = sourceText,
                    onSourceTextChange = { sourceText = it },
                    status = status,
                    settings = settings,
                    payload = initialPayload,
                    onTranslate = { runTranslation() },
                    onOpenSettings = { screen = TranslatorScreen.Settings },
                )

                TranslatorScreen.Settings -> SettingsScreen(
                    settings = settings,
                    onSaveSettings = { newSettings ->
                        scope.launch {
                            onSaveSettings(newSettings)
                            Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                            screen = TranslatorScreen.Translate
                        }
                    },
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* 弹窗宿主                                                            */
/* ------------------------------------------------------------------ */

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
        if (initialPayload.text.isNotBlank() && settings.isConfigured) runTranslation()
    }

    PopupScreen(
        sourceText = sourceText,
        onSourceTextChange = { sourceText = it },
        status = status,
        settings = settings,
        payload = initialPayload,
        onCopy = { copyToClipboard(context, status.successText) },
        onReplace = { onReplaceSelection(status.successText) },
        onRetry = { runTranslation() },
        onOpenSettings = onOpenSettings,
    )
}

private fun copyToClipboard(context: Context, text: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("translation", text))
    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
}
