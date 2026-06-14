package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Reusable status pill badge matching the premium design system.
 * Paid=green, Pending=amber, Overdue=red, Lead statuses (NEW=blue, CONTACTED=amber, ADMITTED=green)
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    showTamil: Boolean = true
) {
    val bgColor = when (status.lowercase()) {
        "paid", "active", "admitted" -> StatusSuccess.copy(alpha = 0.1f)
        "pending", "contacted" -> StatusWarning.copy(alpha = 0.15f)
        "overdue" -> ErrorColor.copy(alpha = 0.1f)
        "new" -> PrimaryColor.copy(alpha = 0.1f)
        else -> SurfaceContainerHighest
    }
    val textColor = when (status.lowercase()) {
        "paid", "active", "admitted" -> Color(0xFF137333)
        "pending", "contacted" -> Color(0xFF92400E)
        "overdue" -> Color(0xFFBA1A1A)
        "new" -> PrimaryColor
        else -> OnSurfaceVariantColor
    }
    val badgeIcon = when (status.lowercase()) {
        "paid", "active", "admitted" -> Icons.Default.CheckCircle
        "pending", "contacted" -> Icons.Default.Schedule
        "overdue" -> Icons.Default.Warning
        else -> null
    }
    val tamilLabel = when (status.lowercase()) {
        "paid" -> "செலுத்தப்பட்டது"
        "pending" -> "நிலுவையில்"
        "overdue" -> "தாமதம்"
        "active" -> "செயலில்"
        "new" -> "புதியது"
        "contacted" -> "தொடர்பு கொண்டது"
        "admitted" -> "சேர்க்கை"
        else -> status
    }

    Box(modifier = modifier) {
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(9999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showIcon && badgeIcon != null) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = status,
                            tint = textColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    Text(
                        text = status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            if (showTamil) {
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = tamilLabel,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
