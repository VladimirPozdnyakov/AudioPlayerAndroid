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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.foxelectronic.audioplayer.data.model.Track
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
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerTheme
import com.foxelectronic.audioplayer.ui.components.ExpandablePlayer
import com.foxelectronic.audioplayer.ui.settings.SettingsScreen
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
                MainScreen(
                    viewModel = viewModel,
                    settings = settings,
                    settingsViewModel = settingsViewModel
                )
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
    settingsViewModel: SettingsViewModel
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Главная, 1: Настройки
    var expandProgress by remember { mutableFloatStateOf(-1f) }
    val ctx = LocalContext.current
    val playerUiState by viewModel.uiState.collectAsState()
    val hasCurrentTrack = playerUiState.currentIndex >= 0 && playerUiState.tracks.isNotEmpty()

    val navBarHeight = 72.dp
    val density = LocalDensity.current
    val navBarHeightPx = with(density) { navBarHeight.toPx() }

    // Offset для NavigationBar: уезжает вниз при раскрытии плеера
    // При свайпе вниз (expandProgress < 0) остаётся на месте
    val navBarOffset = if (expandProgress > 0f) {
        (expandProgress * navBarHeightPx).roundToInt()
    } else {
        0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Основной контент
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "bottom_nav",
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) togetherWith
                                slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it })
                    } else {
                        slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it })
                    }
                }
            ) { tab ->
                when (tab) {
                    0 -> PlayerScreen(
                        viewModel = viewModel,
                        settingsRepository = settingsViewModel.settingsRepository,
                        expandProgress = expandProgress,
                        onTrackClick = { track ->
                            if (playerUiState.currentIndex >= 0 &&
                                playerUiState.tracks.isNotEmpty() &&
                                playerUiState.tracks[playerUiState.currentIndex].id != track.id) {
                                viewModel.play(track)
                            } else if (playerUiState.currentIndex < 0) {
                                viewModel.play(track)
                            }
                        }
                    )
                    1 -> SettingsScreen(
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

        // NavigationBar внизу с анимацией (zIndex выше когда плеер свёрнут)
        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(if (expandProgress < 0.5f) 5f else 0f)
                .height(navBarHeight)
                .offset { IntOffset(0, navBarOffset) },
            containerColor = MaterialTheme.colorScheme.surface,
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

        // ExpandablePlayer над NavigationBar (zIndex ниже когда свёрнут)
        if (hasCurrentTrack) {
            ExpandablePlayer(
                uiState = playerUiState,
                viewModel = viewModel,
                onExpandProgressChange = { progress -> expandProgress = progress },
                navBarHeight = navBarHeight,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
    expandProgress: Float = 0f,
    onTrackClick: (Track) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // История поиска
    val searchHistory by viewModel.searchHistory.collectAsState()
    var isSearchFocused by remember { mutableStateOf(false) }
    val showHistoryDropdown by remember {
        derivedStateOf {
            isSearchFocused && searchHistory.isNotEmpty() && searchQuery.isEmpty()
        }
    }

    // Загружаем сохранённую вкладку синхронно при первом запуске
    val initialTab = remember {
        kotlinx.coroutines.runBlocking {
            settingsRepository?.selectedTabFlow?.first() ?: 0
        }
    }
    val pagerState = rememberPagerState(initialPage = initialTab, pageCount = { 4 })

    // Создаём LazyListState для каждой вкладки
    val allTracksListState = rememberLazyListState()
    val favoriteTracksListState = rememberLazyListState()
    val artistsListState = rememberLazyListState()
    val albumsListState = rememberLazyListState()

    // Сохраняем вкладку при изменении
    LaunchedEffect(pagerState.currentPage) {
        settingsRepository?.setSelectedTab(pagerState.currentPage)
        // Очищаем выбор при смене вкладки
        if (pagerState.currentPage != 2) viewModel.clearSelectedArtist()
        if (pagerState.currentPage != 3) viewModel.clearSelectedAlbum()
        // Снимаем фокус с поля поиска при переключении вкладок
        focusManager.clearFocus()
    }

    // Обработка навигации "назад" при просмотре деталей исполнителя/альбома
    val isViewingArtistOrAlbum = uiState.selectedArtist != null || uiState.selectedAlbum != null
    BackHandler(enabled = isViewingArtistOrAlbum) {
        if (uiState.selectedArtist != null) {
            viewModel.clearSelectedArtist()
        } else if (uiState.selectedAlbum != null) {
            viewModel.clearSelectedAlbum()
        }
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

    // Группы исполнителей (для вкладки "Исполнители")
    val filteredArtistGroups = remember(uiState.artistGroups, searchQuery, uiState.sortMode) {
        val filtered = if (searchQuery.isEmpty()) {
            uiState.artistGroups
        } else {
            uiState.artistGroups.filter { (artist, _) ->
                artist.contains(searchQuery, ignoreCase = true)
            }
        }
        when (uiState.sortMode) {
            SortMode.ALPHABETICAL_AZ -> filtered.toSortedMap(compareBy { it.lowercase() })
            SortMode.ALPHABETICAL_ZA -> filtered.toSortedMap(compareByDescending { it.lowercase() })
        }
    }

    // Группы альбомов (для вкладки "Альбомы")
    val filteredAlbumGroups = remember(uiState.albumGroups, searchQuery, uiState.sortMode) {
        val filtered = if (searchQuery.isEmpty()) {
            uiState.albumGroups
        } else {
            uiState.albumGroups.filter { (album, _) ->
                album.contains(searchQuery, ignoreCase = true)
            }
        }
        when (uiState.sortMode) {
            SortMode.ALPHABETICAL_AZ -> filtered.toSortedMap(compareBy { it.lowercase() })
            SortMode.ALPHABETICAL_ZA -> filtered.toSortedMap(compareByDescending { it.lowercase() })
        }
    }

    // Измеряем ширину текста для вкладок
    val textMeasurer = rememberTextMeasurer()
    val tab0Text = "Все  ${allTracks.size}"
    val tab1Text = "Любимые  ${favoriteTracks.size}"
    val tab2Text = "Исполнители  ${filteredArtistGroups.size}"
    val tab3Text = "Альбомы  ${filteredAlbumGroups.size}"

    val tab0TextWidth = textMeasurer.measure(
        text = tab0Text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    ).size.width

    val tab1TextWidth = textMeasurer.measure(
        text = tab1Text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    ).size.width

    val tab2TextWidth = textMeasurer.measure(
        text = tab2Text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    ).size.width

    val tab3TextWidth = textMeasurer.measure(
        text = tab3Text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    ).size.width

    Box(modifier = Modifier.fillMaxSize()) {
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
            // Оборачиваем TextField в Box для добавления dropdown
            Box(
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { newQuery ->
                        searchQuery = newQuery
                        // Вызываем debounce функцию для сохранения запроса
                        viewModel.onSearchQueryChanged(newQuery)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isSearchFocused = focusState.isFocused
                        },
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

                // Dropdown с историей поиска
                DropdownMenu(
                    expanded = showHistoryDropdown,
                    onDismissRequest = {
                        isSearchFocused = false
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .heightIn(max = 300.dp)
                ) {
                    // Элементы истории
                    searchHistory.forEach { historyItem ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = historyItem.query,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.removeSearchHistoryItem(historyItem.query)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Удалить",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                searchQuery = historyItem.query
                                isSearchFocused = false
                            }
                        )
                    }

                    // Разделитель перед кнопкой очистки
                    if (searchHistory.isNotEmpty()) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteSweep,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Очистить историю",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            onClick = {
                                viewModel.clearSearchHistory()
                                isSearchFocused = false
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }

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
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    val density = LocalDensity.current
                    val currentTab = tabPositions[pagerState.currentPage]

                    // Определяем целевую страницу и направление
                    val targetPage = if (pagerState.currentPageOffsetFraction > 0) {
                        (pagerState.currentPage + 1).coerceIn(0, tabPositions.size - 1)
                    } else if (pagerState.currentPageOffsetFraction < 0) {
                        (pagerState.currentPage - 1).coerceIn(0, tabPositions.size - 1)
                    } else {
                        pagerState.currentPage
                    }
                    val targetTab = tabPositions[targetPage]

                    // Ширина текста в dp
                    val currentTextWidth = with(density) {
                        when (pagerState.currentPage) {
                            0 -> tab0TextWidth.toDp()
                            1 -> tab1TextWidth.toDp()
                            2 -> tab2TextWidth.toDp()
                            else -> tab3TextWidth.toDp()
                        }
                    }
                    val targetTextWidth = with(density) {
                        when (targetPage) {
                            0 -> tab0TextWidth.toDp()
                            1 -> tab1TextWidth.toDp()
                            2 -> tab2TextWidth.toDp()
                            else -> tab3TextWidth.toDp()
                        }
                    }

                    val fraction = kotlin.math.abs(pagerState.currentPageOffsetFraction)

                    // Центр текущей и целевой вкладок
                    val currentTabCenter = currentTab.left + currentTab.width / 2
                    val targetTabCenter = targetTab.left + targetTab.width / 2

                    // Интерполяция центра и ширины (добавляем 16dp padding)
                    val indicatorPadding = 16.dp
                    val indicatorCenter = currentTabCenter + (targetTabCenter - currentTabCenter) * fraction
                    val indicatorWidth: Dp = currentTextWidth + (targetTextWidth - currentTextWidth) * fraction + indicatorPadding
                    val indicatorLeft = indicatorCenter - indicatorWidth / 2

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = indicatorLeft)
                            .width(width = indicatorWidth)
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            },
            divider = {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
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
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                Text(
                    text = tab0Text,
                    style = if (pagerState.currentPage == 0)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                Text(
                    text = tab1Text,
                    style = if (pagerState.currentPage == 1)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
            Tab(
                selected = pagerState.currentPage == 2,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                Text(
                    text = tab2Text,
                    style = if (pagerState.currentPage == 2)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
            Tab(
                selected = pagerState.currentPage == 3,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(3)
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                Text(
                    text = tab3Text,
                    style = if (pagerState.currentPage == 3)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
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
            val isViewingArtistOrAlbum = uiState.selectedArtist != null || uiState.selectedAlbum != null
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 16.dp,
                userScrollEnabled = !isViewingArtistOrAlbum
            ) { page ->
                when (page) {
                    0 -> TrackList(
                        tracks = allTracks,
                        uiState = uiState,
                        viewModel = viewModel,
                        isPlaylistMode = false,
                        onTrackClick = onTrackClick,
                        emptyMessage = "Нет треков (попробуйте добавить папки в настройках)",
                        listState = allTracksListState,
                        expandProgress = expandProgress,
                        playlistName = "Все треки"
                    )
                    1 -> TrackList(
                        tracks = favoriteTracks,
                        uiState = uiState,
                        viewModel = viewModel,
                        isPlaylistMode = true,
                        onTrackClick = onTrackClick,
                        emptyMessage = "Нет любимых треков",
                        listState = favoriteTracksListState,
                        expandProgress = expandProgress,
                        playlistName = "Любимые треки",
                        playlistType = PlaylistType.FAVORITES
                    )
                    2 -> ArtistsTab(
                        artistGroups = filteredArtistGroups,
                        selectedArtist = uiState.selectedArtist,
                        uiState = uiState,
                        viewModel = viewModel,
                        onTrackClick = onTrackClick,
                        listState = artistsListState,
                        expandProgress = expandProgress
                    )
                    3 -> AlbumsTab(
                        albumGroups = filteredAlbumGroups,
                        selectedAlbum = uiState.selectedAlbum,
                        uiState = uiState,
                        viewModel = viewModel,
                        onTrackClick = onTrackClick,
                        listState = albumsListState,
                        expandProgress = expandProgress
                    )
                }
            }
        }
        }

        // Scrollbar у правого края экрана
        if (!uiState.isLoading) {
            val currentListState = when (pagerState.currentPage) {
                0 -> allTracksListState
                1 -> favoriteTracksListState
                2 -> artistsListState
                else -> albumsListState
            }
            val currentTrackCount = when (pagerState.currentPage) {
                0 -> allTracks.size
                1 -> favoriteTracks.size
                2 -> if (uiState.selectedArtist != null) {
                    filteredArtistGroups[uiState.selectedArtist]?.size ?: 0
                } else {
                    filteredArtistGroups.size
                }
                else -> if (uiState.selectedAlbum != null) {
                    filteredAlbumGroups[uiState.selectedAlbum]?.size ?: 0
                } else {
                    filteredAlbumGroups.size
                }
            }

            // Анимированный padding в зависимости от expandProgress
            // expandProgress >= 0: плеер виден (122dp), expandProgress < 0: плеер скрыт (50dp)
            val targetPadding = if (expandProgress >= 0f) 122.dp else 50.dp
            val bottomPadding by animateDpAsState(
                targetValue = targetPadding,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "scrollbarBottomPadding"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = 156.dp, bottom = bottomPadding, end = 8.dp)
            ) {
                VerticalScrollbar(
                    listState = currentListState,
                    itemCount = currentTrackCount,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
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
    emptyMessage: String,
    listState: LazyListState,
    expandProgress: Float = 0f,
    playlistName: String = "Все треки",
    playlistType: PlaylistType = PlaylistType.ALL
) {
    if (tracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage)
        }
    } else {
        // Анимированный padding в зависимости от expandProgress
        // expandProgress >= 0: плеер виден (122dp), expandProgress < 0: плеер скрыт (50dp)
        val targetPadding = if (expandProgress >= 0f) 122.dp else 50.dp
        val bottomPadding by animateDpAsState(
            targetValue = targetPadding,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "trackListBottomPadding"
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPadding)
        ) {
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
                            // Воспроизводим из текущего плейлиста (все или любимые)
                            viewModel.playFromPlaylist(track, tracks, playlistName, playlistType)
                        }
                        onTrackClick(track)
                    },
                    onPlayPauseClick = {
                        if (isPlaying) viewModel.pause() else viewModel.resume()
                    },
                    onFavoriteClick = { viewModel.toggleFavorite(track) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 200),
                        fadeOutSpec = tween(durationMillis = 200),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
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
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                        text = track.artist ?: "Неизвестный исполнитель",
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

@Composable
fun VerticalScrollbar(
    listState: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    val isScrolling = listState.isScrollInProgress

    // Показываем scrollbar только при прокрутке
    val alpha by animateFloatAsState(
        targetValue = if (isScrolling) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isScrolling) 150 else 500,
            delayMillis = if (isScrolling) 0 else 700
        ),
        label = "scrollbarAlpha"
    )

    if (itemCount > 0) {
        val firstVisibleIndex = listState.firstVisibleItemIndex.toFloat()
        val firstVisibleOffset = listState.firstVisibleItemScrollOffset.toFloat()

        // Примерная высота одного элемента (100dp высота карточки + 4dp padding)
        val itemHeightDp = 104.dp
        val itemHeightPx = with(LocalDensity.current) { itemHeightDp.toPx() }

        // Общая высота контента
        val totalContentHeight = itemCount * itemHeightPx

        Canvas(
            modifier = modifier
                .width(4.dp)
                .fillMaxHeight()
                .graphicsLayer { this.alpha = alpha }
        ) {
                val canvasHeight = size.height
                val canvasWidth = size.width

                // Размер полосы пропорционален видимой области
                val thumbHeight = (canvasHeight * canvasHeight / totalContentHeight)
                    .coerceIn(40f, canvasHeight) // минимум 40px, максимум высота canvas

                // Позиция полосы
                val scrollProgress = (firstVisibleIndex * itemHeightPx + firstVisibleOffset) /
                    (totalContentHeight - canvasHeight).coerceAtLeast(1f)
                val thumbTop = scrollProgress * (canvasHeight - thumbHeight)

            // Рисуем закруглённую полосу
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.5f),
                topLeft = Offset(0f, thumbTop),
                size = Size(canvasWidth, thumbHeight),
                cornerRadius = CornerRadius(canvasWidth / 2, canvasWidth / 2)
            )
        }
    }
}

@Composable
private fun ArtistsTab(
    artistGroups: Map<String, List<Track>>,
    selectedArtist: String?,
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    onTrackClick: (Track) -> Unit,
    listState: LazyListState,
    expandProgress: Float
) {
    val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var previousArtist by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    // Анимация при изменении selectedArtist
    LaunchedEffect(selectedArtist) {
        if (selectedArtist != null && previousArtist == null) {
            // Открытие - анимируем справа налево
            swipeOffset.snapTo(screenWidthPx)
            swipeOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        previousArtist = selectedArtist
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Список исполнителей (задний слой, статичный)
        ArtistGroupList(artistGroups, { viewModel.selectArtist(it) }, listState, expandProgress)

        // Детали исполнителя (передний слой)
        if (selectedArtist != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    // Порог: 30% ширины экрана
                                    val threshold = size.width * 0.3f
                                    if (swipeOffset.value > threshold) {
                                        // Завершаем свайп: анимируем до конца экрана
                                        swipeOffset.animateTo(
                                            targetValue = size.width.toFloat(),
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                        viewModel.clearSelectedArtist()
                                    } else {
                                        // Возвращаем назад
                                        swipeOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    swipeOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    // Синхронизируем с движением пальца
                                    val newValue = (swipeOffset.value + dragAmount).coerceAtLeast(0f)
                                    swipeOffset.snapTo(newValue)
                                }
                            }
                        )
                    }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                swipeOffset.animateTo(
                                    targetValue = screenWidthPx,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                viewModel.clearSelectedArtist()
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.ArrowBack, "Назад", Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedArtist, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                TrackList(
                    tracks = artistGroups[selectedArtist] ?: emptyList(),
                    uiState = uiState,
                    viewModel = viewModel,
                    isPlaylistMode = true,
                    onTrackClick = onTrackClick,
                    emptyMessage = "Нет треков у этого исполнителя",
                    listState = listState,
                    expandProgress = expandProgress,
                    playlistName = selectedArtist,
                    playlistType = PlaylistType.ARTIST
                )
            }
        }
    }
}

@Composable
private fun ArtistGroupList(
    artistGroups: Map<String, List<Track>>,
    onArtistClick: (String) -> Unit,
    listState: LazyListState,
    expandProgress: Float
) {
    if (artistGroups.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет исполнителей")
        }
    } else {
        val bottomPadding by animateDpAsState(
            if (expandProgress >= 0f) 122.dp else 50.dp,
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
            label = "artistListBottomPadding"
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = bottomPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(artistGroups.toList(), key = { (artist, _) -> "artist_$artist" }) { (artist, tracks) ->
                ArtistGroupItem(artist, tracks.size, tracks.firstOrNull()?.albumArtPath, { onArtistClick(artist) })
            }
        }
    }
}

@Composable
private fun ArtistGroupItem(artist: String, trackCount: Int, albumArtPath: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(albumArtPath)
                    .size(256, 256).memoryCachePolicy(CachePolicy.ENABLED).build(),
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                },
                error = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = artist,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "$trackCount ${if (trackCount == 1) "трек" else if (trackCount in 2..4) "трека" else "треков"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlbumsTab(
    albumGroups: Map<String, List<Track>>,
    selectedAlbum: String?,
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    onTrackClick: (Track) -> Unit,
    listState: LazyListState,
    expandProgress: Float
) {
    val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var previousAlbum by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    // Анимация при изменении selectedAlbum
    LaunchedEffect(selectedAlbum) {
        if (selectedAlbum != null && previousAlbum == null) {
            // Открытие - анимируем справа налево
            swipeOffset.snapTo(screenWidthPx)
            swipeOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        previousAlbum = selectedAlbum
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Список альбомов (задний слой, статичный)
        AlbumGroupList(albumGroups, { viewModel.selectAlbum(it) }, listState, expandProgress)

        // Детали альбома (передний слой)
        if (selectedAlbum != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    // Порог: 30% ширины экрана
                                    val threshold = size.width * 0.3f
                                    if (swipeOffset.value > threshold) {
                                        // Завершаем свайп: анимируем до конца экрана
                                        swipeOffset.animateTo(
                                            targetValue = size.width.toFloat(),
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                        viewModel.clearSelectedAlbum()
                                    } else {
                                        // Возвращаем назад
                                        swipeOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    swipeOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    // Синхронизируем с движением пальца
                                    val newValue = (swipeOffset.value + dragAmount).coerceAtLeast(0f)
                                    swipeOffset.snapTo(newValue)
                                }
                            }
                        )
                    }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                swipeOffset.animateTo(
                                    targetValue = screenWidthPx,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                viewModel.clearSelectedAlbum()
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.ArrowBack, "Назад", Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedAlbum, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                TrackList(
                    tracks = albumGroups[selectedAlbum] ?: emptyList(),
                    uiState = uiState,
                    viewModel = viewModel,
                    isPlaylistMode = true,
                    onTrackClick = onTrackClick,
                    emptyMessage = "Нет треков в этом альбоме",
                    listState = listState,
                    expandProgress = expandProgress,
                    playlistName = selectedAlbum,
                    playlistType = PlaylistType.ALBUM
                )
            }
        }
    }
}

@Composable
private fun AlbumGroupList(
    albumGroups: Map<String, List<Track>>,
    onAlbumClick: (String) -> Unit,
    listState: LazyListState,
    expandProgress: Float
) {
    if (albumGroups.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет альбомов")
        }
    } else {
        val bottomPadding by animateDpAsState(
            if (expandProgress >= 0f) 122.dp else 50.dp,
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
            label = "albumListBottomPadding"
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = bottomPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(albumGroups.toList(), key = { (album, _) -> "album_$album" }) { (album, tracks) ->
                val uniqueArtists = tracks.mapNotNull { it.artist }.distinct()
                val artistText = if (uniqueArtists.isEmpty()) {
                    "Неизвестный исполнитель"
                } else {
                    uniqueArtists.joinToString(", ")
                }
                AlbumGroupItem(album, artistText, tracks.size, tracks.firstOrNull()?.albumArtPath, { onAlbumClick(album) })
            }
        }
    }
}

@Composable
private fun AlbumGroupItem(album: String, artist: String, trackCount: Int, albumArtPath: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(albumArtPath)
                    .size(256, 256).memoryCachePolicy(CachePolicy.ENABLED).build(),
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                },
                error = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = album,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "$artist • $trackCount ${if (trackCount == 1) "трек" else if (trackCount in 2..4) "трека" else "треков"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
