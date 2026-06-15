# AI Text Translator Android

[English](README.md) | [中文](README.zh-CN.md)

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

## API 配置

默认 endpoint：

```text
https://api.openai.com/v1
```

应用调用：

```text
POST /chat/completions
```

只要服务商兼容 Chat Completions API，就可以填入对应 Base URL 和模型名。

## 安全说明

- API Key 由用户在应用内填写，保存在本机 DataStore 中，不提交到仓库。
- Android backup 已关闭，避免本机 API 配置被系统备份带走。
- Debug APK、审计记录、过程 todo 等本地文件不会提交到 Git。
