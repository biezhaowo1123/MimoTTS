package com.mimotts.data.repository

import com.mimotts.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDataStore: SettingsDataStore) {

    val apiKey: Flow<String> = settingsDataStore.apiKey
    val apiBaseUrl: Flow<String> = settingsDataStore.apiBaseUrl
    val defaultVoiceId: Flow<String> = settingsDataStore.defaultVoiceId
    val speechRate: Flow<Float> = settingsDataStore.speechRate
    val cacheEnabled: Flow<Boolean> = settingsDataStore.cacheEnabled
    val voiceDesignPrompt: Flow<String> = settingsDataStore.voiceDesignPrompt
    val voiceDesignEnabled: Flow<Boolean> = settingsDataStore.voiceDesignEnabled
    val voiceCloneEnabled: Flow<Boolean> = settingsDataStore.voiceCloneEnabled
    val voiceCloneAudioPath: Flow<String> = settingsDataStore.voiceCloneAudioPath
    val persistentMode: Flow<Boolean> = settingsDataStore.persistentMode

    suspend fun setApiKey(value: String) = settingsDataStore.setApiKey(value)
    suspend fun setApiBaseUrl(value: String) = settingsDataStore.setApiBaseUrl(value)
    suspend fun setDefaultVoiceId(value: String) = settingsDataStore.setDefaultVoiceId(value)
    suspend fun setSpeechRate(value: Float) = settingsDataStore.setSpeechRate(value)
    suspend fun setCacheEnabled(value: Boolean) = settingsDataStore.setCacheEnabled(value)
    suspend fun setVoiceDesignPrompt(value: String) = settingsDataStore.setVoiceDesignPrompt(value)
    suspend fun setVoiceDesignEnabled(value: Boolean) = settingsDataStore.setVoiceDesignEnabled(value)
    suspend fun setVoiceCloneEnabled(value: Boolean) = settingsDataStore.setVoiceCloneEnabled(value)
    suspend fun setVoiceCloneAudioPath(value: String) = settingsDataStore.setVoiceCloneAudioPath(value)
    suspend fun setPersistentMode(value: Boolean) = settingsDataStore.setPersistentMode(value)
}
