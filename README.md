# AI Text Translator Android

Android app for translating selected text with an OpenAI-compatible chat completion API.

The app supports Android selected-text actions and can translate text in a compact popup window. The main app screen provides API and prompt settings.

## Features

- Handle selected text from Android `ACTION_TRANSLATE` and `ACTION_PROCESS_TEXT`.
- Compact popup translation window for selection-based workflows.
- Light and dark themes that follow the system setting.
- Automatically translates Chinese-looking source text into English while keeping the configured target language for other text.
- OpenAI-compatible `/chat/completions` API support.
- Configurable Base URL, API key, model, target language, and prompt template.
- Copy translated text or replace selected text when the calling app allows it.
- Kotlin, Jetpack Compose, Material 3, DataStore, and OkHttp.

## Package

```text
dev.aitexttranslator
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
