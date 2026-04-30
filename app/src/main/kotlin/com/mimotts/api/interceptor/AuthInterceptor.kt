package com.mimotts.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val apiKeyProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = apiKeyProvider()
        val request = chain.request().newBuilder()
            .addHeader("api-key", apiKey)
            .build()
        return chain.proceed(request)
    }
}
