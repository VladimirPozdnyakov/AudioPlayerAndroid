package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    favoriteColor: Color = MaterialTheme.colorScheme.primary,
    unfavoriteColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var scale by remember { mutableStateOf(1f) }
    val targetScale = 1f

    IconButton(
        onClick = {
            onToggleFavorite()
            scale = 1.2f
        },
        modifier = modifier
    ) {
        val currentScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "favoriteScaleAnimation"
        )

        val contentDesc = if (isFavorite)
            stringResource(R.string.menu_remove_from_favorites)
        else
            stringResource(R.string.menu_add_to_favorites)
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = contentDesc,
            tint = if (isFavorite) favoriteColor else unfavoriteColor,
            modifier = Modifier.size(iconSize).scale(currentScale)
        )
    }

    LaunchedEffect(scale) {
        if (scale != targetScale) {
            delay(200)
            scale = targetScale
        }
    }
}
