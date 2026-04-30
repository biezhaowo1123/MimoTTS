package com.mimotts.api.dto

import com.google.gson.annotations.SerializedName

data class ChatCompletionRequest(
    @SerializedName("model")
    val model: String = "mimo-v2.5-tts",
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    @SerializedName("audio")
    val audio: AudioConfig,
    @SerializedName("stream")
    val stream: Boolean = false
)

data class ChatMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class AudioConfig(
    @SerializedName("format")
    val format: String = "pcm16",
    @SerializedName("voice")
    val voice: String? = null
)
