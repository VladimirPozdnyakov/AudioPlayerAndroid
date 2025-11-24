package com.foxelectronic.audioplayer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import androidx.compose.ui.unit.Dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.foxelectronic.audioplayer.PlayerViewModel
import com.foxelectronic.audioplayer.PlayerUiState
import com.foxelectronic.audioplayer.Track

@Composable
fun PlaybackScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {} // Функция, вызываемая при нажатии на кнопку "назад"
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Кнопка «назад» в верхнем левом углу
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 16.dp, top = 48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

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
            AlbumArt(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Track Information
            TrackInfo(uiState = uiState, viewModel = viewModel)
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
    }
}

@Composable
fun AlbumArt(uiState: PlayerUiState) {
    val currentTrack = if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
        uiState.tracks[uiState.currentIndex]
    } else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Album art with fallback to placeholder
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (currentTrack?.albumArtPath != null && currentTrack.albumArtPath.isNotBlank()) {
                // Display actual album art if available
                Image(
                    painter = rememberAsyncImagePainter(
                        model = currentTrack.albumArtPath,
                        placeholder = null, // No placeholder needed, as we show icon underneath
                        error = null // No error fallback needed, as we show icon underneath
                    ),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Display placeholder if no album art
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Album Art",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TrackInfo(uiState: PlayerUiState, viewModel: PlayerViewModel) {
    val currentTrack = if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
        uiState.tracks[uiState.currentIndex]
    } else null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Track Title
        Text(
            text = currentTrack?.title ?: "No Track Playing",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center, // Центрируем текст
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Track Artist
        Text(
            text = currentTrack?.artist ?: "Unknown Artist",
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
fun ProgressSlider(
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
        // Progress Slider
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
                            .clip(RoundedCornerShape(12.dp)) // Увеличиваем скругление для большего эффекта
                            .border(
                                width = 2.5.dp,
                                color = MaterialTheme.colorScheme.background
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 20.dp) // Увеличиваем ширину внутреннего элемента
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(uiState.positionMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatTime(uiState.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlaybackControls(
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
            Box(
                modifier = Modifier
                    .size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                val cornerRadius by animateFloatAsState(
                    targetValue = if (uiState.isPlaying) 18f else 36f, // 36f approximates a circle (72dp/2)
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    ),
                    label = "cornerRadius"
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(cornerRadius.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val scale by animateFloatAsState(
                        targetValue = if (uiState.isPlaying) 1.2f else 1f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        label = "scale"
                    )

                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        modifier = Modifier
                            .size(32.dp)
                            .scale(scale),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

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
            IconButton(
                onClick = { viewModel.toggleShuffleMode() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(24.dp),
                    tint = if (uiState.isShuffleModeEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Favorite button
            val currentTrack = if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
                uiState.tracks[uiState.currentIndex]
            } else null

            if (currentTrack != null) {
                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(currentTrack)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (currentTrack.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (currentTrack.isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (currentTrack.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // Empty space to maintain alignment when no track is playing
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Repeat Button
            IconButton(
                onClick = { viewModel.toggleRepeatMode() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when (uiState.repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_OFF -> Icons.Rounded.Repeat
                        androidx.media3.common.Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
                        else -> Icons.Rounded.RepeatOne
                    },
                    contentDescription = when (uiState.repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_OFF -> "Repeat off"
                        androidx.media3.common.Player.REPEAT_MODE_ALL -> "Repeat all"
                        else -> "Repeat one"
                    },
                    modifier = Modifier.size(24.dp),
                    tint = when (uiState.repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_OFF -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

@Composable
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
