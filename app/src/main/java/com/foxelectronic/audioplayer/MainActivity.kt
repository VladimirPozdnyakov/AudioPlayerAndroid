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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import coil.request.ImageRequest
import androidx.compose.runtime.derivedStateOf
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foxelectronic.audioplayer.ui.PlaybackScreen
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerTheme
import com.foxelectronic.audioplayer.ui.theme.ThemeMode
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxelectronic.audioplayer.SortMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay


@AndroidEntryPoint
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
                    runOnUiThread { viewModel.loadTracks(this@MainActivity, settingsRepo, foldersToUse) }
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
            AudioPlayerTheme(themeMode = themeMode, accentHex = settings.accentHex, fontType = settings.fontType) {
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
                        icon = { Icon(Icons.Rounded.Home, contentDescription = "Главная", modifier = Modifier.size(24.dp)) },
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
                        icon = {
                            var rotationState by remember { mutableStateOf(0f) }

                            // Trigger rotation only when navigating to settings (from 0 to 1)
                            LaunchedEffect(selectedTab) {
                                if (selectedTab == 1) {
                                    rotationState += 360f
                                }
                            }

                            val rotation by animateFloatAsState(
                                targetValue = rotationState,
                                animationSpec = tween(durationMillis = 500),
                                label = "rotationAnimation"
                            )

                            Box(
                                modifier = Modifier
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Настройки",
                                    modifier = Modifier
                                        .graphicsLayer {
                                            rotationZ = rotation
                                        }
                                        .fillMaxSize(),
                                    tint = if (selectedTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
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
                    // Плавные анимации перелистывания влево и вправо
                    if (targetState > initialState) {
                        // Переход вправо (с Главной на Настройки)
                        slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) togetherWith
                                slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it })
                    } else {
                        // Переход влево (с Настроек на Главную)
                        slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it })
                    }
                }
            ) { tab ->
                when (tab) {
                    0 -> PlayerScreen(
                        viewModel = viewModel,
                        settingsRepository = settingsViewModel.settingsRepository,
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
                        onFontTypeChange = settingsViewModel::setFontType,
                        onAddFolder = { newFolder ->
                            val updated = (settings.folders + newFolder).distinct()
                            settingsViewModel.setFolders(updated)
                            viewModel.loadTracks(ctx, settingsViewModel.settingsRepository, updated)
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
                            viewModel.loadTracks(ctx, settingsViewModel.settingsRepository, finalUpdated)
                        }
                    )
                }
            }
    }
}

@Composable
fun TrackProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        // Background of the progress bar (showing total duration)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
                )  // More opaque background
        )
        // Progress portion of the bar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))  // Ensure progress is between 0 and 1
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )  // More opaque primary color for progress
        )
    }
}

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    settingsRepository: SettingsRepository? = null,
    onTrackClick: (Track) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Загружаем сохранённую вкладку синхронно при первом запуске
    val initialTab = remember {
        kotlinx.coroutines.runBlocking {
            settingsRepository?.selectedTabFlow?.first() ?: 0
        }
    }
    val pagerState = rememberPagerState(initialPage = initialTab, pageCount = { 2 })

    // Сохраняем вкладку при изменении
    LaunchedEffect(pagerState.currentPage) {
        settingsRepository?.setSelectedTab(pagerState.currentPage)
    }

    // Все треки (для вкладки "Все")
    val allTracks = remember(uiState.allTracks, searchQuery, uiState.sortMode) {
        val filtered = if (searchQuery.isEmpty()) {
            uiState.allTracks
        } else {
            uiState.allTracks.filter { track ->
                track.title.contains(searchQuery, ignoreCase = true) ||
                (track.artist?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
        when (uiState.sortMode) {
            SortMode.ALPHABETICAL_AZ -> filtered.sortedBy { it.title.lowercase() }
            SortMode.ALPHABETICAL_ZA -> filtered.sortedByDescending { it.title.lowercase() }
        }
    }

    // Любимые треки (для вкладки "Любимые")
    val favoriteTracks = remember(uiState.allTracks, searchQuery, uiState.sortMode) {
        val filtered = uiState.allTracks.filter { it.isFavorite }.let { favorites ->
            if (searchQuery.isEmpty()) {
                favorites
            } else {
                favorites.filter { track ->
                    track.title.contains(searchQuery, ignoreCase = true) ||
                    (track.artist?.contains(searchQuery, ignoreCase = true) == true)
                }
            }
        }
        when (uiState.sortMode) {
            SortMode.ALPHABETICAL_AZ -> filtered.sortedBy { it.title.lowercase() }
            SortMode.ALPHABETICAL_ZA -> filtered.sortedByDescending { it.title.lowercase() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )

            IconButton(
                onClick = { viewModel.toggleSortMode() },
                modifier = Modifier.size(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SortByAlpha,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Icon(
                        imageVector = if (uiState.sortMode == SortMode.ALPHABETICAL_AZ) {
                            Icons.Rounded.ArrowDropDown
                        } else {
                            Icons.Rounded.ArrowDropUp
                        },
                        contentDescription = if (uiState.sortMode == SortMode.ALPHABETICAL_AZ) "По алфавиту (А-Я)" else "По алфавиту (Я-А)",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Tabs with swipe support
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            divider = {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
            }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = "Все  ${allTracks.size}",
                    style = if (pagerState.currentPage == 0)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = "Любимые  ${favoriteTracks.size}",
                    style = if (pagerState.currentPage == 1)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 16.dp
            ) { page ->
                when (page) {
                    0 -> TrackList(
                        tracks = allTracks,
                        uiState = uiState,
                        viewModel = viewModel,
                        isPlaylistMode = false,
                        onTrackClick = onTrackClick,
                        emptyMessage = "Нет треков (попробуйте добавить папки в настройках)"
                    )
                    1 -> TrackList(
                        tracks = favoriteTracks,
                        uiState = uiState,
                        viewModel = viewModel,
                        isPlaylistMode = true,
                        onTrackClick = onTrackClick,
                        emptyMessage = "Нет любимых треков"
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    isPlaylistMode: Boolean,
    onTrackClick: (Track) -> Unit,
    emptyMessage: String
) {
    if (tracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = tracks,
                key = { track -> "${if (isPlaylistMode) "fav" else "all"}_${track.id}" }
            ) { track ->
                val isCurrent = uiState.currentIndex >= 0 &&
                    uiState.tracks.isNotEmpty() &&
                    uiState.tracks.getOrNull(uiState.currentIndex)?.id == track.id
                val isPlaying = isCurrent && uiState.isPlaying

                TrackItem(
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isPlaying,
                    uiState = uiState,
                    onTrackClick = {
                        if (isCurrent) {
                            if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
                        } else {
                            if (isPlaylistMode) {
                                // Воспроизводим из плейлиста любимых
                                viewModel.playFromPlaylist(track, tracks)
                            } else {
                                viewModel.play(track)
                            }
                        }
                        onTrackClick(track)
                    },
                    onPlayPauseClick = {
                        if (isPlaying) viewModel.pause() else viewModel.resume()
                    },
                    onFavoriteClick = { viewModel.toggleFavorite(track) }
                )
            }
        }
    }
}

@Composable
private fun TrackItem(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    uiState: PlayerUiState,
    onTrackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isCurrent) {
                val progress = if (uiState.durationMs > 0) {
                    uiState.positionMs.toFloat() / uiState.durationMs.toFloat()
                } else 0f
                TrackProgressBar(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable { onTrackClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(track.albumArtPath)
                            .size(256, 256)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    )

                    if (isCurrent) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val cornerRadius by animateFloatAsState(
                                targetValue = if (isPlaying) 9f else 18f,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                label = "cornerRadius"
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(cornerRadius.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    .clickable { onPlayPauseClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                val scale by animateFloatAsState(
                                    targetValue = if (isPlaying) 1.2f else 1f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    ),
                                    label = "scale"
                                )

                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(scale),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = track.artist ?: "Неизвестен",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedFavoriteButton(
                    isFavorite = track.isFavorite,
                    onToggleFavorite = onFavoriteClick,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    val targetScale = 1f

    IconButton(
        onClick = {
            onToggleFavorite()
            scale = 1.2f // Initial scale for bounce effect
        },
        modifier = modifier
    ) {
        val currentScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "scaleAnimation"
        )

        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .scale(currentScale)
        )
    }

    // Reset scale after animation completes
    LaunchedEffect(scale) {
        if (scale != targetScale) {
            delay(200) // Wait for animation to complete
            scale = targetScale
        }
    }
}
