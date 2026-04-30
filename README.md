# MimoTTS

基于小米 MiMo-V2.5-TTS 系列模型的 Android 系统级文字转语音引擎。

## 功能特性

- **系统级 TTS 引擎** — 注册为系统 TTS，支持阅读器（Legado 等）直接调用
- **多种音色模式**
  - 内置音色：冰糖、茉莉、苏打、白桦、Mia、Chloe、Milo、Dean
  - 音色设计：通过文字描述自定义音色（如"温柔的年轻女声"）
  - 音色克隆：上传音频样本克隆任意音色
- **语速控制** — 0.5x ~ 2.0x 语速调节
- **音频缓存** — LRU 内存缓存，减少重复 API 调用
- **SSML 支持** — 支持 `<speak>` 标签实现多角色对话朗读
- **片段预取** — 多段文本自动预取下一段，减少停顿
- **常驻后台** — 前台服务防止系统杀死引擎

## 系统要求

- Android 8.0 (API 26) 及以上
- 需要小米 MiMo API Key（[获取地址](https://platform.xiaomimimo.com)）

## 安装

1. 从 [Releases](https://github.com/biezhaowo1123/MimoTTS/releases) 下载最新 APK
2. 安装后打开应用，配置 API Key 和服务器地址
3. 在系统设置中将 MimoTTS 设为默认 TTS 引擎

## 配置

| 设置项 | 说明 | 默认值 |
|--------|------|--------|
| API Key | MiMo 平台 API Key | 空 |
| API 地址 | 服务器地址 | `https://api.xiaomimimo.com/v1` |
| 默认音色 | 内置音色名称 | 冰糖 |
| 语速 | 朗读速度 | 1.0x |
| 缓存 | 启用音频缓存 | 开启 |
| 音色设计 | 文字描述自定义音色 | 关闭 |
| 音色克隆 | 音频样本克隆音色 | 关闭 |
| 常驻后台 | 防止系统杀死引擎 | 关闭 |

## 技术栈

- Kotlin + Jetpack Compose
- Retrofit + OkHttp（网络请求）
- DataStore（偏好存储）
- Android TextToSpeechService 框架

## 项目结构

```
app/src/main/kotlin/com/mimotts/
├── engine/          # TTS 引擎核心
├── api/             # API 客户端
├── data/            # 数据层
├── domain/          # 业务逻辑
└── ui/              # Jetpack Compose 界面
```

## API 说明

使用小米 MiMo-V2.5-TTS 系列 API，兼容 OpenAI Chat Completions 格式：

| 模型 | 用途 |
|------|------|
| `mimo-v2.5-tts` | 内置音色合成 |
| `mimo-v2.5-tts-voicedesign` | 音色设计 |
| `mimo-v2.5-tts-voiceclone` | 音色克隆 |

## 许可证

MIT License
