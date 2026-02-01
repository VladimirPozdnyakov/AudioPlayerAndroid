package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Базовый shimmer эффект для skeleton-анимаций
 * Создаёт диагональную градиентную анимацию, имитирующую движение света
 */
@Composable
fun ShimmerBrush(): Brush {
    // Цвета для shimmer эффекта - используем базовый цвет фона с varying alpha
    val baseColor = AudioPlayerThemeExtended.colors.cardBackground
    val shimmerColors = listOf(
        baseColor.copy(alpha = 0.3f),
        baseColor.copy(alpha = 0.5f),
        baseColor.copy(alpha = 0.8f), // Пик блеска
        baseColor.copy(alpha = 0.5f),
        baseColor.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_animation"
    )

    // Диагональный градиент для более естественного эффекта
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation - 1000f, y = translateAnimation - 1000f),
        end = Offset(x = translateAnimation, y = translateAnimation)
    )
}

/**
 * Skeleton для элемента трека
 * Повторяет структуру TrackItem: обложка 56dp + текстовые блоки
 */
@Composable
fun TrackItemSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val shimmerBrush = ShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(extendedColors.cardBackground)
            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Skeleton для обложки
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerBrush)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Skeleton для текстовых блоков
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Название трека
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            // Артист
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            // Бейдж формата
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

/**
 * Skeleton для карточки артиста/альбома/плейлиста
 * Повторяет структуру ArtistGroupItem/AlbumGroupItem
 */
@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier
) {
    val extendedColors = AudioPlayerThemeExtended.colors
    val dimens = AudioPlayerThemeExtended.dimens
    val shimmerBrush = ShimmerBrush()

    Column(
        modifier = modifier
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
        // Skeleton для обложки (квадратная)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(dimens.cardCornerRadiusSmall))
                .background(shimmerBrush)
        )

        Spacer(Modifier.height(dimens.itemSpacing))

        // Skeleton для названия
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )

        Spacer(Modifier.height(4.dp))

        // Skeleton для дополнительного текста
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
    }
}

/**
 * Список skeleton-элементов треков
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
        items(itemCount) {
            TrackItemSkeleton()
        }
    }
}

/**
 * Сетка skeleton-карточек
 */
@Composable
fun CardGridSkeleton(
    itemCount: Int = 15,
    columns: Int = 3,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(itemCount) {
            CardSkeleton()
        }
    }
}

/**
 * Shimmer-эффект для произвольного блока
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ShimmerBrush())
    )
}
