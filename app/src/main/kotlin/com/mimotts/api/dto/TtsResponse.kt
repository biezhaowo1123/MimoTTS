package com.mimotts.api.dto

import com.google.gson.annotations.SerializedName

data class ChatCompletionResponse(
    @SerializedName("choices")
    val choices: List<Choice>?,
    @SerializedName("error")
    val error: ErrorBody?
)

data class Choice(
    @SerializedName("message")
    val message: MessageContent?
)

data class MessageContent(
    @SerializedName("audio")
    val audio: AudioData?
)

data class AudioData(
    @SerializedName("data")
    val data: String?
)

data class ErrorBody(
    @SerializedName("message")
    val message: String?,
    @SerializedName("type")
    val type: String?
)
