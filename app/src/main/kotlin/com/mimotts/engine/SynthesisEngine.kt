package com.mimotts.engine

import android.util.Log
import android.util.LruCache
import com.mimotts.api.Mimov25ApiClient
import com.mimotts.domain.model.SynthesisConfig
import com.mimotts.domain.ssml.SsmlParser
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class SynthesisEngine(
    private val apiClient: Mimov25ApiClient,
    private val cacheEnabled: Boolean = true
) {
    companion object {
        private const val TAG = "SynthesisEngine"
        private const val MAX_CACHE_SIZE = 50
    }

    private val ssmlParser = SsmlParser()
    private val audioCache = LruCache<String, ByteArray>(MAX_CACHE_SIZE)

    fun synthesize(
        text: String,
        config: SynthesisConfig,
        onAudioChunk: (ByteArray) -> Boolean,
        isCancelled: () -> Boolean
    ): Boolean {
        val segments = ssmlParser.parse(text)
        Log.d(TAG, "解析为 ${segments.size} 个片段")

        if (segments.isEmpty()) return true

        runBlocking {
            // 预取第一个片段
            var nextPrefetch: Deferred<ByteArray?>? = null

            for ((index, segment) in segments.withIndex()) {
                if (isCancelled()) return@runBlocking false

                val voiceId = segment.voiceId ?: config.defaultVoiceId
                val speed = segment.prosody?.rate ?: config.speed

                // 获取当前片段音频（来自预取或缓存或新请求）
                val voiceModeSuffix = when {
                    config.voiceCloneAudioBase64 != null -> "_vc${config.voiceCloneAudioBase64.hashCode()}"
                    config.voiceDesignPrompt != null -> "_vd${config.voiceDesignPrompt.hashCode()}"
                    else -> ""
                }
                val cacheKey = "${segment.text.hashCode()}_$voiceId$voiceModeSuffix"
                val cachedAudio = if (cacheEnabled) audioCache.get(cacheKey) else null

                val audioData: ByteArray = if (cachedAudio != null) {
                    Log.d(TAG, "缓存命中: $cacheKey, 大小: ${cachedAudio.size} bytes")
                    // 取消不需要的预取
                    nextPrefetch?.let { if (!it.isCompleted) it.cancel() }
                    cachedAudio
                } else {
                    // 等待预取结果，或直接请求
                    val prefetched = nextPrefetch?.await()
                    if (prefetched != null) {
                        Log.d(TAG, "预取命中: 片段 $index, 大小: ${prefetched.size} bytes")
                        if (cacheEnabled) audioCache.put(cacheKey, prefetched)
                        prefetched
                    } else {
                        Log.d(TAG, "合成片段 $index: text=${segment.text.take(30)}, voice=$voiceId")
                        val result = apiClient.synthesize(
                            text = segment.text,
                            voiceId = voiceId,
                            speed = speed,
                            sampleRate = config.sampleRate,
                            voiceDesignPrompt = config.voiceDesignPrompt,
                            voiceCloneAudioBase64 = config.voiceCloneAudioBase64
                        )
                        result.fold(
                            onSuccess = { data ->
                                if (cacheEnabled) audioCache.put(cacheKey, data)
                                data
                            },
                            onFailure = { error ->
                                Log.e(TAG, "片段 $index 失败: ${error.message}", error)
                                return@runBlocking false
                            }
                        )
                    }
                }

                // 预取下一个片段
                nextPrefetch = if (index + 1 < segments.size) {
                    val nextSegment = segments[index + 1]
                    val nextVoiceId = nextSegment.voiceId ?: config.defaultVoiceId
                    val nextSpeed = nextSegment.prosody?.rate ?: config.speed
                    val nextVoiceModeSuffix = when {
                        config.voiceCloneAudioBase64 != null -> "_vc${config.voiceCloneAudioBase64.hashCode()}"
                        config.voiceDesignPrompt != null -> "_vd${config.voiceDesignPrompt.hashCode()}"
                        else -> ""
                    }
                    val nextCacheKey = "${nextSegment.text.hashCode()}_$nextVoiceId$nextVoiceModeSuffix"
                    val nextCached = if (cacheEnabled) audioCache.get(nextCacheKey) else null

                    if (nextCached != null) {
                        null // 下一个片段已有缓存，不需要预取
                    } else {
                        async(Dispatchers.IO) {
                            Log.d(TAG, "预取片段 ${index + 1}: text=${nextSegment.text.take(30)}")
                            apiClient.synthesize(
                                text = nextSegment.text,
                                voiceId = nextVoiceId,
                                speed = nextSpeed,
                                sampleRate = config.sampleRate,
                                voiceDesignPrompt = config.voiceDesignPrompt,
                                voiceCloneAudioBase64 = config.voiceCloneAudioBase64
                            ).getOrNull()
                        }
                    }
                } else null

                // 播放当前片段音频
                Log.d(TAG, "片段 $index 成功, 音频大小: ${audioData.size} bytes")
                val chunkSize = 8192
                var offset = 0
                while (offset < audioData.size) {
                    if (isCancelled()) {
                        nextPrefetch?.cancel()
                        return@runBlocking false
                    }
                    val length = minOf(chunkSize, audioData.size - offset)
                    val chunk = audioData.copyOfRange(offset, offset + length)
                    val shouldContinue = onAudioChunk(chunk)
                    if (!shouldContinue) {
                        nextPrefetch?.cancel()
                        return@runBlocking false
                    }
                    offset += length
                }
            }
            true
        }

        return true
    }
}
