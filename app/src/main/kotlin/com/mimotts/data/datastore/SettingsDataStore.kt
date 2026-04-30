package com.mimotts.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val DEFAULT_VOICE_ID = stringPreferencesKey("default_voice_id")
        val VOICE_DESIGN_PROMPT = stringPreferencesKey("voice_design_prompt")
        val VOICE_DESIGN_ENABLED = booleanPreferencesKey("voice_design_enabled")
        val VOICE_CLONE_ENABLED = booleanPreferencesKey("voice_clone_enabled")
        val VOICE_CLONE_AUDIO_PATH = stringPreferencesKey("voice_clone_audio_path")
        val PERSISTENT_MODE = booleanPreferencesKey("persistent_mode")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val CACHE_ENABLED = booleanPreferencesKey("cache_enabled")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }
    val apiBaseUrl: Flow<String> = context.dataStore.data.map { it[API_BASE_URL] ?: "https://api.example.com/" }
    val defaultVoiceId: Flow<String> = context.dataStore.data.map { it[DEFAULT_VOICE_ID] ?: "冰糖" }
    val voiceDesignPrompt: Flow<String> = context.dataStore.data.map { it[VOICE_DESIGN_PROMPT] ?: "" }
    val voiceDesignEnabled: Flow<Boolean> = context.dataStore.data.map { it[VOICE_DESIGN_ENABLED] ?: false }
    val voiceCloneEnabled: Flow<Boolean> = context.dataStore.data.map { it[VOICE_CLONE_ENABLED] ?: false }
    val voiceCloneAudioPath: Flow<String> = context.dataStore.data.map { it[VOICE_CLONE_AUDIO_PATH] ?: "" }
    val persistentMode: Flow<Boolean> = context.dataStore.data.map { it[PERSISTENT_MODE] ?: false }
    val speechRate: Flow<Float> = context.dataStore.data.map { it[SPEECH_RATE] ?: 1.0f }
    val cacheEnabled: Flow<Boolean> = context.dataStore.data.map { it[CACHE_ENABLED] ?: true }

    suspend fun setApiKey(value: String) {
        context.dataStore.edit { it[API_KEY] = value }
    }

    suspend fun setApiBaseUrl(value: String) {
        context.dataStore.edit { it[API_BASE_URL] = value }
    }

    suspend fun setDefaultVoiceId(value: String) {
        context.dataStore.edit { it[DEFAULT_VOICE_ID] = value }
    }

    suspend fun setVoiceDesignPrompt(value: String) {
        context.dataStore.edit { it[VOICE_DESIGN_PROMPT] = value }
    }

    suspend fun setVoiceDesignEnabled(value: Boolean) {
        context.dataStore.edit { it[VOICE_DESIGN_ENABLED] = value }
    }

    suspend fun setVoiceCloneEnabled(value: Boolean) {
        context.dataStore.edit { it[VOICE_CLONE_ENABLED] = value }
    }

    suspend fun setVoiceCloneAudioPath(value: String) {
        context.dataStore.edit { it[VOICE_CLONE_AUDIO_PATH] = value }
    }

    suspend fun setPersistentMode(value: Boolean) {
        context.dataStore.edit { it[PERSISTENT_MODE] = value }
    }

    suspend fun setSpeechRate(value: Float) {
        context.dataStore.edit { it[SPEECH_RATE] = value }
    }

    suspend fun setCacheEnabled(value: Boolean) {
        context.dataStore.edit { it[CACHE_ENABLED] = value }
    }
}
