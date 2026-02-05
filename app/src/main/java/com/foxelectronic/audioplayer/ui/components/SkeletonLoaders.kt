package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer
import kotlin.random.Random

// ============================================
// ACCESSIBILITY & CONFIGURATION
// ============================================

/**
 * Проверка системной настройки reduced motion
 * Возвращает true если пользователь включил уменьшение движения
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val accessibilityManager = LocalAccessibilityManager.current
    return remember(accessibilityManager) {
        // Android не предоставляет прямой API для reduced motion,
        // но мы можем использовать общую настройку анимаций
        false // По умолчанию анимации включены
    }
}

/**
 * Контроллер для синхронизации shimmer-анимаций между элементами
 */
class ShimmerController {
    private val _progress = mutableFloatStateOf(0f)
    val progress: State<Float> = _progress

    fun updateProgress(value: Float) {
        _progress.floatValue = value
    }
}

val LocalShimmerController = compositionLocalOf { ShimmerController() }

/**
 * Провайдер для синхронизированных shimmer-анимаций
 */
@Composable
fun SynchronizedShimmerProvider(
    content: @Composable () -> Unit
) {
    val controller = remember { ShimmerController() }
    val transition = rememberInfiniteTransition(label = "sync_shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_progress"
    )

    controller.updateProgress(progress)

    CompositionLocalProvider(LocalShimmerController provides controller) {
        content()
    }
}

// ============================================
// SHIMMER EFFECTS
// ============================================

/**
 * Адаптивные цвета shimmer для светлой и тёмной темы
 */
@Composable
fun rememberShimmerColors(): List<Color> {
    val isDark = isSystemInDarkTheme()

    return remember(isDark) {
        if (isDark) {
            listOf(
                Color(0xFF1E1E1E),
                Color(0xFF2A2A2A),
                Color(0xFF3A3A3A),  // Яркий блик для тёмной темы
                Color(0xFF2A2A2A),
                Color(0xFF1E1E1E)
            )
        } else {
            listOf(
                Color(0xFFE8E8E8),
                Color(0xFFEEEEEE),
                Color(0xFFF8F8F8),  // Мягкий блик для светлой темы
                Color(0xFFEEEEEE),
                Color(0xFFE8E8E8)
            )
        }
    }
}

/**
 * Wave shimmer эффект - горизонтальная волна (основной стиль)
 * Современный стиль, используемый в iOS и многих приложениях
 */
@Composable
fun WaveShimmerBrush(
    widthPx: Float = 1000f,
    respectReducedMotion: Boolean = true
): Brush {
    val reduceMotion = if (respectReducedMotion) rememberReducedMotion() else false
    val shimmerColors = rememberShimmerColors()

    if (reduceMotion) {
        // Статичный градиент без анимации
        return Brush.horizontalGradient(
            colors = listOf(shimmerColors[0], shimmerColors[2], shimmerColors[0])
        )
    }

    val transition = rememberInfiniteTransition(label = "wave_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -widthPx,
        targetValue = widthPx * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_x"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX, 0f),
        end = Offset(translateX + widthPx * 0.5f, 0f)
    )
}

/**
 * Диагональный shimmer эффект (альтернативный стиль)
 * Классический Android стиль
 */
@Composable
fun DiagonalShimmerBrush(
    respectReducedMotion: Boolean = true
): Brush {
    val reduceMotion = if (respectReducedMotion) rememberReducedMotion() else false
    val shimmerColors = rememberShimmerColors()

    if (reduceMotion) {
        return Brush.linearGradient(
            colors = listOf(shimmerColors[0], shimmerColors[2], shimmerColors[0])
        )
    }

    val transition = rememberInfiniteTransition(label = "diagonal_shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diagonal_animation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation * 0.5f, translateAnimation * 0.5f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * Акцентный shimmer эффект с primary цветом
 */
@Composable
fun AccentShimmerBrush(): Brush {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary

    val shimmerColors = remember(isDark, accent) {
        if (isDark) {
            listOf(
                Color(0xFF2A2A2A),
                Color(0xFF3A3A3A),
                accent.copy(alpha = 0.5f),
                Color(0xFF3A3A3A),
                Color(0xFF2A2A2A)
            )
        } else {
            listOf(
                Color(0xFFE8E8E8),
                Color(0xFFEEEEEE),
                accent.copy(alpha = 0.3f),
                Color(0xFFEEEEEE),
                Color(0xFFE8E8E8)
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "accent_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "accent_x"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX, 0f),
        end = Offset(translateX + 500f, 0f)
    )
}

/**
 * Пульсирующий эффект для фона карточек
 */
@Composable
fun PulseShimmerBrush(): Brush {
    val shimmerColors = rememberShimmerColors()

    val transition = rememberInfiniteTransition(label = "pulse_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    return Brush.horizontalGradient(
        colors = listOf(
            shimmerColors[0].copy(alpha = alpha),
            shimmerColors[2].copy(alpha = alpha * 0.8f),
            shimmerColors[0].copy(alpha = alpha)
        )
    )
}

// ============================================
// CONTENT TRANSITION
// ============================================

/**
 * Компонент для плавного перехода от skeleton к реальному контенту
 */
@Composable
fun <T> SkeletonContent(
    data: T?,
    modifier: Modifier = Modifier,
    skeleton: @Composable () -> Unit,
    content: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = data,
        animationSpec = tween(300),
        modifier = modifier,
        label = "skeleton_transition"
    ) { state ->
        if (state == null) {
            skeleton()
        } else {
            content(state)
        }
    }
}

/**
 * Компонент для плавного перехода с поддержкой списков
 */
@Composable
fun <T> SkeletonListContent(
    items: List<T>?,
    isLoading: Boolean,
    skeletonCount: Int = 8,
    modifier: Modifier = Modifier,
    skeletonItem: @Composable (index: Int) -> Unit,
    content: @Composable (List<T>) -> Unit
) {
    Crossfade(
        targetState = isLoading,
        animationSpec = tween(300),
        modifier = modifier,
        label = "skeleton_list_transition"
    ) { loading ->
        if (loading || items == null) {
            Column {
                repeat(skeletonCount) { index ->
                    skeletonItem(index)
                }
            }
        } else {
            content(items)
        }
    }
}

// ============================================
// BASE SKELETON COMPONENTS (Optimized)
// ============================================

/**
 * Базовый shimmer блок с оптимизированной отрисовкой
 * Использует drawBehind для уменьшения recomposition
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    useAccompanist: Boolean = true
) {
    if (useAccompanist) {
        Box(
            modifier = modifier
                .clip(shape)
                .placeholder(
                    visible = true,
                    shape = shape,
                    highlight = PlaceholderHighlight.shimmer()
                )
        )
    } else {
        val shimmerBrush = WaveShimmerBrush()
        Box(
            modifier = modifier
                .clip(shape)
                .drawBehind {
                    drawRect(shimmerBrush)
                }
        )
    }
}

/**
 * Shimmer блок с кастомным brush
 */
@Composable
fun ShimmerBoxWithBrush(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                drawRect(shimmerBrush)
            }
    )
}

/**
 * Skeleton для круглого элемента (аватар, иконка)
 */
@Composable
fun CircleShimmer(
    size: Dp,
    modifier: Modifier = Modifier,
    useAccompanist: Boolean = true
) {
    if (useAccompanist) {
        Box(
            modifier = modifier
                .size(size)
                .placeholder(
                    visible = true,
                    shape = CircleShape,
                    highlight = PlaceholderHighlight.shimmer()
                )
        )
    } else {
        val shimmerBrush = WaveShimmerBrush()
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(shimmerBrush)
                }
        )
    }
}

/**
 * Текстовая линия skeleton с рандомизированной шириной
 */
@Composable
fun TextLineSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    minWidthFraction: Float = 0.4f,
    maxWidthFraction: Float = 0.9f,
    seed: Int = 0
) {
    val width = remember(seed, minWidthFraction, maxWidthFraction) {
        minWidthFraction + (maxWidthFraction - minWidthFraction) *
            Random(seed).nextFloat()
    }

    ShimmerBox(
        modifier = modifier
            .fillMaxWidth(width)
            .height(height)
    )
}

// ============================================
// SKELETON COMPONENTS
// ============================================

/**
 * Skeleton для элемента трека с улучшенными анимациями
 */
@Composable
fun TrackItemSkeleton(
    modifier: Modifier = Modifier,
    showPlayButton: Boolean = false,
    delayMillis: Int = 0,
    index: Int = 0
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    val transition = rememberInfiniteTransition(label = "track_pulse_$index")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "track_pulse_animation_$index"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(extendedColors.cardBackground.copy(alpha = pulseAlpha))
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Обложка с опциональной кнопкой play
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            ShimmerBox(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp)
            )

            if (showPlayButton) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .placeholder(
                            visible = true,
                            shape = RoundedCornerShape(9.dp),
                            highlight = PlaceholderHighlight.shimmer()
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Текстовые блоки с рандомизированной шириной
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Название трека
            TextLineSkeleton(
                height = 16.dp,
                minWidthFraction = 0.6f,
                maxWidthFraction = 0.85f,
                seed = index * 3
            )

            // Артист
            TextLineSkeleton(
                height = 14.dp,
                minWidthFraction = 0.4f,
                maxWidthFraction = 0.65f,
                seed = index * 3 + 1
            )

            // Бейджи
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(35.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Кнопка избранного
        CircleShimmer(size = 32.dp)

        Spacer(modifier = Modifier.width(8.dp))

        // Меню (три точки)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            repeat(3) {
                CircleShimmer(size = 6.dp)
            }
        }
    }
}

/**
 * Skeleton для карточки артиста/альбома/плейлиста
 */
@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier,
    showCountBadge: Boolean = true,
    delayMillis: Int = 0,
    index: Int = 0
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens

    val transition = rememberInfiniteTransition(label = "card_pulse_$index")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse_animation_$index"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
            .background(extendedColors.cardBackground.copy(alpha = pulseAlpha))
            .border(
                width = 1.dp,
                color = extendedColors.cardBorder,
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )
            .padding(dimens.cardPaddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Обложка с placeholder иконкой
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            ShimmerBox(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(dimens.cardCornerRadiusSmall)
            )

            // Placeholder иконка
            CircleShimmer(size = 40.dp)
        }

        Spacer(Modifier.height(dimens.itemSpacing))

        // Название с рандомизированной шириной
        TextLineSkeleton(
            height = 16.dp,
            minWidthFraction = 0.7f,
            maxWidthFraction = 0.95f,
            seed = index * 2
        )

        Spacer(Modifier.height(4.dp))

        // Дополнительный текст
        TextLineSkeleton(
            height = 14.dp,
            minWidthFraction = 0.5f,
            maxWidthFraction = 0.75f,
            seed = index * 2 + 1
        )

        if (showCountBadge) {
            Spacer(Modifier.height(6.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(60.dp)
                    .height(14.dp),
                shape = RoundedCornerShape(7.dp)
            )
        }
    }
}

/**
 * Skeleton для мини-плеера
 */
@Composable
fun MiniPlayerSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors

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
        ShimmerBox(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Текстовые блоки
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextLineSkeleton(
                height = 14.dp,
                minWidthFraction = 0.6f,
                maxWidthFraction = 0.8f,
                seed = 100
            )
            TextLineSkeleton(
                height = 12.dp,
                minWidthFraction = 0.4f,
                maxWidthFraction = 0.6f,
                seed = 101
            )
        }

        // Прогресс бар
        ShimmerBox(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp),
            shape = RoundedCornerShape(2.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Кнопка play/pause
        CircleShimmer(size = 40.dp)
    }
}

/**
 * Skeleton для пустого состояния
 */
@Composable
fun EmptyStateSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Большая иконка
        Box(
            modifier = Modifier
                .size(80.dp)
                .placeholder(
                    visible = true,
                    shape = CircleShape,
                    highlight = PlaceholderHighlight.fade()
                )
        )

        Spacer(Modifier.height(24.dp))

        // Заголовок
        ShimmerBox(
            modifier = Modifier
                .width(180.dp)
                .height(20.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Подзаголовок
        ShimmerBox(
            modifier = Modifier
                .width(240.dp)
                .height(16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Кнопка действия
        ShimmerBox(
            modifier = Modifier
                .width(140.dp)
                .height(44.dp),
            shape = RoundedCornerShape(22.dp)
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
            // Каскадная задержка
            val delayMillis = index * 80
            TrackItemSkeleton(
                showPlayButton = index == 0,
                delayMillis = delayMillis,
                index = index
            )
        }
    }
}

/**
 * Сетка skeleton-карточек с диагональной staggered анимацией
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
            val row = index / columns
            val col = index % columns

            // Диагональная staggered задержка
            val delayMillis = (row + col) * 60

            CardSkeleton(
                showCountBadge = showCountBadge,
                delayMillis = delayMillis,
                index = index
            )
        }
    }
}

/**
 * Skeleton для полной страницы
 */
@Composable
fun FullPageSkeleton(
    showMiniPlayer: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (showMiniPlayer) {
            MiniPlayerSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

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
    val extendedColors = AudioPlayerThemeExtended.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleShimmer(size = 24.dp)

        Spacer(modifier = Modifier.width(12.dp))

        ShimmerBox(
            modifier = Modifier
                .width(120.dp)
                .height(16.dp)
        )
    }
}

/**
 * Skeleton для строки табов
 */
@Composable
fun TabRowSkeleton(
    tabCount: Int = 5,
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors

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
        repeat(tabCount.coerceAtMost(5)) { index ->
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/**
 * Skeleton для полноэкранного плеера
 */
@Composable
fun ExpandedPlayerSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.cardBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Обложка альбома
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            ShimmerBox(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp)
            )

            CircleShimmer(size = 80.dp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Название
        TextLineSkeleton(
            height = 24.dp,
            minWidthFraction = 0.6f,
            maxWidthFraction = 0.85f,
            seed = 200
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Артист
        TextLineSkeleton(
            height = 18.dp,
            minWidthFraction = 0.4f,
            maxWidthFraction = 0.6f,
            seed = 201
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
            ) {
                ShimmerBox(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(3.dp)
                )

                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp)
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
            CircleShimmer(size = 40.dp)
            CircleShimmer(size = 48.dp)
            CircleShimmer(size = 72.dp)
            CircleShimmer(size = 48.dp)
            CircleShimmer(size = 40.dp)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Нижняя панель
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) {
                CircleShimmer(size = 36.dp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Skeleton для заголовка с кнопкой назад
 */
@Composable
fun DetailHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleShimmer(size = 24.dp)

        Spacer(modifier = Modifier.width(8.dp))

        TextLineSkeleton(
            height = 24.dp,
            minWidthFraction = 0.5f,
            maxWidthFraction = 0.7f,
            seed = 300
        )
    }
}

/**
 * Skeleton для информационного бейджа
 */
@Composable
fun InfoBadgeSkeleton(
    modifier: Modifier = Modifier,
    width: Dp = 60.dp
) {
    ShimmerBox(
        modifier = modifier
            .width(width)
            .height(20.dp),
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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок секции
        item {
            TextLineSkeleton(
                height = 14.dp,
                minWidthFraction = 0.2f,
                maxWidthFraction = 0.35f,
                seed = 400
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Элементы настроек
        items(4) { index ->
            SettingsItemSkeleton(index = index)
        }

        // Ещё заголовок
        item {
            Spacer(modifier = Modifier.height(16.dp))
            TextLineSkeleton(
                height = 14.dp,
                minWidthFraction = 0.15f,
                maxWidthFraction = 0.3f,
                seed = 410
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Ещё элементы
        items(3) { index ->
            SettingsItemSkeleton(index = index + 4)
        }
    }
}

/**
 * Skeleton для элемента настройки
 */
@Composable
fun SettingsItemSkeleton(
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    val extendedColors = AudioPlayerThemeExtended.colors

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
        CircleShimmer(size = 24.dp)

        Spacer(modifier = Modifier.width(16.dp))

        // Текстовый блок
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextLineSkeleton(
                height = 16.dp,
                minWidthFraction = 0.4f,
                maxWidthFraction = 0.7f,
                seed = 500 + index * 2
            )
            TextLineSkeleton(
                height = 14.dp,
                minWidthFraction = 0.6f,
                maxWidthFraction = 0.9f,
                seed = 500 + index * 2 + 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Переключатель
        ShimmerBox(
            modifier = Modifier
                .width(48.dp)
                .height(24.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Skeleton для прогресс-бара
 */
@Composable
fun ProgressBarSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(2.dp)
        )

        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(2.dp)
        )
    }
}
