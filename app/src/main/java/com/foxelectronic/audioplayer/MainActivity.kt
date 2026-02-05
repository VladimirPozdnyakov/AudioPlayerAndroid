package com.foxelectronic.audioplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.*
import com.foxelectronic.audioplayer.data.model.Playlist
import com.foxelectronic.audioplayer.data.model.PlaylistWithTrackCount
import com.foxelectronic.audioplayer.ui.playlist.dialogs.CreatePlaylistDialog
import com.foxelectronic.audioplayer.ui.playlist.dialogs.AddToPlaylistDialog
import com.foxelectronic.audioplayer.ui.playlist.dialogs.EditMetadataDialog
import com.foxelectronic.audioplayer.ui.playlist.dialogs.DeletePlaylistDialog
import com.foxelectronic.audioplayer.ui.playlist.dialogs.RenamePlaylistDialog
import com.foxelectronic.audioplayer.ui.components.AudioFormatBadge
import com.foxelectronic.audioplayer.ui.components.DetailedFormatInfo
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.components.ModernEmptyState
import com.foxelectronic.audioplayer.ui.components.ModernSelectionItem
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
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
import com.foxelectronic.audioplayer.ui.components.ModernNavigationBar
import com.foxelectronic.audioplayer.ui.theme.ThemeMode
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxelectronic.audioplayer.SortMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
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
import com.foxelectronic.audioplayer.update.UpdateChecker
import com.foxelectronic.audioplayer.network.GitHubRelease
import com.foxelectronic.audioplayer.ui.update.UpdateDialog
import android.net.Uri
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: PlayerViewModel by viewModels()
    private var pendingExternalUri: android.net.Uri? = null

    @Inject
    lateinit var updateChecker: UpdateChecker

    // Флаг для предотвращения повторных проверок обновлений в рамках одной сессии
    private var updateCheckPerformed = false

    // Состояние для доступного обновления
    private val availableUpdateState = mutableStateOf<GitHubRelease?>(null)

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
                    runOnUiThread {
                        viewModel.loadTracks(this@MainActivity, settingsRepo, foldersToUse)
                        // Воспроизводим отложенный внешний файл после инициализации плеера
                        pendingExternalUri?.let { uri ->
                            viewModel.playExternalFile(this@MainActivity, uri)
                            pendingExternalUri = null
                        }
                    }
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

            // Используем состояние обновления из activity
            val availableUpdate by availableUpdateState

            AudioPlayerTheme(themeMode = themeMode, accentHex = settings.accentHex, fontType = settings.fontType) {
                MainScreen(
                    viewModel = viewModel,
                    settings = settings,
                    settingsViewModel = settingsViewModel,
                    availableUpdate = availableUpdate,
                    onDismissUpdate = { availableUpdateState.value = null },
                    onDownloadUpdate = { url ->
                        // Открываем URL релиза в браузере
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(browserIntent)
                    }
                )
            }
        }

        // Сохраняем URI из intent для воспроизведения после инициализации
        handleExternalAudioIntent(intent)
        ensurePermissionsAndLoad()

        // Проверка обновлений при первом запуске
        checkForUpdates()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // При новом intent плеер уже инициализирован, воспроизводим сразу
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                viewModel.playExternalFile(this, uri)
            }
        }
    }

    private fun handleExternalAudioIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            pendingExternalUri = intent.data
        }
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

    /**
     * Проверяет наличие обновлений приложения при первом запуске
     */
    private fun checkForUpdates() {
        // Предотвращаем повторные проверки в рамках одной сессии
        if (updateCheckPerformed) {
            android.util.Log.d("UpdateCheck", "Проверка обновлений уже выполнялась в этой сессии")
            return
        }
        updateCheckPerformed = true

        lifecycleScope.launch {
            try {
                // Проверяем, включена ли проверка обновлений в настройках
                val settingsRepo = SettingsRepository(this@MainActivity)
                val checkEnabled = settingsRepo.checkUpdatesEnabledFlow.firstOrNull() ?: true

                android.util.Log.d("UpdateCheck", "Статус проверки обновлений в настройках: $checkEnabled")

                if (!checkEnabled) {
                    android.util.Log.d("UpdateCheck", "Проверка обновлений отключена пользователем")
                    return@launch
                }

                // Выполняем проверку обновлений
                // GitHub repo: vladimirpozdnyakov/AudioPlayerAndroid
                android.util.Log.d("UpdateCheck", "Начинаем проверку обновлений, текущая версия: ${BuildConfig.VERSION_NAME}")

                val release = updateChecker.checkForUpdate(
                    currentVersion = BuildConfig.VERSION_NAME,
                    owner = "vladimirpozdnyakov",
                    repo = "AudioPlayerAndroid"
                )

                // Если доступно обновление, показываем диалог
                if (release != null) {
                    android.util.Log.d("UpdateCheck", "Найдено обновление: ${release.tagName}")
                    availableUpdateState.value = release
                } else {
                    android.util.Log.d("UpdateCheck", "Обновление не найдено или текущая версия актуальна")
                }
            } catch (e: Exception) {
                // Ошибки обрабатываются молча - не показываем пользователю
                android.util.Log.e("UpdateCheck", "Ошибка при проверке обновлений: ${e.message}", e)
            }
        }
    }

}

private fun applyLanguage(language: AppLanguage) {
    val localeTag = when (language) {
        AppLanguage.ENGLISH -> "en"
        AppLanguage.RUSSIAN -> "ru"
    }
    val localeList = LocaleListCompat.forLanguageTags(localeTag)
    AppCompatDelegate.setApplicationLocales(localeList)
}

@Composable
fun MainScreen(
    viewModel: PlayerViewModel,
    settings: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    availableUpdate: GitHubRelease? = null,
    onDismissUpdate: () -> Unit = {},
    onDownloadUpdate: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Главная, 1: Настройки
    var expandProgress by remember { mutableFloatStateOf(-1f) }
    val ctx = LocalContext.current
    val playerUiState by viewModel.uiState.collectAsState()
    val hasCurrentTrack = playerUiState.currentIndex >= 0 && playerUiState.tracks.isNotEmpty()

    val navBarHeight = 80.dp
    val density = LocalDensity.current
    val navBarHeightPx = with(density) { navBarHeight.toPx() }

    // Состояния для диалогов (общие для списка треков и большого плеера)
    var showAddToPlaylistDialogFromPlayer by remember { mutableStateOf(false) }
    var showEditMetadataDialogFromPlayer by remember { mutableStateOf(false) }
    var playlistsContainingTrack by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // Состояние для навигации к исполнителю и альбому
    var artistToNavigate by remember { mutableStateOf<String?>(null) }
    var albumToNavigate by remember { mutableStateOf<String?>(null) }

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
                        },
                        navigateToArtist = artistToNavigate,
                        onArtistNavigated = { artistToNavigate = null },
                        navigateToAlbum = albumToNavigate,
                        onAlbumNavigated = { albumToNavigate = null },
                        onGoToArtist = { artist -> artistToNavigate = artist },
                        onGoToAlbum = { album -> albumToNavigate = album }
                    )
                    1 -> SettingsScreen(
                        state = settings,
                        onThemeChange = settingsViewModel::setTheme,
                        onAccentChange = settingsViewModel::setAccent,
                        onFontTypeChange = settingsViewModel::setFontType,
                        onLanguageChange = { language ->
                            settingsViewModel.setLanguage(language)
                            applyLanguage(language)
                        },
                        onAudioQualityChange = settingsViewModel::setAudioQuality,
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
                        },
                        onCheckUpdatesChange = settingsViewModel::setCheckUpdatesEnabled
                    )
                }
            }
        }

        // Современная нижняя навигация
        ModernNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            expandProgress = expandProgress,
            navBarHeightPx = navBarHeightPx,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // ExpandablePlayer над NavigationBar (zIndex ниже когда свёрнут)
        if (hasCurrentTrack) {
            ExpandablePlayer(
                uiState = playerUiState,
                viewModel = viewModel,
                onExpandProgressChange = { progress -> expandProgress = progress },
                navBarHeight = navBarHeight,
                modifier = Modifier.align(Alignment.BottomCenter),
                onAddToPlaylistClick = {
                    showAddToPlaylistDialogFromPlayer = true
                },
                onEditInfoClick = {
                    showEditMetadataDialogFromPlayer = true
                },
                onArtistClick = { artist ->
                    // Переключаемся на главную вкладку (PlayerScreen)
                    selectedTab = 0
                    // Устанавливаем исполнителя для навигации
                    artistToNavigate = artist
                },
                onAlbumClick = { album ->
                    // Переключаемся на главную вкладку (PlayerScreen)
                    selectedTab = 0
                    // Устанавливаем альбом для навигации
                    albumToNavigate = album
                }
            )
        }
    }

    // Диалоги для большого плеера
    val currentTrack = if (playerUiState.currentIndex >= 0 && playerUiState.tracks.isNotEmpty()) {
        playerUiState.tracks[playerUiState.currentIndex]
    } else null

    // Диалог добавления в плейлист
    if (showAddToPlaylistDialogFromPlayer && currentTrack != null) {
        LaunchedEffect(currentTrack) {
            playlistsContainingTrack = viewModel.getPlaylistsContainingTrack(currentTrack.id)
        }

        com.foxelectronic.audioplayer.ui.playlist.dialogs.AddToPlaylistDialog(
            playlists = playerUiState.customPlaylists,
            trackId = currentTrack.id,
            playlistsContainingTrack = playlistsContainingTrack,
            onDismiss = {
                showAddToPlaylistDialogFromPlayer = false
                playlistsContainingTrack = emptySet()
            },
            onPlaylistSelected = { playlistWithCount ->
                viewModel.addTrackToPlaylist(playlistWithCount.playlistId, currentTrack.id)
            },
            onPlaylistRemoved = { playlistWithCount ->
                viewModel.removeTrackFromPlaylist(playlistWithCount.playlistId, currentTrack.id)
            },
            onCreateNewPlaylist = {
                showAddToPlaylistDialogFromPlayer = false
                // Можно добавить логику создания нового плейлиста
            }
        )
    }

    // Диалог редактирования метаданных
    if (showEditMetadataDialogFromPlayer && currentTrack != null) {
        com.foxelectronic.audioplayer.ui.playlist.dialogs.EditMetadataDialog(
            track = currentTrack,
            onDismiss = {
                showEditMetadataDialogFromPlayer = false
            },
            onSave = { title, artist, album, coverUri, writeToFile, onComplete ->
                viewModel.updateTrackMetadata(
                    track = currentTrack,
                    title = title,
                    artist = artist,
                    album = album,
                    coverImageUri = coverUri,
                    writeToFile = writeToFile,
                    onResult = { _ ->
                        onComplete()
                    }
                )
            }
        )
    }

    // Диалог обновления приложения
    if (availableUpdate != null) {
        com.foxelectronic.audioplayer.ui.update.UpdateDialog(
            release = availableUpdate,
            onDownload = onDownloadUpdate,
            onDismiss = onDismissUpdate
        )
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
    onTrackClick: (Track) -> Unit = {},
    navigateToArtist: String? = null,
    onArtistNavigated: () -> Unit = {},
    navigateToAlbum: String? = null,
    onAlbumNavigated: () -> Unit = {},
    onGoToArtist: (String) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {}
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
    val pagerState = rememberPagerState(initialPage = initialTab, pageCount = { 5 })

    // Создаём LazyListState для каждой вкладки
    val allTracksListState = rememberLazyListState()
    val favoriteTracksListState = rememberLazyListState()
    val artistsListState = rememberLazyListState()
    val albumsListState = rememberLazyListState()
    val playlistsListState = rememberLazyListState()

    // Сохраняем вкладку при изменении
    LaunchedEffect(pagerState.currentPage) {
        settingsRepository?.setSelectedTab(pagerState.currentPage)
        // Очищаем выбор при смене вкладки
        if (pagerState.currentPage != 2) viewModel.clearSelectedArtist()
        if (pagerState.currentPage != 3) viewModel.clearSelectedAlbum()
        if (pagerState.currentPage != 4) viewModel.clearSelectedCustomPlaylist()
        // Снимаем фокус с поля поиска при переключении вкладок
        focusManager.clearFocus()
    }

    // Обработка навигации "назад" при просмотре деталей исполнителя/альбома/плейлиста
    val isViewingDetails = uiState.selectedArtist != null || uiState.selectedAlbum != null || uiState.selectedCustomPlaylist != null
    BackHandler(enabled = isViewingDetails) {
        when {
            uiState.selectedArtist != null -> viewModel.clearSelectedArtist()
            uiState.selectedAlbum != null -> viewModel.clearSelectedAlbum()
            uiState.selectedCustomPlaylist != null -> viewModel.clearSelectedCustomPlaylist()
        }
    }

    // Обработка навигации к исполнителю из плеера
    LaunchedEffect(navigateToArtist) {
        if (navigateToArtist != null) {
            // Переходим на вкладку исполнителей
            pagerState.animateScrollToPage(2)
            // Выбираем исполнителя
            viewModel.selectArtist(navigateToArtist)
            // Сбрасываем флаг навигации
            onArtistNavigated()
        }
    }

    // Обработка навигации к альбому
    LaunchedEffect(navigateToAlbum) {
        if (navigateToAlbum != null) {
            // Переходим на вкладку альбомов
            pagerState.animateScrollToPage(3)
            // Выбираем альбом
            viewModel.selectAlbum(navigateToAlbum)
            // Сбрасываем флаг навигации
            onAlbumNavigated()
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
    val tabAllLabel = stringResource(R.string.tab_all)
    val tabFavoritesLabel = stringResource(R.string.tab_favorites)
    val tabArtistsLabel = stringResource(R.string.tab_artists)
    val tabAlbumsLabel = stringResource(R.string.tab_albums)
    val tabPlaylistsLabel = stringResource(R.string.tab_playlists)

    val tab0Text = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(tabAllLabel)
        }
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append("${allTracks.size}")
        }
    }
    val tab1Text = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(tabFavoritesLabel)
        }
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append("${favoriteTracks.size}")
        }
    }
    val tab2Text = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(tabArtistsLabel)
        }
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append("${filteredArtistGroups.size}")
        }
    }
    val tab3Text = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(tabAlbumsLabel)
        }
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append("${filteredAlbumGroups.size}")
        }
    }
    val tab4Text = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(tabPlaylistsLabel)
        }
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append("${uiState.customPlaylists.size}")
        }
    }

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

    val tab4TextWidth = textMeasurer.measure(
        text = tab4Text,
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
                val extendedColors = AudioPlayerThemeExtended.colors

                OutlinedTextField(
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = extendedColors.cardBackground,
                        unfocusedContainerColor = extendedColors.cardBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = extendedColors.cardBorder
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = extendedColors.iconTint
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = extendedColors.iconTint
                                )
                            }
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.subtleText
                        )
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
                        .background(extendedColors.cardBackground, RoundedCornerShape(12.dp))
                ) {
                    // Элементы истории
                    searchHistory.forEach { historyItem ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
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
                                            tint = extendedColors.iconTint
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
                                            contentDescription = stringResource(R.string.btn_delete),
                                            modifier = Modifier.size(18.dp),
                                            tint = extendedColors.subtleText
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
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = extendedColors.divider
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
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
                                        text = stringResource(R.string.clear_history),
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

            // Modern sort button with icon container styling
            val sortInteractionSource = remember { MutableInteractionSource() }
            val isSortPressed by sortInteractionSource.collectIsPressedAsState()
            val sortScale by animateFloatAsState(
                targetValue = if (isSortPressed) 0.92f else 1f,
                animationSpec = tween(durationMillis = 100),
                label = "sortButtonScale"
            )
            val sortExtendedColors = AudioPlayerThemeExtended.colors

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(sortScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sortExtendedColors.accentSoft)
                    .clickable(
                        interactionSource = sortInteractionSource,
                        indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                        onClick = { viewModel.toggleSortMode() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SortByAlpha,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Icon(
                        imageVector = if (uiState.sortMode == SortMode.ALPHABETICAL_AZ) {
                            Icons.Rounded.ArrowDropDown
                        } else {
                            Icons.Rounded.ArrowDropUp
                        },
                        contentDescription = if (uiState.sortMode == SortMode.ALPHABETICAL_AZ) stringResource(R.string.sort_az) else stringResource(R.string.sort_za),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Modern card-based tabs with swipe support
        val tabExtendedColors = AudioPlayerThemeExtended.colors

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tabExtendedColors.cardBackground)
                .border(1.dp, tabExtendedColors.cardBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                edgePadding = 8.dp,
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
                                3 -> tab3TextWidth.toDp()
                                else -> tab4TextWidth.toDp()
                            }
                        }
                        val targetTextWidth = with(density) {
                            when (targetPage) {
                                0 -> tab0TextWidth.toDp()
                                1 -> tab1TextWidth.toDp()
                                2 -> tab2TextWidth.toDp()
                                3 -> tab3TextWidth.toDp()
                                else -> tab4TextWidth.toDp()
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
                divider = {}
            ) {
                ModernTab(
                    text = tab0Text,
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                ModernTab(
                    text = tab1Text,
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                ModernTab(
                    text = tab2Text,
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                )
                ModernTab(
                    text = tab3Text,
                    selected = pagerState.currentPage == 3,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    }
                )
                ModernTab(
                    text = tab4Text,
                    selected = pagerState.currentPage == 4,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(4)
                        }
                    }
                )
            }
        }

        // Определяем bottom padding в зависимости от expandProgress
        val bottomPadding by animateDpAsState(
            if (expandProgress >= 0f) 122.dp else 50.dp,
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
            label = "contentBottomPadding"
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 16.dp,
            userScrollEnabled = !isViewingDetails
        ) { page ->
            // Плавный переход от skeleton к контенту
            Crossfade(
                targetState = uiState.isLoading,
                animationSpec = tween(durationMillis = 400),
                label = "skeleton_to_content_$page"
            ) { isLoading ->
                if (isLoading) {
                    // Skeleton-анимации во время загрузки
                    when (page) {
                        0, 1 -> {
                            // Skeleton для списков треков (Все, Избранное)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = bottomPadding),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(10) { index ->
                                    val delay = index * 100
                                    com.foxelectronic.audioplayer.ui.components.TrackItemSkeleton(
                                        showPlayButton = index == 0,
                                        delayMillis = delay
                                    )
                                }
                            }
                        }
                        2 -> {
                            // Skeleton для артистов (сетка)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp, bottom = bottomPadding),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(12) { index ->
                                    val delay = (index / 2) * 150
                                    com.foxelectronic.audioplayer.ui.components.CardSkeleton(
                                        showCountBadge = false,
                                        delayMillis = delay
                                    )
                                }
                            }
                        }
                        3 -> {
                            // Skeleton для альбомов (сетка 3 колонки)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp, bottom = bottomPadding),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(15) { index ->
                                    val delay = (index / 3) * 150
                                    com.foxelectronic.audioplayer.ui.components.CardSkeleton(
                                        showCountBadge = true,
                                        delayMillis = delay
                                    )
                                }
                            }
                        }
                        4 -> {
                            // Skeleton для плейлистов (сетка 2 колонки)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp, bottom = bottomPadding),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(10) { index ->
                                    val delay = (index / 2) * 150
                                    com.foxelectronic.audioplayer.ui.components.CardSkeleton(
                                        showCountBadge = true,
                                        delayMillis = delay
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Основной контент
                    when (page) {
                        0 -> TrackList(
                            tracks = allTracks,
                            uiState = uiState,
                            viewModel = viewModel,
                            isPlaylistMode = false,
                            onTrackClick = onTrackClick,
                            emptyMessage = stringResource(R.string.empty_no_tracks),
                            emptyIcon = Icons.Rounded.MusicNote,
                            listState = allTracksListState,
                            expandProgress = expandProgress,
                            playlistName = stringResource(R.string.all_tracks),
                            onGoToArtist = onGoToArtist,
                            onGoToAlbum = onGoToAlbum
                        )
                        1 -> TrackList(
                            tracks = favoriteTracks,
                            uiState = uiState,
                            viewModel = viewModel,
                            isPlaylistMode = true,
                            onTrackClick = onTrackClick,
                            emptyMessage = stringResource(R.string.empty_no_favorites),
                            emptyIcon = Icons.Rounded.FavoriteBorder,
                            listState = favoriteTracksListState,
                            expandProgress = expandProgress,
                            playlistName = stringResource(R.string.tab_favorites),
                            playlistType = PlaylistType.FAVORITES,
                            onGoToArtist = onGoToArtist,
                            onGoToAlbum = onGoToAlbum
                        )
                        2 -> ArtistsTab(
                            artistGroups = filteredArtistGroups,
                            selectedArtist = uiState.selectedArtist,
                            uiState = uiState,
                            viewModel = viewModel,
                            onTrackClick = onTrackClick,
                            listState = artistsListState,
                            expandProgress = expandProgress,
                            onGoToArtist = onGoToArtist,
                            onGoToAlbum = onGoToAlbum
                        )
                        3 -> AlbumsTab(
                            albumGroups = filteredAlbumGroups,
                            selectedAlbum = uiState.selectedAlbum,
                            uiState = uiState,
                            viewModel = viewModel,
                            onTrackClick = onTrackClick,
                            listState = albumsListState,
                            expandProgress = expandProgress,
                            onGoToArtist = onGoToArtist,
                            onGoToAlbum = onGoToAlbum
                        )
                        4 -> PlaylistsTab(
                            playlists = uiState.customPlaylists,
                            selectedPlaylist = uiState.selectedCustomPlaylist,
                            playlistTracks = uiState.customPlaylistTracks,
                            uiState = uiState,
                            viewModel = viewModel,
                            onTrackClick = onTrackClick,
                            listState = playlistsListState,
                            expandProgress = expandProgress,
                            onGoToArtist = onGoToArtist,
                            onGoToAlbum = onGoToAlbum
                        )
                    }
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
                3 -> albumsListState
                else -> playlistsListState
            }
            val currentTrackCount = when (pagerState.currentPage) {
                0 -> allTracks.size
                1 -> favoriteTracks.size
                2 -> if (uiState.selectedArtist != null) {
                    filteredArtistGroups[uiState.selectedArtist]?.size ?: 0
                } else {
                    filteredArtistGroups.size
                }
                3 -> if (uiState.selectedAlbum != null) {
                    filteredAlbumGroups[uiState.selectedAlbum]?.size ?: 0
                } else {
                    filteredAlbumGroups.size
                }
                else -> if (uiState.selectedCustomPlaylist != null) {
                    uiState.customPlaylistTracks.size
                } else {
                    uiState.customPlaylists.size
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
    emptyIcon: ImageVector = Icons.Rounded.MusicNote,
    listState: LazyListState,
    expandProgress: Float = 0f,
    playlistName: String = "Все треки",
    playlistType: PlaylistType = PlaylistType.ALL,
    onGoToArtist: (String) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {}
) {
    // Состояния для диалогов
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showEditMetadataDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForDialog by remember { mutableStateOf<Track?>(null) }
    var playlistsContainingTrack by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // Диалог добавления в плейлист
    if (showAddToPlaylistDialog && selectedTrackForDialog != null) {
        // Загружаем список плейлистов, содержащих трек
        LaunchedEffect(selectedTrackForDialog) {
            playlistsContainingTrack = viewModel.getPlaylistsContainingTrack(selectedTrackForDialog!!.id)
        }

        AddToPlaylistDialog(
            playlists = uiState.customPlaylists,
            trackId = selectedTrackForDialog!!.id,
            playlistsContainingTrack = playlistsContainingTrack,
            onDismiss = {
                showAddToPlaylistDialog = false
                selectedTrackForDialog = null
                playlistsContainingTrack = emptySet()
            },
            onPlaylistSelected = { playlistWithCount ->
                viewModel.addTrackToPlaylist(playlistWithCount.playlistId, selectedTrackForDialog!!.id)
            },
            onPlaylistRemoved = { playlistWithCount ->
                viewModel.removeTrackFromPlaylist(playlistWithCount.playlistId, selectedTrackForDialog!!.id)
            },
            onCreateNewPlaylist = {
                showAddToPlaylistDialog = false
                showCreatePlaylistDialog = true
            }
        )
    }

    // Диалог создания плейлиста (после выбора "Создать новый" в диалоге добавления)
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = {
                showCreatePlaylistDialog = false
                selectedTrackForDialog = null
            },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                // После создания плейлиста трек будет добавлен при следующем открытии диалога
            }
        )
    }

    // Диалог редактирования метаданных
    if (showEditMetadataDialog && selectedTrackForDialog != null) {
        EditMetadataDialog(
            track = selectedTrackForDialog!!,
            onDismiss = {
                showEditMetadataDialog = false
                selectedTrackForDialog = null
            },
            onSave = { title, artist, album, coverUri, writeToFile, onComplete ->
                android.util.Log.d("MainActivity", "Calling updateTrackMetadata: title=$title, artist=$artist, album=$album")
                viewModel.updateTrackMetadata(
                    track = selectedTrackForDialog!!,
                    title = title,
                    artist = artist,
                    album = album,
                    coverImageUri = coverUri,
                    writeToFile = writeToFile,
                    onResult = { success ->
                        // Вызываем callback после завершения
                        android.util.Log.d("MainActivity", "updateTrackMetadata completed: success=$success")
                        onComplete()
                    }
                )
            }
        )
    }

    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            ModernEmptyState(
                icon = emptyIcon,
                title = emptyMessage
            )
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomPadding)
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
                    viewModel = viewModel,
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
                    onAddToPlaylistClick = {
                        selectedTrackForDialog = track
                        showAddToPlaylistDialog = true
                    },
                    onEditInfoClick = {
                        selectedTrackForDialog = track
                        showEditMetadataDialog = true
                    },
                    onRemoveFromPlaylistClick = {
                        // Удаляем трек из текущего пользовательского плейлиста
                        uiState.selectedCustomPlaylist?.let { playlist ->
                            viewModel.removeTrackFromPlaylist(playlist.playlistId, track.id)
                        }
                    },
                    showRemoveFromPlaylist = playlistType == PlaylistType.CUSTOM_PLAYLIST,
                    onGoToArtist = onGoToArtist,
                    onGoToAlbum = onGoToAlbum,
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
    viewModel: PlayerViewModel,
    onTrackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit = {},
    onEditInfoClick: () -> Unit = {},
    onRemoveFromPlaylistClick: () -> Unit = {},
    showRemoveFromPlaylist: Boolean = false,
    onGoToArtist: (String) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showArtistSelectionDialog by remember { mutableStateOf(false) }
    var showAudioInfoDialog by remember { mutableStateOf(false) }

    // Парсинг исполнителей
    val artists = remember(track.artist) {
        track.artist?.let { artistString ->
            artistString
                .split(Regex("[,;]|\\s+feat\\.?\\s+|\\s+ft\\.?\\s+|\\s+&\\s+|\\s+and\\s+|\\s+featuring\\s+", RegexOption.IGNORE_CASE))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } ?: emptyList()
    }

    val extendedColors = AudioPlayerThemeExtended.colors

    // Анимация нажатия
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "trackItemScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.cardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder)
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
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                        onClick = onTrackClick
                    )
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
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(extendedColors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = extendedColors.iconTint,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(extendedColors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = extendedColors.iconTint,
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
                                    .background(extendedColors.accentSoft)
                                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(cornerRadius.dp))
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
                                    tint = if (isPlaying) MaterialTheme.colorScheme.primary else extendedColors.iconTint
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = track.artist ?: stringResource(R.string.unknown_artist),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Compact Audio Format Badge
                    val audioFormat by viewModel.getAudioFormat(track.id).collectAsState(initial = null)
                    audioFormat?.let {
                        AudioFormatBadge(
                            format = it,
                            compact = true,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                AnimatedFavoriteButton(
                    isFavorite = track.isFavorite,
                    onToggleFavorite = onFavoriteClick,
                    modifier = Modifier.size(32.dp)
                )

                // Кнопка меню
                Box {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(extendedColors.cardBackgroundElevated)
                            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.menu),
                            tint = extendedColors.iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(extendedColors.cardBackground, RoundedCornerShape(12.dp))
                            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                    ) {
                        if (showRemoveFromPlaylist) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_remove_from_playlist)) },
                                onClick = {
                                    showMenu = false
                                    onRemoveFromPlaylistClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = extendedColors.iconTint
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_add_to_playlist)) },
                            onClick = {
                                showMenu = false
                                onAddToPlaylistClick()
                            },
                            leadingIcon = {
                                @Suppress("DEPRECATION")
                                Icon(
                                    imageVector = Icons.Rounded.PlaylistAdd,
                                    contentDescription = null,
                                    tint = extendedColors.iconTint
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_edit)) },
                            onClick = {
                                showMenu = false
                                onEditInfoClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = extendedColors.iconTint
                                )
                            }
                        )
                        // Перейти к исполнителю
                        if (track.artist != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_go_to_artist)) },
                                onClick = {
                                    showMenu = false
                                    if (artists.size > 1) {
                                        showArtistSelectionDialog = true
                                    } else if (artists.isNotEmpty()) {
                                        onGoToArtist(artists.first())
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = extendedColors.iconTint
                                    )
                                }
                            )
                        }
                        // Перейти к альбому
                        if (track.album != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_go_to_album)) },
                                onClick = {
                                    showMenu = false
                                    onGoToAlbum(track.album)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Album,
                                        contentDescription = null,
                                        tint = extendedColors.iconTint
                                    )
                                }
                            )
                        }
                        // Информация об аудио
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_audio_info)) },
                            onClick = {
                                showMenu = false
                                showAudioInfoDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = extendedColors.iconTint
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Диалог выбора исполнителя
    if (showArtistSelectionDialog && artists.size > 1) {
        ModernDialog(
            onDismissRequest = { showArtistSelectionDialog = false },
            title = stringResource(R.string.dialog_select_artist),
            dismissText = stringResource(R.string.btn_cancel),
            onDismiss = { showArtistSelectionDialog = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                artists.forEach { artist ->
                    ModernSelectionItem(
                        title = artist,
                        selected = false,
                        onClick = {
                            showArtistSelectionDialog = false
                            onGoToArtist(artist)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }

    // Диалог информации об аудио
    if (showAudioInfoDialog) {
        val audioFormat by viewModel.getAudioFormat(track.id).collectAsState(initial = null)
        val trackDuration = if (isCurrent) uiState.durationMs else 0L
        ModernDialog(
            onDismissRequest = { showAudioInfoDialog = false },
            title = stringResource(R.string.audio_format_details),
            confirmText = stringResource(R.string.btn_ok),
            onConfirm = { showAudioInfoDialog = false }
        ) {
            DetailedFormatInfo(
                format = audioFormat,
                trackTitle = track.title,
                trackArtist = track.artist,
                trackAlbum = track.album,
                durationMs = trackDuration,
                onArtistClick = { artist ->
                    showAudioInfoDialog = false
                    onGoToArtist(artist)
                },
                onAlbumClick = track.album?.let { album ->
                    { _: String ->
                        showAudioInfoDialog = false
                        onGoToAlbum(album)
                    }
                }
            )
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
            contentDescription = if (isFavorite) stringResource(R.string.menu_remove_from_favorites) else stringResource(R.string.menu_add_to_favorites),
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
    expandProgress: Float,
    onGoToArtist: (String) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {}
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
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedArtist, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                TrackList(
                    tracks = artistGroups[selectedArtist] ?: emptyList(),
                    uiState = uiState,
                    viewModel = viewModel,
                    isPlaylistMode = true,
                    onTrackClick = onTrackClick,
                    emptyMessage = stringResource(R.string.empty_artist_tracks),
                    emptyIcon = Icons.Rounded.Person,
                    listState = listState,
                    expandProgress = expandProgress,
                    playlistName = selectedArtist,
                    playlistType = PlaylistType.ARTIST,
                    onGoToArtist = onGoToArtist,
                    onGoToAlbum = onGoToAlbum
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun ArtistGroupList(
    artistGroups: Map<String, List<Track>>,
    onArtistClick: (String) -> Unit,
    listState: LazyListState,
    expandProgress: Float
) {
    if (artistGroups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            ModernEmptyState(
                icon = Icons.Rounded.Person,
                title = stringResource(R.string.empty_no_artists)
            )
        }
    } else {
        val bottomPadding by animateDpAsState(
            if (expandProgress >= 0f) 122.dp else 50.dp,
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
            label = "artistListBottomPadding"
        )
        val unknownArtistText = stringResource(R.string.unknown_artist)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = bottomPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(artistGroups.toList(), key = { (artist, _) -> "artist_$artist" }) { (artist, tracks) ->
                val displayArtist = if (artist == PlayerViewModel.UNKNOWN_ARTIST_KEY) unknownArtistText else artist
                ArtistGroupItem(displayArtist, tracks.size, tracks.firstOrNull()?.albumArtPath, { onArtistClick(artist) })
            }
        }
    }
}

/**
 * Современная карточка артиста с мягкими анимациями
 */
@Composable
private fun ArtistGroupItem(artist: String, trackCount: Int, albumArtPath: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "artistCardScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.cardBackground)
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .padding(dimens.cardPaddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(albumArtPath)
                .size(256, 256).memoryCachePolicy(CachePolicy.ENABLED).build(),
            contentDescription = "Album Art",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
                        .background(extendedColors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimens.iconSizeLarge)
                    )
                }
            },
            error = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
                        .background(extendedColors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimens.iconSizeLarge)
                    )
                }
            }
        )
        Spacer(Modifier.height(dimens.itemSpacing))
        Text(
            text = artist,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = pluralStringResource(R.plurals.track_count, trackCount, trackCount),
            style = MaterialTheme.typography.bodySmall,
            color = extendedColors.subtleText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
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
    expandProgress: Float,
    onGoToArtist: (String) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {}
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
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedAlbum, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                TrackList(
                    tracks = albumGroups[selectedAlbum] ?: emptyList(),
                    uiState = uiState,
                    viewModel = viewModel,
                    isPlaylistMode = true,
                    onTrackClick = onTrackClick,
                    emptyMessage = stringResource(R.string.empty_album_tracks),
                    emptyIcon = Icons.Rounded.Album,
                    listState = listState,
                    expandProgress = expandProgress,
                    playlistName = selectedAlbum,
                    playlistType = PlaylistType.ALBUM,
                    onGoToArtist = onGoToArtist,
                    onGoToAlbum = onGoToAlbum
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun AlbumGroupList(
    albumGroups: Map<String, List<Track>>,
    onAlbumClick: (String) -> Unit,
    listState: LazyListState,
    expandProgress: Float
) {
    if (albumGroups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            ModernEmptyState(
                icon = Icons.Rounded.Album,
                title = stringResource(R.string.empty_no_albums)
            )
        }
    } else {
        val bottomPadding by animateDpAsState(
            if (expandProgress >= 0f) 122.dp else 50.dp,
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
            label = "albumListBottomPadding"
        )
        val unknownArtistText = stringResource(R.string.unknown_artist)
        val unknownAlbumText = stringResource(R.string.unknown_album)
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
                    unknownArtistText
                } else {
                    uniqueArtists.joinToString(", ")
                }
                val displayAlbum = if (album == PlayerViewModel.UNKNOWN_ALBUM_KEY) unknownAlbumText else album
                AlbumGroupItem(displayAlbum, artistText, tracks.size, tracks.firstOrNull()?.albumArtPath, { onAlbumClick(album) })
            }
        }
    }
}

/**
 * Modern album card with soft press animation following SettingsMenuButton pattern
 */
@Composable
private fun AlbumGroupItem(album: String, artist: String, trackCount: Int, albumArtPath: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "albumCardScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.cardBackground)
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
            .padding(dimens.cardPaddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(albumArtPath)
                .size(256, 256).memoryCachePolicy(CachePolicy.ENABLED).build(),
            contentDescription = "Album Art",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(extendedColors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            },
            error = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(extendedColors.accentSoft),
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
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.bodySmall,
            color = extendedColors.subtleText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = pluralStringResource(R.plurals.track_count, trackCount, trackCount),
            style = MaterialTheme.typography.bodySmall,
            color = extendedColors.subtleText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlaylistsTab(
    playlists: List<PlaylistWithTrackCount>,
    selectedPlaylist: Playlist?,
    playlistTracks: List<Track>,
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    onTrackClick: (Track) -> Unit,
    listState: LazyListState,
    expandProgress: Float,
    onGoToArtist: (String) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {}
) {
    val swipeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var previousPlaylist by remember { mutableStateOf<Playlist?>(null) }
    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    // Состояния для диалогов
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val extendedColors = AudioPlayerThemeExtended.colors

    // Анимация при изменении selectedPlaylist
    LaunchedEffect(selectedPlaylist) {
        if (selectedPlaylist != null && previousPlaylist == null) {
            swipeOffset.snapTo(screenWidthPx)
            swipeOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        previousPlaylist = selectedPlaylist
    }

    // Диалог создания плейлиста
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Список плейлистов (задний слой)
        PlaylistGroupList(
            playlists = playlists,
            onPlaylistClick = { playlistWithCount ->
                val playlist = Playlist(
                    playlistId = playlistWithCount.playlistId,
                    name = playlistWithCount.name,
                    coverImagePath = playlistWithCount.coverImagePath,
                    createdAt = playlistWithCount.createdAt,
                    updatedAt = playlistWithCount.updatedAt
                )
                viewModel.selectCustomPlaylist(playlist)
            },
            listState = listState,
            expandProgress = expandProgress,
            onCreatePlaylistClick = { showCreateDialog = true }
        )

        // Детали плейлиста (передний слой)
        if (selectedPlaylist != null) {
            // Диалог переименования
            if (showRenameDialog) {
                RenamePlaylistDialog(
                    currentName = selectedPlaylist.name,
                    onDismiss = { showRenameDialog = false },
                    onRename = { newName ->
                        viewModel.renamePlaylist(selectedPlaylist, newName)
                    }
                )
            }

            // Диалог удаления
            if (showDeleteDialog) {
                DeletePlaylistDialog(
                    playlistName = selectedPlaylist.name,
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        viewModel.deletePlaylist(selectedPlaylist)
                        viewModel.clearSelectedCustomPlaylist()
                    }
                )
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    val threshold = size.width * 0.3f
                                    if (swipeOffset.value > threshold) {
                                        swipeOffset.animateTo(
                                            targetValue = size.width.toFloat(),
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                        viewModel.clearSelectedCustomPlaylist()
                                    } else {
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
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                swipeOffset.animateTo(
                                    targetValue = screenWidthPx,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                viewModel.clearSelectedCustomPlaylist()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), Modifier.size(24.dp))
                    }
                    Text(
                        text = selectedPlaylist.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // Кнопка меню плейлиста
                    var showPlaylistMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showPlaylistMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, stringResource(R.string.menu))
                        }
                        DropdownMenu(
                            expanded = showPlaylistMenu,
                            onDismissRequest = { showPlaylistMenu = false },
                            modifier = Modifier
                                .background(extendedColors.cardBackground, RoundedCornerShape(12.dp))
                                .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.btn_rename)) },
                                onClick = {
                                    showPlaylistMenu = false
                                    showRenameDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Edit, null, tint = extendedColors.iconTint)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.btn_delete)) },
                                onClick = {
                                    showPlaylistMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Close, null, tint = extendedColors.iconTint)
                                }
                            )
                        }
                    }
                }
                TrackList(
                    tracks = playlistTracks,
                    uiState = uiState,
                    viewModel = viewModel,
                    isPlaylistMode = true,
                    onTrackClick = onTrackClick,
                    emptyMessage = stringResource(R.string.empty_playlist),
                    emptyIcon = Icons.AutoMirrored.Rounded.QueueMusic,
                    listState = listState,
                    expandProgress = expandProgress,
                    playlistName = selectedPlaylist.name,
                    playlistType = PlaylistType.CUSTOM_PLAYLIST,
                    onGoToArtist = onGoToArtist,
                    onGoToAlbum = onGoToAlbum
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun PlaylistGroupList(
    playlists: List<PlaylistWithTrackCount>,
    onPlaylistClick: (PlaylistWithTrackCount) -> Unit,
    listState: LazyListState,
    expandProgress: Float,
    onCreatePlaylistClick: () -> Unit
) {
    val bottomPadding by animateDpAsState(
        if (expandProgress >= 0f) 122.dp else 50.dp,
        spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
        label = "playlistListBottomPadding"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Существующие плейлисты
        items(playlists, key = { it.playlistId }) { playlist ->
            PlaylistItem(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) }
            )
        }

        // Карточка создания нового плейлиста
        item {
            CreatePlaylistCard(
                onClick = onCreatePlaylistClick
            )
        }
    }
}

/**
 * Modern playlist card with card-based styling and animations
 */
@Composable
private fun PlaylistItem(
    playlist: PlaylistWithTrackCount,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "playlistCardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.cardBackground)
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.cardPaddingSmall),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Обложка плейлиста
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(extendedColors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.coverImagePath != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(playlist.coverImagePath)
                            .size(256, 256)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = stringResource(R.string.playlist_cover),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = playlist.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = pluralStringResource(R.plurals.track_count, playlist.trackCount, playlist.trackCount),
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.subtleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Modern create playlist card with card-based styling and animations
 */
@Composable
private fun CreatePlaylistCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "createPlaylistCardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.cardBackground)
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.cardPaddingSmall),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Плюс вместо обложки
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.create_new),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Modern tab item with card-based styling and animations
 */
@Composable
private fun ModernTab(
    text: androidx.compose.ui.text.AnnotatedString,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "tabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) extendedColors.accentSoft else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else extendedColors.subtleText,
        animationSpec = tween(200),
        label = "tabContent"
    )

    Tab(
        selected = selected,
        onClick = onClick,
        interactionSource = interactionSource,
        selectedContentColor = MaterialTheme.colorScheme.primary,
        unselectedContentColor = extendedColors.subtleText,
        modifier = modifier
            .padding(horizontal = 2.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
        )
    }
}
