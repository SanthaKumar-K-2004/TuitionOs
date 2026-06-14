package com.example.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Reusable press animation modifier.
 * Scales the element down to [scale] when pressed, matching the design system's active:scale-95.
 */
fun Modifier.pressAnimation(
    interactionSource: MutableInteractionSource,
    scale: Float = 0.95f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) scale else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "pressScale"
    )
    this.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}

/**
 * Convenience press animation modifier with built-in click handler.
 * Combines scale animation + clickable in one call.
 */
fun Modifier.pressAnimation(
    scale: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) scale else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "pressScale"
    )
    this.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}
