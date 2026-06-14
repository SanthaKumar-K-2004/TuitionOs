package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StatusInactive
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow

/**
 * Shimmer skeleton loading placeholders for premium loading states.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Int = 80,
    cornerRadius: Int = 16
) {
    val shimmerColors = listOf(
        SurfaceContainerLow,
        SurfaceContainerHighest.copy(alpha = 0.5f),
        SurfaceContainerLow
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(brush)
    )
}

@Composable
fun SkeletonList(
    count: Int = 3,
    modifier: Modifier = Modifier,
    itemHeight: Int = 120,
    spacing: Int = 12
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.dp)
    ) {
        repeat(count) {
            SkeletonCard(height = itemHeight, cornerRadius = 20)
        }
    }
}

@Composable
fun SkeletonCircle(
    size: Int = 56,
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        SurfaceContainerLow,
        SurfaceContainerHighest.copy(alpha = 0.5f),
        SurfaceContainerLow
    )

    val transition = rememberInfiniteTransition(label = "shimmerCircle")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerCircleTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .background(brush, androidx.compose.foundation.shape.CircleShape)
    )
}
