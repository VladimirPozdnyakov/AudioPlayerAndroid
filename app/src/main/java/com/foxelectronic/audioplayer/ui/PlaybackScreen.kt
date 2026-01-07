package com.foxelectronic.audioplayer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.foxelectronic.audioplayer.PlayerViewModel
import com.foxelectronic.audioplayer.PlayerUiState
import com.foxelectronic.audioplayer.PlaylistType
import com.foxelectronic.audioplayer.ui.components.AnimatedFavoriteButton
import com.foxelectronic.audioplayer.ui.components.AnimatedPlayPauseButton
import com.foxelectronic.audioplayer.ui.components.RepeatModeButton
import com.foxelectronic.audioplayer.ui.components.ShuffleModeButton
import com.foxelectronic.audioplayer.ui.components.toTimeString

@Composable
@Suppress("UNUSED_PARAMETER")
fun PlaybackScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
    onEditInfoClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFullscreenArt by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Основной контент с альбомом и информацией о треке по центру
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 110.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Album Art
            AlbumArt(
                uiState = uiState,
                onArtClick = { showFullscreenArt = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Track Information
            TrackInfo(uiState = uiState)
        }

        // Прогресс бар и элементы управления чуть выше нижнего края
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 30.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Slider with Time Labels
            ProgressSlider(uiState = uiState, viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls
            PlaybackControls(uiState = uiState, viewModel = viewModel)
        }

        // Кнопка меню в правом верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 8.dp)
        ) {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Меню",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Добавить в плейлист") },
                    onClick = {
                        showMenu = false
                        onAddToPlaylistClick()
                    },
                    leadingIcon = {
                        @Suppress("DEPRECATION")
                        Icon(
                            imageVector = Icons.Rounded.PlaylistAdd,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Изменить информацию") },
                    onClick = {
                        showMenu = false
                        onEditInfoClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }

    // Полноэкранный просмотр обложки
    if (showFullscreenArt) {
        FullscreenAlbumArt(
            uiState = uiState,
            onDismiss = { showFullscreenArt = false }
        )
    }
}

@Composable
private fun AlbumArt(
    uiState: PlayerUiState,
    onArtClick: () -> Unit = {}
) {
    val currentTrack = remember(uiState.currentIndex, uiState.tracks) {
        if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
            uiState.tracks[uiState.currentIndex]
        } else null
    }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clickable { onArtClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentTrack?.albumArtPath)
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

@Composable
private fun AlbumArtPlaceholder() {
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
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
private fun FullscreenAlbumArt(
    uiState: PlayerUiState,
    onDismiss: () -> Unit
) {
    val currentTrack = remember(uiState.currentIndex, uiState.tracks) {
        if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
            uiState.tracks[uiState.currentIndex]
        } else null
    }
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
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
                            if (scale == 1f) {
                                onDismiss()
                            }
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

@Composable
private fun TrackInfo(uiState: PlayerUiState) {
    val currentTrack = remember(uiState.currentIndex, uiState.tracks) {
        if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
            uiState.tracks[uiState.currentIndex]
        } else null
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Показываем тип плейлиста (Исполнитель/Альбом/Плейлист) если это не "Все" и не "Любимые"
        if (uiState.playlistType == PlaylistType.ARTIST ||
            uiState.playlistType == PlaylistType.ALBUM ||
            uiState.playlistType == PlaylistType.CUSTOM_PLAYLIST) {
            val prefix = when (uiState.playlistType) {
                PlaylistType.ARTIST -> "Исполнитель"
                PlaylistType.ALBUM -> "Альбом"
                PlaylistType.CUSTOM_PLAYLIST -> "Плейлист"
                else -> ""
            }
            Text(
                text = prefix,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = uiState.playlistName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

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
            text = currentTrack?.artist ?: "Неизвестный исполнитель",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressSlider(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel
) {
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
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        val newPosition = (newProgress * uiState.durationMs).toLong()
                        viewModel.seekTo(newPosition)
                    }
                }
        ) {
            Slider(
                value = progress * uiState.durationMs,
                onValueChange = { newPosition ->
                    viewModel.seekTo(newPosition.toLong())
                },
                valueRange = 0f..uiState.durationMs.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(width = 19.dp, height = 24.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 2.5.dp,
                                color = MaterialTheme.colorScheme.background
                            ),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = uiState.durationMs.toTimeString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main playback controls row (Previous, Play/Pause, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            IconButton(
                onClick = { viewModel.previous() },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Play/Pause Button (main)
            AnimatedPlayPauseButton(
                isPlaying = uiState.isPlaying,
                onToggle = { if (uiState.isPlaying) viewModel.pause() else viewModel.resume() },
                size = 72.dp,
                iconSize = 32.dp
            )

            // Next Button
            IconButton(
                onClick = { viewModel.next() },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Secondary controls row (Shuffle, Favorite, Repeat)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            ShuffleModeButton(
                isEnabled = uiState.isShuffleModeEnabled,
                onToggle = { viewModel.toggleShuffleMode() },
                size = 48.dp,
                iconSize = 24.dp
            )

            // Favorite button
            val currentTrack = remember(uiState.currentIndex, uiState.tracks) {
                if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
                    uiState.tracks[uiState.currentIndex]
                } else null
            }

            if (currentTrack != null) {
                AnimatedFavoriteButton(
                    isFavorite = currentTrack.isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite(currentTrack) },
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Repeat Button
            RepeatModeButton(
                repeatMode = uiState.repeatMode,
                onToggle = { viewModel.toggleRepeatMode() },
                size = 48.dp,
                iconSize = 24.dp
            )
        }
    }
}
