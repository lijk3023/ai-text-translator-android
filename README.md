# AI Text Translator Android

[English](#ai-text-translator-android) | [中文](#中文)

Android selected-text translator powered by OpenAI-compatible Chat Completions APIs. The main screen manages API settings, while selected text can be translated from the Android text action menu in a compact popup window.

## Features

- Handles Android `ACTION_TRANSLATE` and `ACTION_PROCESS_TEXT` text actions.
- Can be launched from the selected-text menu, usually through `Translate` or `More (...)`.
- Shows translation results in a compact popup instead of a full-screen workflow.
- Supports light and dark themes following the system setting.
- Supports OpenAI-compatible `/chat/completions` APIs.
- Configurable Base URL, API key, model, target language, and prompt template.
- Automatically translates Chinese-looking text into English; other text uses the configured target language.
- Can copy translated text or replace selected text when the calling app allows it.
- Built with Kotlin, Jetpack Compose, Material 3, DataStore, and OkHttp.

## Package

```text
dev.aitexttranslator
```

## How To Use

1. Install the APK.
2. Open `AI Translate`.
3. Fill in API settings:
   - Base URL, for example `https://api.openai.com/v1`
   - API Key
   - Model
   - Target language
   - Prompt template
4. Select text in another app.
5. Choose `Translate` from the Android text action menu. If it is not shown directly, open `More (...)` and look for `Translate`.
6. Select `AI Translate` from the app chooser.

Android versions and vendor ROMs handle the selected-text menu differently. Some systems show the app directly under `Translate`, some put it under `More (...)`, and some apps use custom text selection menus that may not expose Android text actions. The app declares `ACTION_TRANSLATE` and `ACTION_PROCESS_TEXT`; final menu placement is controlled by the system or the calling app.

## Build

```bash
./gradlew assembleDebug
```

Full check:

```bash
./gradlew testDebugUnitTest assembleDebug lintDebug
```

Release builds require your own signing key. Do not commit keystores, API keys, or local signing configuration.

## Release

GitHub Releases are usually created from a Git tag and can include APK files as release assets.

Web flow:

1. Push the latest code to GitHub.
2. Open the repository page.
3. Go to `Releases`.
4. Click `Draft a new release`.
5. Create a tag such as `v0.1.0`.
6. Use `v0.1.0` as the release title.
7. Upload an APK file, such as `artifacts/ai-text-translator-debug.apk` or your signed release APK.
8. State clearly whether the uploaded APK is a debug build or a release build.
9. Click `Publish release`.

With GitHub CLI:

```bash
gh release create v0.1.0 artifacts/ai-text-translator-debug.apk \
  --repo lijk3023/ai-text-translator-android \
  --title "v0.1.0" \
  --notes "Initial Android selected-text translator release."
```

For public distribution, upload a signed release APK instead of a debug APK.

## API Configuration

Default endpoint:

```text
https://api.openai.com/v1
```

The app calls:

```text
POST /chat/completions
```

Any provider compatible with Chat Completions can be used by changing the Base URL and model name.

## Security Notes

- API keys are entered by users in the app and stored locally in DataStore; they are not committed to the repository.
- Android backup is disabled to avoid leaking local API settings through device backup.
- Debug APKs, audit notes, and local todo files are intentionally ignored by Git.

---

## 中文

[English](#ai-text-translator-android) | [中文](#中文)

一个 Android 选中文字翻译应用，通过 OpenAI-compatible Chat Completions API 翻译文本。主界面用于配置 API，选中文本后可从系统文本操作菜单调用应用，并以紧凑弹窗显示翻译结果。

## 功能

- 支持 Android `ACTION_TRANSLATE` 和 `ACTION_PROCESS_TEXT` 文本操作入口。
- 选中文字后通过系统菜单调用，常见入口是“翻译”或“更多(...)”里的“翻译”。
- 使用非全屏弹窗展示翻译结果，适合从其他应用临时调用。
- 支持跟随系统的浅色/深色模式。
- 支持 OpenAI-compatible `/chat/completions` API。
- 可配置 Base URL、API Key、模型、目标语言和 prompt 模板。
- 检测到中文文本时默认翻译为英文；其他文本使用配置里的目标语言。
- 调用方允许时，可复制译文或替换选中的原文。
- 使用 Kotlin、Jetpack Compose、Material 3、DataStore 和 OkHttp。

## 包名

```text
dev.aitexttranslator
```

## 使用方式

1. 安装 APK。
2. 打开 `AI Translate`。
3. 在设置页填写 API 配置：
   - Base URL，例如 `https://api.openai.com/v1`
   - API Key
   - Model
   - Target language
   - Prompt template
4. 在浏览器、阅读器、聊天软件或其他应用中选中一段文字。
5. 在系统文本操作菜单中选择“翻译”。如果没有直接显示，点“更多(...)”再找“翻译”。
6. 在应用选择列表里选择 `AI Translate`。

不同 Android 版本和厂商 ROM 对文本操作菜单的展示不同。有的系统会把应用直接放在“翻译”入口里，有的系统会收进“更多(...)”，还有些应用自己的文本选择菜单可能不暴露系统入口。这不是应用能完全控制的行为，应用侧已经声明了 `ACTION_TRANSLATE` 和 `ACTION_PROCESS_TEXT`。

## 构建

```bash
./gradlew assembleDebug
```

完整检查：

```bash
./gradlew testDebugUnitTest assembleDebug lintDebug
```

release 构建需要你自己配置签名密钥。不要把 keystore、API Key 或本地签名配置提交到仓库。

## 发布 Release

GitHub Release 通常基于一个 Git tag，并可上传 APK 作为附件。Web 页面操作步骤：

1. 确认代码已经推到 GitHub。
2. 打开仓库页面。
3. 进入 `Releases`。
4. 点击 `Draft a new release`。
5. 创建一个 tag，例如 `v0.1.0`。
6. Release title 可以写 `v0.1.0`。
7. 上传 APK 文件，例如 `artifacts/ai-text-translator-debug.apk` 或你自己签名后的 release APK。
8. 在说明里写清楚这是 debug 包还是 release 包。
9. 点击 `Publish release`。

命令行方式需要安装并登录 GitHub CLI：

```bash
gh release create v0.1.0 artifacts/ai-text-translator-debug.apk \
  --repo lijk3023/ai-text-translator-android \
  --title "v0.1.0" \
  --notes "Initial Android selected-text translator release."
```

更正式的公开发布建议上传签名后的 release APK，而不是 debug APK。
