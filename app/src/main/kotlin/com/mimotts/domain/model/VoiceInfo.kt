package com.mimotts.domain.model

data class VoiceInfo(
    val id: String,
    val name: String,
    val locale: String,
    val gender: String,
    val description: String,
    val category: String? = null
)
