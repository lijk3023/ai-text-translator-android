# AI Text Translator Android

Android app for translating selected text with an OpenAI-compatible chat completion API.

The app registers Android selected-text actions, so it can be opened from the system text selection menu. It can translate in a compact popup window without forcing a full app switch, and the main app screen provides API and prompt settings.

## Features

- Translate selected text from Android `ACTION_TRANSLATE` and `ACTION_PROCESS_TEXT`.
- Compact popup translation window for selection-based workflows.
- OpenAI-compatible `/chat/completions` API support.
- Configurable Base URL, API key, model, target language, and prompt template.
- Copy translated text or replace selected text when the calling app allows it.
- Kotlin, Jetpack Compose, Material 3, DataStore, and OkHttp.

## Package

```text
com.lijk.aitexttranslator
```

## Build

This project uses the Gradle wrapper and Android Gradle Plugin.

```bash
./gradlew assembleDebug
```

For a release build, configure your own signing key locally. Do not commit keystores or API keys.

## API Configuration

Default endpoint:

```text
https://api.openai.com/v1
```

The app calls:

```text
POST /chat/completions
```

Any OpenAI-compatible provider can be used if it supports chat completions.

## Security Notes

- API keys are user-provided at runtime and are not stored in the repository.
- Android backup is disabled to avoid leaking locally stored API settings through device backup.
- Debug APKs are local build artifacts and are intentionally ignored by Git.
