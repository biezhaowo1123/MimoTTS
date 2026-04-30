package com.mimotts.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mimotts.data.datastore.SettingsDataStore
import com.mimotts.engine.PersistentService
import com.mimotts.ui.navigation.NavGraph
import com.mimotts.ui.theme.MimoTTSTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查是否启用常驻后台模式
        val settingsDataStore = SettingsDataStore(this)
        val persistentMode = runBlocking {
            settingsDataStore.persistentMode.first()
        }
        if (persistentMode) {
            PersistentService.start(this)
        }

        enableEdgeToEdge()
        setContent {
            MimoTTSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
