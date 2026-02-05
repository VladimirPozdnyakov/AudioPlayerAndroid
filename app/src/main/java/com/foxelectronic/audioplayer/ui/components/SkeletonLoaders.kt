package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

// ============================================
// SHIMMER EFFECTS
// ============================================

/**
 * Базовый shimmer эффект для skeleton-анимаций
 * Создаёт диагональную градиентную анимацию с плавным переходом
 */
@Composable
fun ShimmerBrush(
    baseColor: Color = AudioPlayerThemeExtended.colors.cardBackground,
    highlightColor: Color? = null
): Brush {
    val shimmerColors = listOf(
        baseColor.copy(alpha = 0.2f),
        baseColor.copy(alpha = 0.4f),
        highlightColor?.copy(alpha = 0.9f) ?: baseColor.copy(alpha = 0.7f), // Пик блеска
        baseColor.copy(alpha = 0.4f),
        baseColor.copy(alpha = 0.2f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_animation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation * 0.5f, translateAnimation * 0.5f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * Shimmer эффект с акцентным цветом (для кнопок и активных элементов)
 */
@Composable
fun AccentShimmerBrush(): Brush {
    val baseColor = AudioPlayerThemeExtended.colors.cardBackground
    val accent = MaterialTheme.colorScheme.primary

    val shimmerColors = listOf(
        baseColor.copy(alpha = 0.3f),
        baseColor.copy(alpha = 0.5f),
        accent.copy(alpha = 0.4f), // Акцентный блеск
        baseColor.copy(alpha = 0.5f),
        baseColor.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "accent_shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                delayMillis = 200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "accent_shimmer_animation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation * 0.5f, translateAnimation * 0.5f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * Пульсирующий shimmer эффект (для фона карточек)
 */
@Composable
fun PulseShimmerBrush(): Brush {
    val baseColor = AudioPlayerThemeExtended.colors.cardBackground

    val transition = rememberInfiniteTransition(label = "pulse_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_animation"
    )

    return Brush.horizontalGradient(
        colors = listOf(
            baseColor.copy(alpha = alpha),
            baseColor.copy(alpha = alpha * 0.8f),
            baseColor.copy(alpha = alpha)
        )
    )
}

// ============================================
// SKELETON COMPONENTS
// ============================================

/**
 * Базовый shimmer блок с закругленными углами
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = ShimmerBrush(),
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush)
    )
}

/**
 * Skeleton для круглого элемента (аватар, иконка)
 */
@Composable
fun CircleShimmer(
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    shimmerBrush: Brush = ShimmerBrush()
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush)
    )
}

/**
 * Skeleton для элемента трека
 * Повторяет структуру TrackItem с дополнительными деталями:
 * - Обложка альбома
 * - Кнопка play/pause в центре обложки
 * - Название трека
 * - Артист
 * - Бейдж аудио формата
 * - Кнопка избранного
 * - Кнопка меню
 */
@Composable
fun TrackItemSkeleton(
    modifier: Modifier = Modifier,
    showPlayButton: Boolean = false,
    delayMillis: Int = 0
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    val transition = rememberInfiniteTransition(label = "track_pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "track_pulse_animation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
            .alpha(pulseAlpha)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Skeleton для обложки с кнопкой play/pause
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Обложка
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerBrush)
            )

            // Кнопка play/pause (опционально)
            if (showPlayButton) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accentShimmer)
                        .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Play icon placeholder
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentShimmer)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Skeleton для текстовых блоков
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Название трека
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(16.dp),
                shimmerBrush = shimmerBrush
            )

            // Артист
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(14.dp),
                shimmerBrush = shimmerBrush
            )

            // Бейдж аудио формата
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp),
                    shimmerBrush = shimmerBrush
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(35.dp)
                        .height(14.dp),
                    shimmerBrush = accentShimmer
                )
            }
        }

        // Spacer для кнопок
        Spacer(modifier = Modifier.width(8.dp))

        // Кнопка избранного
        CircleShimmer(
            size = 32.dp,
            shimmerBrush = accentShimmer
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Кнопка меню (три точки)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            ShimmerBox(
                modifier = Modifier.size(6.dp, 6.dp),
                shimmerBrush = shimmerBrush,
                shape = CircleShape
            )
            ShimmerBox(
                modifier = Modifier.size(6.dp, 6.dp),
                shimmerBrush = shimmerBrush,
                shape = CircleShape
            )
            ShimmerBox(
                modifier = Modifier.size(6.dp, 6.dp),
                shimmerBrush = shimmerBrush,
                shape = CircleShape
            )
        }
    }
}

/**
 * Skeleton для карточки артиста/альбома/плейлиста
 * Повторяет структуру ArtistGroupItem/AlbumGroupItem с улучшенными деталями:
 * - Квадратная обложка
 * - Название
 * - Вторичный текст (артист/количество треков)
 * - Бейдж с количеством
 */
@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier,
    showCountBadge: Boolean = true,
    delayMillis: Int = 0
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    val transition = rememberInfiniteTransition(label = "card_pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse_animation"
    )

    Column(
        modifier = modifier
            .alpha(pulseAlpha)
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.cardBackground)
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .padding(dimens.cardPaddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skeleton для обложки с placeholder иконкой
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
                .background(shimmerBrush),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder иконка
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentShimmer)
            )
        }

        Spacer(Modifier.height(dimens.itemSpacing))

        // Skeleton для названия
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(16.dp),
            shimmerBrush = shimmerBrush
        )

        Spacer(Modifier.height(4.dp))

        // Skeleton для дополнительного текста
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(14.dp),
            shimmerBrush = shimmerBrush
        )

        // Бейдж с количеством (опционально)
        if (showCountBadge) {
            Spacer(Modifier.height(6.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(60.dp)
                    .height(14.dp),
                shimmerBrush = accentShimmer
            )
        }
    }
}

/**
 * Skeleton для мини-плеера (ExpandablePlayer в свёрнутом состоянии)
 */
@Composable
fun MiniPlayerSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Обложка
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(shimmerBrush)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Текстовые блоки
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp),
                shimmerBrush = shimmerBrush
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp),
                shimmerBrush = shimmerBrush
            )
        }

        // Прогресс бар
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentShimmer)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Кнопка play/pause
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentShimmer)
        )
    }
}

/**
 * Skeleton для прогресс-бара трека
 */
@Composable
fun ProgressBarSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(shimmerBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .fillMaxHeight()
                .background(accentShimmer)
        )
    }
}

// ============================================
// LIST & GRID SKELETONS
// ============================================

/**
 * Список skeleton-элементов треков с каскадной анимацией
 */
@Composable
fun TrackListSkeleton(
    itemCount: Int = 8,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(itemCount) { index ->
            // Добавляем задержку для каскадного эффекта
            val delayMillis = index * 100
            TrackItemSkeleton(
                showPlayButton = index == 0, // Первый элемент с кнопкой play
                delayMillis = delayMillis
            )
        }
    }
}

/**
 * Сетка skeleton-карточек с каскадной анимацией
 */
@Composable
fun CardGridSkeleton(
    itemCount: Int = 15,
    columns: Int = 3,
    showCountBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(itemCount) { index ->
            // Добавляем задержку для каскадного эффекта
            val delayMillis = (index / columns) * 150
            CardSkeleton(
                showCountBadge = showCountBadge,
                delayMillis = delayMillis
            )
        }
    }
}

/**
 * Skeleton для полной страницы с плеером и списком треков
 */
@Composable
fun FullPageSkeleton(
    showMiniPlayer: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Мини-плеер (опционально)
        if (showMiniPlayer) {
            MiniPlayerSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Список треков
        TrackListSkeleton(itemCount = 8)
    }
}

/**
 * Skeleton для строки поиска
 */
@Composable
fun SearchBarSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = ShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(AudioPlayerThemeExtended.colors.cardBackground)
            .border(1.dp, AudioPlayerThemeExtended.colors.cardBorder, RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка поиска
        CircleShimmer(size = 24.dp, shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.width(12.dp))

        // Placeholder текста
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(16.dp),
            shimmerBrush = shimmerBrush
        )
    }
}

/**
 * Skeleton для строки табов (ScrollableTabRow)
 */
@Composable
fun TabRowSkeleton(
    tabCount: Int = 5,
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Генерируем skeleton для каждого таба с разной шириной
        val tabWidths = listOf(0.65f, 0.75f, 0.6f, 0.7f, 0.8f)
        repeat(tabCount.coerceAtMost(tabWidths.size)) { index ->
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                shimmerBrush = if (index == 0) accentShimmer else shimmerBrush,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/**
 * Skeleton для полноэкранного плеера (ExpandablePlayer в развёрнутом состоянии)
 */
@Composable
fun ExpandedPlayerSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.cardBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Обложка альбома (большая)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(shimmerBrush),
            contentAlignment = Alignment.Center
        ) {
            // Иконка музыки
            CircleShimmer(
                size = 80.dp,
                shimmerBrush = accentShimmer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Название трека
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(24.dp),
            shimmerBrush = shimmerBrush
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Артист
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(18.dp),
            shimmerBrush = shimmerBrush
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Альбом
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(16.dp),
            shimmerBrush = shimmerBrush
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Прогресс бар
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(shimmerBrush)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .fillMaxHeight()
                        .background(accentShimmer)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Время
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp),
                    shimmerBrush = shimmerBrush
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp),
                    shimmerBrush = shimmerBrush
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопки управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            CircleShimmer(size = 40.dp, shimmerBrush = shimmerBrush)

            // Previous
            CircleShimmer(size = 48.dp, shimmerBrush = shimmerBrush)

            // Play/Pause (большая кнопка)
            CircleShimmer(size = 72.dp, shimmerBrush = accentShimmer)

            // Next
            CircleShimmer(size = 48.dp, shimmerBrush = shimmerBrush)

            // Repeat
            CircleShimmer(size = 40.dp, shimmerBrush = shimmerBrush)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Нижняя панель с кнопками
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleShimmer(size = 36.dp, shimmerBrush = shimmerBrush)
            CircleShimmer(size = 36.dp, shimmerBrush = shimmerBrush)
            CircleShimmer(size = 36.dp, shimmerBrush = shimmerBrush)
            CircleShimmer(size = 36.dp, shimmerBrush = shimmerBrush)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Skeleton для заголовка с кнопкой назад (детальный вид)
 */
@Composable
fun DetailHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = ShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка назад
        CircleShimmer(size = 24.dp, shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.width(8.dp))

        // Заголовок
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(24.dp),
            shimmerBrush = shimmerBrush
        )
    }
}

/**
 * Skeleton для информационного бейджа (формат аудио, битрейт и т.д.)
 */
@Composable
fun InfoBadgeSkeleton(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 60.dp
) {
    val accentShimmer = AccentShimmerBrush()

    ShimmerBox(
        modifier = modifier
            .width(width)
            .height(20.dp),
        shimmerBrush = accentShimmer,
        shape = RoundedCornerShape(10.dp)
    )
}

/**
 * Skeleton для группы бейджей
 */
@Composable
fun BadgeGroupSkeleton(
    badgeCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val widths = listOf(45.dp, 55.dp, 40.dp, 50.dp)
        repeat(badgeCount.coerceAtMost(widths.size)) { index ->
            InfoBadgeSkeleton(width = widths[index])
        }
    }
}

/**
 * Skeleton для экрана настроек
 */
@Composable
fun SettingsSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок секции
        item {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(14.dp),
                shimmerBrush = shimmerBrush
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Элементы настроек
        items(4) {
            SettingsItemSkeleton()
        }

        // Ещё один заголовок секции
        item {
            Spacer(modifier = Modifier.height(16.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(14.dp),
                shimmerBrush = shimmerBrush
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Ещё элементы настроек
        items(3) {
            SettingsItemSkeleton()
        }
    }
}

/**
 * Skeleton для элемента настройки
 */
@Composable
fun SettingsItemSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val shimmerBrush = ShimmerBrush()
    val accentShimmer = AccentShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка
        CircleShimmer(size = 24.dp, shimmerBrush = shimmerBrush)

        Spacer(modifier = Modifier.width(16.dp))

        // Текстовый блок
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp),
                shimmerBrush = shimmerBrush
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp),
                shimmerBrush = shimmerBrush
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Переключатель или стрелка
        ShimmerBox(
            modifier = Modifier
                .width(48.dp)
                .height(24.dp),
            shimmerBrush = accentShimmer,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
