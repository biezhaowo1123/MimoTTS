package com.mimotts.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mimotts.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)

    val defaultVoiceId = settingsDataStore.defaultVoiceId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "冰糖")

    val speechRate = settingsDataStore.speechRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val cacheEnabled = settingsDataStore.cacheEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val voiceDesignPrompt = settingsDataStore.voiceDesignPrompt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val voiceDesignEnabled = settingsDataStore.voiceDesignEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val voiceCloneEnabled = settingsDataStore.voiceCloneEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val voiceCloneAudioPath = settingsDataStore.voiceCloneAudioPath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val persistentMode = settingsDataStore.persistentMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDefaultVoiceId(value: String) {
        viewModelScope.launch { settingsDataStore.setDefaultVoiceId(value) }
    }

    fun setSpeechRate(value: Float) {
        viewModelScope.launch { settingsDataStore.setSpeechRate(value) }
    }

    fun setCacheEnabled(value: Boolean) {
        viewModelScope.launch { settingsDataStore.setCacheEnabled(value) }
    }

    fun setVoiceDesignPrompt(value: String) {
        viewModelScope.launch { settingsDataStore.setVoiceDesignPrompt(value) }
    }

    fun setVoiceDesignEnabled(value: Boolean) {
        viewModelScope.launch { settingsDataStore.setVoiceDesignEnabled(value) }
    }

    fun setVoiceCloneEnabled(value: Boolean) {
        viewModelScope.launch { settingsDataStore.setVoiceCloneEnabled(value) }
    }

    fun setVoiceCloneAudioPath(value: String) {
        viewModelScope.launch { settingsDataStore.setVoiceCloneAudioPath(value) }
    }

    fun setPersistentMode(value: Boolean) {
        viewModelScope.launch { settingsDataStore.setPersistentMode(value) }
    }
}
