package com.mimotts.domain.model

data class SynthesisConfig(
    val defaultVoiceId: String,
    val speed: Float = 1.0f,
    val sampleRate: Int = 24000,
    val voiceDesignPrompt: String? = null,
    val voiceCloneAudioBase64: String? = null
)
