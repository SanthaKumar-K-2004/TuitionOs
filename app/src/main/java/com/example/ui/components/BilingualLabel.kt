package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnSurfaceColor
import com.example.ui.theme.OnSurfaceVariantColor

/**
 * Bilingual label component showing English (uppercase caps) + Tamil subtitle.
 * Matches the design system's label-caps + tamil-label pattern.
 */
@Composable
fun BilingualLabel(
    english: String,
    tamil: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = english.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceColor,
            letterSpacing = 0.6.sp
        )
        Text(
            text = tamil,
            fontSize = 11.sp,
            color = OnSurfaceVariantColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}
