# MimoTTS — Android TTS 引擎应用

基于 mimov2.5 TTS 在线 API 的系统级文字转语音引擎，类似 MultiTTS。

## 项目结构

```
app/src/main/kotlin/com/mimotts/
├── engine/          # TTS 引擎核心（MimoTtsService, SynthesisEngine, AudioConverter）
├── api/             # API 客户端（Retrofit + OkHttp）
├── data/            # 数据层（Room 数据库、DataStore、Repository）
├── domain/          # 业务逻辑（模型、SSML 解析、UseCase）
└── ui/              # Jetpack Compose 界面（设置、语音列表、角色映射）
```

## 技术栈

- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 网络：Retrofit + OkHttp
- 数据库：Room
- 偏好设置：DataStore
- 最低 SDK：26 (Android 8.0)
- 目标 SDK：35

## 开发规范

### 语言要求

- 所有注释使用中文
- commit message 使用中文
- 文档和 README 使用中文
- 代码中的字符串常量（面向用户的）使用中文

### 代码风格

- 遵循项目已有的代码风格和命名约定
- 保持代码简洁，避免过度抽象
- 优先修改现有文件，而非创建新文件

### 提交规范

- commit message 格式：`<类型>: <简要描述>`
- 类型：`新增`、`修复`、`重构`、`文档`、`测试`、`配置`

## 常用命令

```bash
# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease

# 运行测试
./gradlew test

# 清理构建
./gradlew clean
```

## 核心功能

1. **系统 TTS 引擎** — 继承 TextToSpeechService，注册为系统级 TTS
2. **多音色切换** — 支持多种语音角色选择
3. **阅读器集成** — 支持 SSML 标签实现角色对话朗读（Legado 等）

## 注意事项

- 修改代码前先了解上下文，不要引入不必要的依赖
- 不要添加安全漏洞（注入、XSS 等）
- 测试用例应覆盖核心逻辑
- API 客户端的 baseUrl 和 apiKey 需要用户在设置中配置