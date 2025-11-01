package com.foxelectronic.audioplayer

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foxelectronic.audioplayer.ui.PlaybackScreen
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerTheme
import com.foxelectronic.audioplayer.ui.theme.ThemeMode
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize

class MainActivity : ComponentActivity() {
    private val viewModel: PlayerViewModel by viewModels()

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.values.any { it }
            if (granted) {
                // Загрузка с сохранёнными папками
                val settingsRepo = SettingsRepository(this)
                // This is a quick one-shot read; UI will still update via settings screen later
                lifecycleScope.launch {
                    val folders = settingsRepo.foldersFlow.firstOrNull() ?: emptyList()
                    // Если папки не выбраны, добавляется папка Music по умолчанию
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
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            viewModel = viewModel,
                            settings = settings,
                            settingsViewModel = settingsViewModel,
                            navController = navController
                        )
                    }
                    composable("playback") {
                        PlaybackScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
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
fun MainScreen(
    viewModel: PlayerViewModel,
    settings: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    navController: androidx.navigation.NavController
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Главная, 1: Настройки
    val ctx = LocalContext.current
    val playerUiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                // Глобальный блок управления треком
                GlobalPlayerBar(
                    uiState = playerUiState,
                    viewModel = viewModel,
                    onMiniPlayerClick = { navController.navigate("playback") }
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
                    // Простое затухание для анимации перехода
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }
            ) { tab ->
                when (tab) {
                    0 -> PlayerScreen(
                        viewModel = viewModel,
                        onTrackClick = { track ->
                            // Play the track if needed, but don't navigate to playback screen
                            if (playerUiState.currentIndex >= 0 &&
                                playerUiState.tracks.isNotEmpty() &&
                                playerUiState.tracks[playerUiState.currentIndex].id != track.id) {
                                viewModel.play(track)
                            }
                        }
                    )
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
                            // Если удаляем все папки, добавляется папка Music по умолчанию
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

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onTrackClick: (Track) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Аудиофайлы", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Анимированный индикатор загрузки
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
        } else if (uiState.tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет треков (попробуйте добавить папки в настройках)")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    items = uiState.tracks,
                    key = { track -> track.id }
                ) { track ->
                    val isCurrent = uiState.currentIndex >= 0 && uiState.tracks[uiState.currentIndex].id == track.id
                    val isPlaying = isCurrent && uiState.isPlaying

                    // Анимация при нажатии на трек
                    var isSelected by remember { mutableStateOf(false) }
                    val itemScale by animateFloatAsState(
                        targetValue = if (isSelected) 0.95f else 1f,
                        animationSpec = tween(
                            durationMillis = 150,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        label = "itemScale"
                    )

                    // Состояние для анимации при клике
                    val itemElevation by animateDpAsState(
                        targetValue = if (isSelected) 4.dp else 0.dp,
                        animationSpec = tween(
                            durationMillis = 150,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        label = "itemElevation"
                    )

                    // Анимация появления элемента при скроллинге
                    val animationItem by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        label = "animationItem"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .scale(itemScale)
                            .graphicsLayer {
                                alpha = animationItem
                                translationY = if (animationItem == 1f) 0f else 50f
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isSelected = true
                                        tryAwaitRelease()
                                        isSelected = false
                                    }
                                )
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = itemElevation)
                    ) {
                        ListItem(
                            modifier = Modifier.clickable {
                                if (isCurrent) {
                                    if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
                                } else {
                                    viewModel.play(track)
                                }
                                onTrackClick(track) // Navigate to playback screen
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
                    }
                    HorizontalDivider()
                }
            }
        }

    }
}
