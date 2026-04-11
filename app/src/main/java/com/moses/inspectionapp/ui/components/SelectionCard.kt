package com.moses.inspectionapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors

@Composable
fun SelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color = AppColors.SteelBlue,
    accentBgColor: Color = AppColors.SteelBlueTint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentBgColor
            pressed -> AppColors.PressedCard
            else -> AppColors.CardSurface
        },
        label = "selCardBg",
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor
            pressed -> AppColors.SteelBlueLight
            else -> AppColors.BorderLight
        },
        label = "selCardBorder",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (isSelected) accentColor else AppColors.PageBackground,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else AppColors.TextSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    2.dp,
                    if (isSelected) accentColor else AppColors.BorderMedium,
                    CircleShape,
                )
                .background(if (isSelected) accentColor else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape),
                )
            }
        }
    }
}
