package com.mimotts.api

import com.mimotts.api.dto.ChatCompletionRequest
import com.mimotts.api.dto.ChatCompletionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface Mimov25Api {

    @POST("chat/completions")
    suspend fun synthesize(@Body request: ChatCompletionRequest): Response<ChatCompletionResponse>

    @POST("chat/completions")
    @Streaming
    suspend fun synthesizeStreaming(@Body request: ChatCompletionRequest): Response<ResponseBody>
}
