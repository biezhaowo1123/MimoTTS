package com.mimotts.ui.screen.apikey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mimotts.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApiKeyViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)

    val apiKey = settingsDataStore.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val apiBaseUrl = settingsDataStore.apiBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://api.example.com/")

    fun setApiKey(value: String) {
        viewModelScope.launch { settingsDataStore.setApiKey(value) }
    }

    fun setApiBaseUrl(value: String) {
        viewModelScope.launch { settingsDataStore.setApiBaseUrl(value) }
    }
}
