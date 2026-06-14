package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens from the Vibrant Professional design system.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),    // Buttons, inputs
    large = RoundedCornerShape(16.dp),     // Standard cards
    extraLarge = RoundedCornerShape(24.dp) // Large cards, containers
)

/** Additional shape constants not covered by Material3 Shapes */
object AppShapeExtra {
    val Card = RoundedCornerShape(24.dp)
    val CardCompact = RoundedCornerShape(16.dp)
    val Button = RoundedCornerShape(12.dp)
    val Input = RoundedCornerShape(12.dp)
    val Pill = RoundedCornerShape(9999.dp)   // Status badges
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val FAB = RoundedCornerShape(16.dp)
}
