package com.moses.inspectionapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.ui.theme.AppColors

@Composable
fun AppFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> AppColors.SteelBlue
            pressed -> AppColors.HoverChip
            else -> AppColors.CardSurface
        },
        label = "chipBg",
    )
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            pressed -> AppColors.SteelBlue
            else -> AppColors.SteelBlue
        },
        label = "chipText",
    )

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(50.dp))
            .border(
                1.dp,
                if (isSelected) AppColors.SteelBlue else AppColors.BorderMedium,
                RoundedCornerShape(50.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}
