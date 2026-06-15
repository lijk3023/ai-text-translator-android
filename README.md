# AI Text Translator Android

[English](README.md) | [中文](README.zh-CN.md)

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
