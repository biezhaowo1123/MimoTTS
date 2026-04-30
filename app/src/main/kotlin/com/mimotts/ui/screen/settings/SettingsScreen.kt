package com.mimotts.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mimotts.engine.PersistentService
import com.mimotts.ui.navigation.Screen
import java.io.File

private data class VoiceOption(val id: String, val label: String)

private val voiceOptions = listOf(
    VoiceOption("冰糖", "冰糖 (中文女声)"),
    VoiceOption("茉莉", "茉莉 (中文女声)"),
    VoiceOption("苏打", "苏打 (中文男声)"),
    VoiceOption("白桦", "白桦 (中文男声)"),
    VoiceOption("Mia", "Mia (英文女声)"),
    VoiceOption("Chloe", "Chloe (英文女声)"),
    VoiceOption("Milo", "Milo (英文男声)"),
    VoiceOption("Dean", "Dean (英文男声)")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val defaultVoiceId by viewModel.defaultVoiceId.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val cacheEnabled by viewModel.cacheEnabled.collectAsState()
    val voiceDesignPrompt by viewModel.voiceDesignPrompt.collectAsState()
    val voiceDesignEnabled by viewModel.voiceDesignEnabled.collectAsState()
    val voiceCloneEnabled by viewModel.voiceCloneEnabled.collectAsState()
    val voiceCloneAudioPath by viewModel.voiceCloneAudioPath.collectAsState()
    val persistentMode by viewModel.persistentMode.collectAsState()

    val context = LocalContext.current

    var voiceMenuExpanded by remember { mutableStateOf(false) }
    var voiceText by remember { mutableStateOf(defaultVoiceId) }

    // 文件选择器
    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 复制文件到应用内部存储
            val inputStream = context.contentResolver.openInputStream(it)
            val fileName = "voice_clone_${System.currentTimeMillis()}.wav"
            val destFile = File(context.filesDir, fileName)
            inputStream?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.setVoiceCloneAudioPath(destFile.absolutePath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MimoTTS 设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("API 配置") },
                supportingContent = { Text("设置 API Key 和服务器地址") },
                leadingContent = { Icon(Icons.Default.Key, contentDescription = null) },
                modifier = Modifier.clickable { navController.navigate(Screen.ApiKey.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "语音设置",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 音色选择（支持自定义输入）
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = voiceMenuExpanded,
                    onExpandedChange = { voiceMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = voiceText,
                        onValueChange = { newValue ->
                            voiceText = newValue
                            viewModel.setDefaultVoiceId(newValue)
                        },
                        label = { Text("默认音色") },
                        supportingText = { Text("可选择内置音色或输入自定义音色名称") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = voiceMenuExpanded,
                        onDismissRequest = { voiceMenuExpanded = false }
                    ) {
                        voiceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    voiceText = option.id
                                    viewModel.setDefaultVoiceId(option.id)
                                    voiceMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("语速: %.1fx".format(speechRate))
                Slider(
                    value = speechRate,
                    onValueChange = { viewModel.setSpeechRate(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 14
                )
            }

            ListItem(
                headlineContent = { Text("启用缓存") },
                supportingContent = { Text("缓存合成音频以减少 API 调用") },
                trailingContent = {
                    Switch(
                        checked = cacheEnabled,
                        onCheckedChange = { viewModel.setCacheEnabled(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("常驻后台") },
                supportingContent = { Text("防止系统杀死 TTS 引擎（显示通知）") },
                trailingContent = {
                    Switch(
                        checked = persistentMode,
                        onCheckedChange = { enabled ->
                            viewModel.setPersistentMode(enabled)
                            if (enabled) {
                                PersistentService.start(context)
                            } else {
                                PersistentService.stop(context)
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "音色设计",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ListItem(
                headlineContent = { Text("启用音色设计") },
                supportingContent = { Text("使用文字描述创建自定义音色（替代内置音色）") },
                trailingContent = {
                    Switch(
                        checked = voiceDesignEnabled,
                        onCheckedChange = { viewModel.setVoiceDesignEnabled(it) }
                    )
                }
            )

            if (voiceDesignEnabled) {
                var promptText by remember(voiceDesignPrompt) { mutableStateOf(voiceDesignPrompt) }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { newValue ->
                            promptText = newValue
                            viewModel.setVoiceDesignPrompt(newValue)
                        },
                        label = { Text("音色描述") },
                        supportingText = {
                            Text(
                                "描述想要的音色特征，如\"温柔的年轻女声\"、\"低沉磁性的男声\"",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "音色克隆",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ListItem(
                headlineContent = { Text("启用音色克隆") },
                supportingContent = { Text("使用音频样本克隆音色（优先级高于音色设计）") },
                trailingContent = {
                    Switch(
                        checked = voiceCloneEnabled,
                        onCheckedChange = { viewModel.setVoiceCloneEnabled(it) }
                    )
                }
            )

            if (voiceCloneEnabled) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val fileName = if (voiceCloneAudioPath.isNotBlank()) {
                        File(voiceCloneAudioPath).name
                    } else {
                        "未选择音频文件"
                    }
                    Text(
                        "音频样本: $fileName",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    androidx.compose.material3.Button(
                        onClick = { audioFilePicker.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (voiceCloneAudioPath.isNotBlank()) "更换音频文件" else "选择音频文件 (mp3/wav)")
                    }
                    if (voiceCloneAudioPath.isNotBlank()) {
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.setVoiceCloneAudioPath("") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("清除音频文件")
                        }
                    }
                }
            }
        }
    }
}
