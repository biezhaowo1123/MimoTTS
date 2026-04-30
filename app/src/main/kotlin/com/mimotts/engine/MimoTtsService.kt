package com.mimotts.engine

import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.speech.tts.Voice
import android.util.Log
import com.mimotts.api.Mimov25ApiClient
import com.mimotts.data.datastore.SettingsDataStore
import com.mimotts.domain.model.SynthesisConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class MimoTtsService : TextToSpeechService() {

    companion object {
        private const val TAG = "MimoTtsService"
    }

    private val isStopped = AtomicBoolean(false)
    private var synthesisEngine: SynthesisEngine? = null
    private var currentVoiceId: String = "default"
    private var settingsDataStore: SettingsDataStore? = null

    private var cachedBaseUrl: String = ""
    private var cachedApiKey: String = ""

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        settingsDataStore = SettingsDataStore(this)

        // 预加载设置
        runBlocking {
            cachedBaseUrl = settingsDataStore?.apiBaseUrl?.first() ?: ""
            cachedApiKey = settingsDataStore?.apiKey?.first() ?: ""
        }
        Log.d(TAG, "API URL: $cachedBaseUrl, Key: ${cachedApiKey.take(8)}...")

        val cacheEnabled = runBlocking {
            settingsDataStore?.cacheEnabled?.first() ?: true
        }

        val apiClient = Mimov25ApiClient(
            baseUrlProvider = { cachedBaseUrl },
            apiKeyProvider = { cachedApiKey }
        )
        synthesisEngine = SynthesisEngine(apiClient, cacheEnabled)
    }

    override fun onSynthesizeText(request: SynthesisRequest, callback: SynthesisCallback) {
        val text = request.charSequenceText?.toString() ?: return
        Log.d(TAG, "onSynthesizeText: text=${text.take(50)}..., length=${text.length}")

        if (text.isBlank()) {
            callback.start(24000, android.media.AudioFormat.ENCODING_PCM_16BIT, 1)
            callback.done()
            return
        }

        isStopped.set(false)
        val sampleRate = 24000

        val defaultVoiceId = runBlocking {
            settingsDataStore?.defaultVoiceId?.first() ?: "default"
        }
        val speechRate = runBlocking {
            settingsDataStore?.speechRate?.first() ?: 1.0f
        }
        val voiceDesignEnabled = runBlocking {
            settingsDataStore?.voiceDesignEnabled?.first() ?: false
        }
        val voiceDesignPrompt = runBlocking {
            settingsDataStore?.voiceDesignPrompt?.first() ?: ""
        }
        val voiceCloneEnabled = runBlocking {
            settingsDataStore?.voiceCloneEnabled?.first() ?: false
        }
        val voiceCloneAudioPath = runBlocking {
            settingsDataStore?.voiceCloneAudioPath?.first() ?: ""
        }

        callback.start(sampleRate, android.media.AudioFormat.ENCODING_PCM_16BIT, 1)

        // 读取音色克隆音频文件并转为 base64
        val voiceCloneAudioBase64 = if (voiceCloneEnabled && voiceCloneAudioPath.isNotBlank()) {
            try {
                val file = java.io.File(voiceCloneAudioPath)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    val mimeType = when {
                        voiceCloneAudioPath.endsWith(".mp3", true) -> "audio/mpeg"
                        voiceCloneAudioPath.endsWith(".wav", true) -> "audio/wav"
                        else -> "audio/wav"
                    }
                    "data:$mimeType;base64,${android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)}"
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "读取音色克隆音频失败", e)
                null
            }
        } else null

        val config = SynthesisConfig(
            defaultVoiceId = if (currentVoiceId != "default") currentVoiceId else defaultVoiceId,
            speed = speechRate,
            sampleRate = sampleRate,
            voiceDesignPrompt = if (voiceDesignEnabled && voiceDesignPrompt.isNotBlank() && voiceCloneAudioBase64 == null) voiceDesignPrompt else null,
            voiceCloneAudioBase64 = voiceCloneAudioBase64
        )

        Log.d(TAG, "开始合成, voiceId=${config.defaultVoiceId}, speed=${config.speed}, voiceDesign=${config.voiceDesignPrompt?.take(30)}")

        try {
            val success = synthesisEngine?.synthesize(
                text = text,
                config = config,
                onAudioChunk = { chunk ->
                    if (isStopped.get()) {
                        Log.d(TAG, "合成被取消")
                        false
                    } else {
                        callback.audioAvailable(chunk, 0, chunk.size)
                        true
                    }
                },
                isCancelled = { isStopped.get() }
            )

            if (success == true && !isStopped.get()) {
                Log.d(TAG, "合成完成")
                callback.done()
            } else {
                Log.w(TAG, "合成失败或被取消, success=$success")
                callback.done()
            }
        } catch (e: Exception) {
            Log.e(TAG, "合成异常", e)
            callback.done()
        }
    }

    override fun onGetVoices(): List<Voice> {
        return listOf(
            Voice("default", Locale("zho", "CHN"), Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, setOf("default")),
            Voice("female", Locale("zho", "CHN"), Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, setOf("female")),
            Voice("male", Locale("zho", "CHN"), Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, setOf("male"))
        )
    }

    override fun onGetLanguage(): Array<String> = arrayOf("zho", "CHN", "")

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        return when (lang ?: "zho") {
            "zho", "eng" -> TextToSpeech.LANG_AVAILABLE
            else -> TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        return onIsLanguageAvailable(lang, country, variant)
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        isStopped.set(true)
    }
}
