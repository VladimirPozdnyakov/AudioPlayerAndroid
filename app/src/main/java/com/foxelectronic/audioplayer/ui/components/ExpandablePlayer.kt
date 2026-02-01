package com.foxelectronic.audioplayer.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.foxelectronic.audioplayer.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.foxelectronic.audioplayer.PlayerUiState
import com.foxelectronic.audioplayer.PlayerViewModel
import com.foxelectronic.audioplayer.PlaylistType
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ExpandablePlayer(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    onExpandProgressChange: (Float) -> Unit = {},
    navBarHeight: Dp = 72.dp,
    modifier: Modifier = Modifier,
    onAddToPlaylistClick: () -> Unit = {},
    onEditInfoClick: () -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    onAlbumClick: (String) -> Unit = {}
) {
    if (uiState.currentIndex < 0 || uiState.tracks.isEmpty()) return

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val collapsedHeightDp = 120.dp // 80dp контент + 40dp padding (8dp сверху и 32dp снизу)

    val screenHeightPx = with(density) { screenHeightDp.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeightDp.toPx() }
    val navBarHeightPx = with(density) { navBarHeight.toPx() }

    // Позиция мини-плеера: от низа экрана вверх на navBarHeight + collapsedHeight
    // expandProgress: -1 = скрыт, 0 = свёрнут (мини-плеер над навбаром), 1 = развёрнут (весь экран)
    val coroutineScope = rememberCoroutineScope()

    // animationProgress: -1 = скрыт, 0 = свёрнут, 1 = развёрнут
    // Начинаем с -1 (скрыт) для плавного появления
    val animationProgress = remember { Animatable(-1f) }

    val expandProgress by remember {
        derivedStateOf { animationProgress.value }
    }

    // Уведомляем о изменении expandProgress
    LaunchedEffect(expandProgress) {
        onExpandProgressChange(expandProgress)
    }

    // Анимация появления при первом рендере
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Функции анимации
    fun animateToExpanded() {
        coroutineScope.launch {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    fun animateToCollapsed() {
        coroutineScope.launch {
            animationProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    fun animateToDismissed() {
        coroutineScope.launch {
            animationProgress.animateTo(
                targetValue = -1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            // После завершения анимации останавливаем воспроизведение
            viewModel.stopPlayback()
        }
    }

    // Back handler для развёрнутого плеера
    BackHandler(enabled = expandProgress > 0.5f) {
        animateToCollapsed()
    }

    // Расчёт позиций на основе expandProgress
    // expandProgress: -1 = скрыт (уехал вниз), 0 = свёрнут (мини-плеер), 1 = развёрнут (весь экран)

    // Высота компонента
    val collapsedTotalHeight = collapsedHeightPx + navBarHeightPx
    val visibleHeight = when {
        expandProgress >= 0f -> {
            // От свёрнутого до развёрнутого
            collapsedTotalHeight + (screenHeightPx - collapsedTotalHeight) * expandProgress
        }
        else -> {
            // При expandProgress < 0 уменьшаем высоту
            collapsedTotalHeight * (1f + expandProgress)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { visibleHeight.toDp() })
            .pointerInput(Unit) {
                val velocityTracker = VelocityTracker()
                detectDragGestures(
                    onDragStart = { velocityTracker.resetTracking() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                        // Свайп вверх увеличивает progress, вниз уменьшает
                        val delta = -dragAmount.y / screenHeightPx
                        coroutineScope.launch {
                            val newProgress = (animationProgress.value + delta).coerceIn(-1f, 1f)
                            animationProgress.snapTo(newProgress)
                        }
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().y
                        val velocityThreshold = 500f

                        when {
                            // Быстрый свайп вниз при свёрнутом состоянии - закрыть плеер
                            velocity > velocityThreshold && expandProgress < 0.3f -> {
                                animateToDismissed()
                            }
                            // Быстрый свайп вверх - раскрыть
                            velocity < -velocityThreshold -> {
                                animateToExpanded()
                            }
                            // Быстрый свайп вниз - свернуть или закрыть
                            velocity > velocityThreshold -> {
                                if (expandProgress >= 0f) {
                                    // Если мы на мини-плеере или выше - свернуть к мини-плееру
                                    animateToCollapsed()
                                } else {
                                    // Только если тянули вниз от мини-плеера - закрыть
                                    animateToDismissed()
                                }
                            }
                            // Медленный свайп - определяем по позиции
                            expandProgress < -0.3f -> animateToDismissed()  // Свайп вниз от мини-плеера
                            expandProgress > 0.3f -> animateToExpanded()     // 30%
                            expandProgress < 0f -> animateToCollapsed()      // Между -0.3 и 0
                            else -> animateToCollapsed()                     // По умолчанию свернуть
                        }
                    }
                )
            }
            .zIndex(if (expandProgress > 0.5f) 10f else 1f)
    ) {
        // Развёрнутый плеер (появляется при развёртывании)
        if (expandProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ExpandedPlayerContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    alpha = expandProgress.coerceIn(0f, 1f),
                    onCollapseClick = { animateToCollapsed() },
                    onAddToPlaylistClick = onAddToPlaylistClick,
                    onEditInfoClick = onEditInfoClick,
                    onArtistClick = { artist ->
                        animateToCollapsed()
                        onArtistClick(artist)
                    },
                    onAlbumClick = { album ->
                        animateToCollapsed()
                        onAlbumClick(album)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Мини-плеер (поверх всего для взаимодействия)
        // expandProgress = 1: уезжает вверх, expandProgress = -1: уезжает вниз
        val miniPlayerOffset = -expandProgress * collapsedHeightPx
        CollapsedPlayerContent(
            uiState = uiState,
            viewModel = viewModel,
            alpha = (1f - abs(expandProgress)).coerceIn(0f, 1f),
            onExpandClick = { animateToExpanded() },
            modifier = Modifier
                .fillMaxWidth()
                .height(collapsedHeightDp)
                .offset { IntOffset(0, miniPlayerOffset.roundToInt()) }
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollapsedPlayerContent(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    alpha: Float,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alpha < 0.01f) return

    val extendedColors = AudioPlayerThemeExtended.colors
    val context = LocalContext.current

    val progress = if (uiState.durationMs > 0) {
        (uiState.positionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Pager для анимации переключения треков
    val pagerState = rememberPagerState(
        initialPage = if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) uiState.currentIndex else 0,
        pageCount = { uiState.tracks.size.coerceAtLeast(1) }
    )

    // Синхронизируем pagerState с currentIndex
    LaunchedEffect(uiState.currentIndex) {
        if (uiState.currentIndex >= 0 &&
            uiState.currentIndex < uiState.tracks.size &&
            pagerState.currentPage != uiState.currentIndex) {
            pagerState.animateScrollToPage(uiState.currentIndex)
        }
    }

    // Анимация нажатия
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "miniPlayerScale"
    )

    Box(
        modifier = modifier
            .alpha(alpha)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onExpandClick
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Обложка альбома
                val currentTrack = uiState.tracks.getOrNull(uiState.currentIndex)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(extendedColors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentTrack?.albumArtPath)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        loading = {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Анимированная информация о треке
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = false
                ) { page ->
                    val track = uiState.tracks.getOrNull(page)
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                alpha = 1f - (kotlin.math.abs(pageOffset) * 1.5f).coerceIn(0f, 1f),
                                translationX = pageOffset * 50f
                            ),
                        verticalArrangement = Arrangement.Center
                    ) {
                        track?.let {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = it.artist ?: stringResource(R.string.unknown_artist),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = extendedColors.subtleText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопки управления
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Кнопка Previous
                    MiniPlayerControlButton(
                        icon = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        onClick = { viewModel.previous() }
                    )

                    // Кнопка Play/Pause
                    MiniPlayerPlayPauseButton(
                        isPlaying = uiState.isPlaying,
                        onToggle = { if (uiState.isPlaying) viewModel.pause() else viewModel.resume() }
                    )

                    // Кнопка Next
                    MiniPlayerControlButton(
                        icon = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        onClick = { viewModel.next() }
                    )
                }
            }

            // Современный прогресс-бар
            MiniPlayerProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun MiniPlayerControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "controlButtonScale"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun MiniPlayerPlayPauseButton(
    isPlaying: Boolean,
    onToggle: () -> Unit
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "playPauseScale"
    )

    Box(
        modifier = Modifier
            .size(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(extendedColors.accentSoft)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.primary),
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun MiniPlayerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = extendedColors.cardBorder

    Canvas(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        val width = size.width
        val height = size.height

        // Фон трека
        drawRoundRect(
            color = trackColor,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2, height / 2)
        )

        // Прогресс
        if (progress > 0f) {
            drawRoundRect(
                color = primaryColor,
                size = size.copy(width = width * progress),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2, height / 2)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedPlayerContent(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    alpha: Float,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToPlaylistClick: () -> Unit = {},
    onEditInfoClick: () -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    onAlbumClick: (String) -> Unit = {}
) {
    val currentTrack = uiState.tracks.getOrNull(uiState.currentIndex)
    var showFullscreenArt by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showArtistSelectionDialog by remember { mutableStateOf(false) }
    var showAudioInfoDialog by remember { mutableStateOf(false) }

    // Парсинг исполнителей (разделители: запятая, точка с запятой, feat., ft., &, and, featuring)
    val artists = remember(currentTrack?.artist) {
        currentTrack?.artist?.let { artistString ->
            artistString
                .split(Regex("[,;]|\\s+feat\\.?\\s+|\\s+ft\\.?\\s+|\\s+&\\s+|\\s+and\\s+|\\s+featuring\\s+", RegexOption.IGNORE_CASE))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } ?: emptyList()
    }

    val extendedColors = AudioPlayerThemeExtended.colors

    Box(
        modifier = modifier.alpha(alpha)
    ) {
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Album Art
            ExpandedAlbumArt(
                uiState = uiState,
                viewModel = viewModel,
                onArtClick = { showFullscreenArt = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Track Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = currentTrack?.title ?: "No Track Playing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentTrack?.artist ?: stringResource(R.string.unknown_artist),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (currentTrack?.artist != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .then(
                            if (currentTrack?.artist != null) {
                                Modifier.clickable {
                                    if (artists.size > 1) {
                                        showArtistSelectionDialog = true
                                    } else if (artists.isNotEmpty()) {
                                        onArtistClick(artists.first())
                                    }
                                }
                            } else {
                                Modifier
                            }
                        )
                )

                // Audio Format Badge
                currentTrack?.let { track ->
                    val audioFormat by viewModel.getAudioFormat(track.id).collectAsState(initial = null)
                    audioFormat?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        FormattedAudioInfo(
                            format = it,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Контролы внизу
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 30.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpandedProgressSlider(uiState = uiState, viewModel = viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            ExpandedPlaybackControls(uiState = uiState, viewModel = viewModel)
        }

        // Название плейлиста по центру на уровне кнопки "назад"
        val playlistTypeLabel = when (uiState.playlistType) {
            PlaylistType.ARTIST -> stringResource(R.string.playlist_type_artist)
            PlaylistType.ALBUM -> stringResource(R.string.playlist_type_album)
            PlaylistType.CUSTOM_PLAYLIST -> stringResource(R.string.playlist_type_playlist)
            else -> null
        }
        val playlistText = if (playlistTypeLabel != null) {
            "$playlistTypeLabel\n«${uiState.playlistName}»"
        } else {
            uiState.playlistName
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.now_playing),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = playlistText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Кнопка «назад» поверх всего контента
        val backInteractionSource = remember { MutableInteractionSource() }
        val isBackPressed by backInteractionSource.collectIsPressedAsState()
        val backScale by animateFloatAsState(
            targetValue = if (isBackPressed) 0.9f else 1f,
            animationSpec = tween(100),
            label = "backButtonScale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp)
                .offset(y = (-4).dp)
                .scale(backScale)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = backInteractionSource,
                    indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                    onClick = onCollapseClick
                )
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = stringResource(R.string.back),
                tint = extendedColors.iconTint,
                modifier = Modifier.size(32.dp)
            )
        }

        // Кнопка меню в правом верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp)
                .offset(y = (-4).dp)
        ) {
            val menuInteractionSource = remember { MutableInteractionSource() }
            val isMenuPressed by menuInteractionSource.collectIsPressedAsState()
            val menuScale by animateFloatAsState(
                targetValue = if (isMenuPressed) 0.9f else 1f,
                animationSpec = tween(100),
                label = "menuButtonScale"
            )

            Box(
                modifier = Modifier
                    .scale(menuScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = menuInteractionSource,
                        indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                        onClick = { showMenu = true }
                    )
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.menu),
                    tint = extendedColors.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .background(extendedColors.cardBackground, RoundedCornerShape(12.dp))
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
            ) {
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
                    text = { Text(stringResource(R.string.dialog_edit_metadata)) },
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
                if (currentTrack?.artist != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_go_to_artist)) },
                        onClick = {
                            showMenu = false
                            if (artists.size > 1) {
                                showArtistSelectionDialog = true
                            } else if (artists.isNotEmpty()) {
                                onArtistClick(artists.first())
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
                if (currentTrack?.album != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_go_to_album)) },
                        onClick = {
                            showMenu = false
                            onAlbumClick(currentTrack.album)
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

        if (showFullscreenArt) {
            FullscreenAlbumArtDialog(
                uiState = uiState,
                onDismiss = { showFullscreenArt = false }
            )
        }

        // Диалог выбора исполнителя (если несколько)
        if (showArtistSelectionDialog && artists.size > 1) {
            ArtistSelectionDialog(
                artists = artists,
                onDismiss = { showArtistSelectionDialog = false },
                onArtistSelected = { artist ->
                    showArtistSelectionDialog = false
                    onArtistClick(artist)
                }
            )
        }

        // Диалог информации об аудио
        if (showAudioInfoDialog) {
            val audioFormat by viewModel.getAudioFormat(currentTrack?.id ?: 0L)
                .collectAsState(initial = null)

            ModernDialog(
                onDismissRequest = { showAudioInfoDialog = false },
                title = stringResource(R.string.audio_format_details),
                confirmText = stringResource(R.string.btn_ok),
                onConfirm = { showAudioInfoDialog = false }
            ) {
                DetailedFormatInfo(
                    format = audioFormat,
                    trackTitle = currentTrack?.title,
                    trackArtist = currentTrack?.artist,
                    trackAlbum = currentTrack?.album,
                    durationMs = uiState.durationMs,
                    onArtistClick = { artist ->
                        showAudioInfoDialog = false
                        onArtistClick(artist)
                    },
                    onAlbumClick = currentTrack?.album?.let { album ->
                        { _: String ->
                            showAudioInfoDialog = false
                            onAlbumClick(album)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ArtistSelectionDialog(
    artists: List<String>,
    onDismiss: () -> Unit,
    onArtistSelected: (String) -> Unit
) {
    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_select_artist),
        dismissText = stringResource(R.string.btn_cancel),
        onDismiss = onDismiss
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            artists.forEach { artist ->
                ModernSelectionItem(
                    title = artist,
                    selected = false,
                    onClick = { onArtistSelected(artist) },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpandedAlbumArt(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    onArtClick: () -> Unit
) {
    val context = LocalContext.current
    val extendedColors = AudioPlayerThemeExtended.colors

    if (uiState.tracks.isEmpty() || uiState.currentIndex < 0) return

    val pagerState = rememberPagerState(
        initialPage = uiState.currentIndex,
        pageCount = { uiState.tracks.size }
    )

    // Флаг для блокировки синхронизации во время пользовательского свайпа
    var blockSync by remember { mutableStateOf(false) }
    var lastSettledPage by remember { mutableIntStateOf(uiState.currentIndex) }

    // Синхронизируем pagerState с currentIndex (от кнопок Next/Previous)
    LaunchedEffect(uiState.currentIndex) {
        if (!blockSync && pagerState.settledPage != uiState.currentIndex) {
            pagerState.animateScrollToPage(uiState.currentIndex)
            lastSettledPage = uiState.currentIndex
        }
        blockSync = false
    }

    // Обрабатываем свайпы пользователя
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != lastSettledPage && pagerState.settledPage != uiState.currentIndex) {
            lastSettledPage = pagerState.settledPage
            blockSync = true
            val targetTrack = uiState.tracks.getOrNull(pagerState.settledPage)
            if (targetTrack != null) {
                viewModel.play(targetTrack)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            pageSpacing = 0.dp
        ) { page ->
            val track = uiState.tracks.getOrNull(page)
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        val scale = 1f - (kotlin.math.abs(pageOffset) * 0.2f).coerceIn(0f, 0.2f)
                        scaleX = scale
                        scaleY = scale
                        alpha = 1f - (kotlin.math.abs(pageOffset) * 0.6f).coerceIn(0f, 0.6f)
                    }
                    .clickable {
                        if (page == pagerState.currentPage) {
                            onArtClick()
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = extendedColors.accentSoft
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                border = BorderStroke(1.5.dp, extendedColors.cardBorder)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(track?.albumArtPath)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { AlbumArtPlaceholder() },
                    error = { AlbumArtPlaceholder() }
                )
            }
        }
    }
}

@Composable
private fun AlbumArtPlaceholder() {
    val extendedColors = AudioPlayerThemeExtended.colors

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
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
private fun FullscreenAlbumArtDialog(
    uiState: PlayerUiState,
    onDismiss: () -> Unit
) {
    val currentTrack = uiState.tracks.getOrNull(uiState.currentIndex)
    val context = LocalContext.current

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                            }
                        },
                        onTap = {
                            if (scale == 1f) onDismiss()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentTrack?.albumArtPath)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Album Art Fullscreen",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformableState),
                loading = { AlbumArtPlaceholder() },
                error = { AlbumArtPlaceholder() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedProgressSlider(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val progress by rememberUpdatedState(
        if (uiState.durationMs > 0) {
            (uiState.positionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val newProgress = (tapOffset.x / size.width).coerceIn(0f, 1f)
                        val newPosition = (newProgress * uiState.durationMs).toLong()
                        viewModel.seekTo(newPosition)
                    }
                }
        ) {
            Slider(
                value = progress * uiState.durationMs,
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..uiState.durationMs.toFloat(),
                modifier = Modifier.fillMaxWidth().height(32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(width = 19.dp, height = 24.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.5.dp, MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.positionMs.toTimeString(),
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.subtleText
            )
            Text(
                text = uiState.durationMs.toTimeString(),
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.subtleText
            )
        }
    }
}

@Composable
private fun ExpandedPlaybackControls(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val currentTrack = uiState.tracks.getOrNull(uiState.currentIndex)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button with modern card style
            val prevInteractionSource = remember { MutableInteractionSource() }
            val isPrevPressed by prevInteractionSource.collectIsPressedAsState()
            val prevScale by animateFloatAsState(
                targetValue = if (isPrevPressed) 0.9f else 1f,
                animationSpec = tween(100),
                label = "prevButtonScale"
            )

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(prevScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = prevInteractionSource,
                        indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                        onClick = { viewModel.previous() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(36.dp),
                    tint = extendedColors.iconTint
                )
            }

            // Play/Pause button (main)
            AnimatedPlayPauseButton(
                isPlaying = uiState.isPlaying,
                onToggle = { if (uiState.isPlaying) viewModel.pause() else viewModel.resume() },
                size = 72.dp,
                iconSize = 32.dp
            )

            // Next button with modern card style
            val nextInteractionSource = remember { MutableInteractionSource() }
            val isNextPressed by nextInteractionSource.collectIsPressedAsState()
            val nextScale by animateFloatAsState(
                targetValue = if (isNextPressed) 0.9f else 1f,
                animationSpec = tween(100),
                label = "nextButtonScale"
            )

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(nextScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = nextInteractionSource,
                        indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                        onClick = { viewModel.next() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(36.dp),
                    tint = extendedColors.iconTint
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShuffleModeButton(
                isEnabled = uiState.isShuffleModeEnabled,
                onToggle = { viewModel.toggleShuffleMode() },
                size = 48.dp,
                iconSize = 24.dp
            )

            if (currentTrack != null) {
                AnimatedFavoriteButton(
                    isFavorite = currentTrack.isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite(currentTrack) },
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            RepeatModeButton(
                repeatMode = uiState.repeatMode,
                onToggle = { viewModel.toggleRepeatMode() },
                size = 48.dp,
                iconSize = 24.dp
            )
        }
    }
}
