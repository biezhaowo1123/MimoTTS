package com.mimotts.api

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.mimotts.api.dto.AudioConfig
import com.mimotts.api.dto.ChatCompletionRequest
import com.mimotts.api.dto.ChatMessage
import com.mimotts.api.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Mimov25ApiClient(
    private val baseUrlProvider: () -> String,
    private val apiKeyProvider: () -> String
) {
    companion object {
        private const val TAG = "Mimov25ApiClient"
    }

    private val gson = Gson()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(apiKeyProvider))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val api: Mimov25Api by lazy {
        val rawUrl = baseUrlProvider()
        val baseUrl = if (rawUrl.endsWith("/")) rawUrl else "$rawUrl/"
        Log.d(TAG, "初始化 API, baseUrl=$baseUrl")
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Mimov25Api::class.java)
    }

    suspend fun synthesize(
        text: String,
        voiceId: String,
        speed: Float = 1.0f,
        sampleRate: Int = 24000,
        voiceDesignPrompt: String? = null,
        voiceCloneAudioBase64: String? = null
    ): Result<ByteArray> {
        return try {
            val isVoiceDesign = !voiceDesignPrompt.isNullOrBlank()
            val isVoiceClone = !voiceCloneAudioBase64.isNullOrBlank()
            val messages = mutableListOf<ChatMessage>()

            when {
                isVoiceClone -> {
                    // 音色克隆模式：user message 为空，audio.voice 传入音频样本
                    messages.add(ChatMessage(role = "user", content = ""))
                    messages.add(ChatMessage(role = "assistant", content = text))
                }
                isVoiceDesign -> {
                    // 音色设计模式：user message = 音色描述，assistant message = 要合成的文本
                    val userContent = if (speed != 1.0f) {
                        val speedInstruction = getSpeedInstruction(speed)
                        if (speedInstruction != null) "$voiceDesignPrompt，$speedInstruction" else voiceDesignPrompt
                    } else {
                        voiceDesignPrompt
                    }
                    messages.add(ChatMessage(role = "user", content = userContent))
                    messages.add(ChatMessage(role = "assistant", content = text))
                }
                else -> {
                    // 普通模式：使用内置音色
                    if (speed != 1.0f) {
                        val speedInstruction = getSpeedInstruction(speed)
                        if (speedInstruction != null) {
                            messages.add(ChatMessage(role = "user", content = speedInstruction))
                        }
                    }
                    messages.add(ChatMessage(role = "assistant", content = text))
                }
            }

            val model = when {
                isVoiceClone -> "mimo-v2.5-tts-voiceclone"
                isVoiceDesign -> "mimo-v2.5-tts-voicedesign"
                else -> "mimo-v2.5-tts"
            }
            val audioConfig = when {
                isVoiceClone -> AudioConfig(format = "pcm16", voice = voiceCloneAudioBase64)
                isVoiceDesign -> AudioConfig(format = "pcm16")
                else -> AudioConfig(format = "pcm16", voice = if (voiceId == "default") "冰糖" else voiceId)
            }

            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                audio = audioConfig,
                stream = true
            )
            Log.d(TAG, "发送请求: model=$model, text=${text.take(30)}, voiceDesign=$isVoiceDesign, voiceClone=$isVoiceClone, url=${baseUrlProvider()}")

            val response = api.synthesizeStreaming(request)
            Log.d(TAG, "响应: code=${response.code()}, successful=${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val pcmAudio = parseStreamingAudio(body)
                    Log.d(TAG, "解析完成, PCM 大小: ${pcmAudio.size} bytes")
                    Result.success(pcmAudio)
                } else {
                    Log.e(TAG, "响应体为空")
                    Result.failure(Exception("响应体为空"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "API 错误: ${response.code()} ${response.message()}, body=$errorBody")
                Result.failure(Exception("API 错误: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "请求异常: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun getSpeedInstruction(speed: Float): String? = when {
        speed <= 0.6f -> "请用非常慢的语速朗读，每个字都要清晰缓慢"
        speed <= 0.8f -> "请用较慢的语速朗读"
        speed <= 0.9f -> "请用略慢的语速朗读"
        speed >= 1.5f -> "请用快速的语速朗读"
        speed >= 1.3f -> "请用较快的语速朗读"
        speed >= 1.1f -> "请用略快的语速朗读"
        else -> null
    }

    private fun parseStreamingAudio(body: okhttp3.ResponseBody): ByteArray {
        val audioChunks = mutableListOf<Byte>()
        val source = body.source()

        while (!source.exhausted()) {
            val line = source.readUtf8Line() ?: break
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break

                try {
                    val chunk = gson.fromJson(data, StreamChunk::class.java)
                    val audioData = chunk.choices?.firstOrNull()
                        ?.delta?.audio?.data
                    if (audioData != null) {
                        val decoded = Base64.decode(audioData, Base64.DEFAULT)
                        audioChunks.addAll(decoded.toList())
                        Log.d(TAG, "收到音频块: ${decoded.size} bytes")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "解析 SSE 块失败: $data", e)
                }
            }
        }

        return audioChunks.toByteArray()
    }
}

private data class StreamChunk(
    val choices: List<StreamChoice>?
)

private data class StreamChoice(
    val delta: StreamDelta?
)

private data class StreamDelta(
    val audio: StreamAudio?
)

private data class StreamAudio(
    val data: String?
)
