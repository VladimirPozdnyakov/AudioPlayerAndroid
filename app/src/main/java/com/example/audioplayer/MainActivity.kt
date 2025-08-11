package com.example.audioplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import com.example.audioplayer.ui.theme.AudioPlayerTheme
import com.example.audioplayer.ui.theme.ThemeMode
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.shape.RoundedCornerShape

class MainActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.values.any { it }
            if (granted) {
                // Load with saved folders
                val settingsRepo = SettingsRepository(this)
                // This is a quick one-shot read; UI will still update via settings screen later
                lifecycleScope.launch {
                    val folders = settingsRepo.foldersFlow.firstOrNull() ?: emptyList()
                    // Если папки не выбраны, добавляем папку Music по умолчанию
                    val foldersToUse = if (folders.isEmpty()) {
                        val defaultFolder = "content://com.android.externalstorage.documents/tree/primary%3AMusic"
                        settingsRepo.setFolders(listOf(defaultFolder))
                        listOf(defaultFolder)
                    } else {
                        folders
                    }
                    runOnUiThread { viewModel.loadTracks(this@MainActivity, foldersToUse) }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settings by settingsViewModel.uiState.collectAsState()
            val themeMode = when (settings.theme) {
                AppTheme.SYSTEM -> ThemeMode.SYSTEM
                AppTheme.LIGHT -> ThemeMode.LIGHT
                AppTheme.DARK -> ThemeMode.DARK
            }
            AudioPlayerTheme(themeMode = themeMode, accentHex = settings.accentHex) {
                var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Settings
                val ctx = LocalContext.current
                val playerUiState by viewModel.uiState.collectAsState()
                Scaffold(
                    bottomBar = {
                        Column {
                            // Глобальный блок управления треком
                            GlobalPlayerBar(
                                uiState = playerUiState,
                                viewModel = viewModel
                            )
                            
                            NavigationBar(
                                modifier = Modifier.height(72.dp),
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                windowInsets = WindowInsets(0)
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Главная", modifier = Modifier.size(24.dp)) },
                                    label = { Text("Главная") },
                                    alwaysShowLabel = true,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Настройки", modifier = Modifier.size(24.dp)) },
                                    label = { Text("Настройки") },
                                    alwaysShowLabel = true,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    AnimatedContent(
                            targetState = selectedTab,
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            label = "bottom_nav",
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally(
                                        animationSpec = tween(200),
                                        initialOffsetX = { it }
                                    ) togetherWith slideOutHorizontally(
                                        animationSpec = tween(200),
                                        targetOffsetX = { -it / 2 }
                                    )
                                } else {
                                    slideInHorizontally(
                                        animationSpec = tween(200),
                                        initialOffsetX = { -it }
                                    ) togetherWith slideOutHorizontally(
                                        animationSpec = tween(200),
                                        targetOffsetX = { it / 2 }
                                    )
                                }
                            }
                        ) { tab ->
                            when (tab) {
                                0 -> PlayerScreen(viewModel = viewModel)
                                else -> SettingsScreen(
                                    state = settings,
                                    onThemeChange = settingsViewModel::setTheme,
                                    onAccentChange = settingsViewModel::setAccent,
                                    onAddFolder = { newFolder ->
                                        val updated = (settings.folders + newFolder).distinct()
                                        settingsViewModel.setFolders(updated)
                                        viewModel.loadTracks(ctx, updated)
                                    },
                                    onRemoveFolder = { folder ->
                                        val updated = settings.folders.filterNot { it == folder }
                                        // Если удаляем все папки, добавляем папку Music по умолчанию
                                        val finalUpdated = if (updated.isEmpty()) {
                                            listOf("content://com.android.externalstorage.documents/tree/primary%3AMusic")
                                        } else {
                                            updated
                                        }
                                        settingsViewModel.setFolders(finalUpdated)
                                        viewModel.loadTracks(ctx, finalUpdated)
                                    }
                                )
                            }
                        }
                }
            }
        }

        ensurePermissionsAndLoad()
    }

    private fun ensurePermissionsAndLoad() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            permissions += Manifest.permission.READ_MEDIA_AUDIO
            permissions += Manifest.permission.POST_NOTIFICATIONS
        } else {
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermission.launch(permissions.toTypedArray())
    }
}

@Composable
fun PlayerScreen(viewModel: PlayerViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Аудиофайлы", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (uiState.tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет треков (попробуйте добавить папки в настройках)")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.tracks) { track ->
                    val isCurrent = uiState.currentIndex >= 0 && uiState.tracks[uiState.currentIndex].id == track.id
                    val isPlaying = isCurrent && uiState.isPlaying
                    ListItem(
                        modifier = Modifier.clickable {
                            if (isCurrent) {
                                if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
                            } else {
                                viewModel.play(track)
                            }
                        },
                        headlineContent = { Text(track.title, maxLines = 1) },
                        supportingContent = { Text(track.artist ?: "Неизвестен") },
                        trailingContent = {
                            if (isCurrent) {
                                if (isPlaying) {
                                    IconButton(onClick = { viewModel.pause() }) {
                                        Icon(Icons.Outlined.Pause, contentDescription = "Pause")
                                    }
                                } else {
                                    IconButton(onClick = { viewModel.resume() }) {
                                        Icon(Icons.Outlined.PlayArrow, contentDescription = "Resume")
                                    }
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

    }
}
