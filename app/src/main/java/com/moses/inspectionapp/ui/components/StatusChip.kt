package com.moses.inspectionapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.ChipShape

enum class StatusChipStyle {
    Pending,
    Synced,
    Failed,
    Conflict,
}

@Composable
fun StatusChip(
    text: String,
    style: StatusChipStyle,
    modifier: Modifier = Modifier,
) {
    val (background, contentColor, dotColor) = when (style) {
        StatusChipStyle.Pending -> Triple(AppColors.SyncPendingBg, AppColors.SyncPending, AppColors.SyncPending)
        StatusChipStyle.Synced -> Triple(AppColors.SyncSyncedBg, AppColors.SyncSynced, AppColors.SyncSynced)
        StatusChipStyle.Failed -> Triple(AppColors.SyncFailedBg, AppColors.SyncFailed, AppColors.SyncFailed)
        StatusChipStyle.Conflict -> Triple(AppColors.StatusProsecutionBg, AppColors.StatusProsecution, AppColors.StatusProsecution)
    }
    val transition = rememberInfiniteTransition(label = "pendingPulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Row(
        modifier = modifier
            .background(background, ChipShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor.copy(alpha = if (style == StatusChipStyle.Pending) pulseAlpha else 1f), CircleShape),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
        )
    }
}
