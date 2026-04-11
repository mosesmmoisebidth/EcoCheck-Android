package com.moses.inspectionapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors

@Composable
fun StepProgressBar(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    onStepClick: ((Int) -> Unit)? = null,
) {
    val safeStep = currentStep.coerceIn(1, steps.size)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        steps.forEachIndexed { index, label ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < safeStep
            val isCurrent = stepNumber == safeStep
            val fillColor = when {
                isCompleted -> AppColors.SteelBlue
                isCurrent -> AppColors.AccentOrange
                else -> Color.Transparent
            }
            val borderColor = when {
                isCurrent || isCompleted -> fillColor
                else -> AppColors.BorderMedium
            }
            val lineProgress by animateFloatAsState(
                targetValue = if (isCompleted) 1f else 0f,
                label = "lineProgress",
            )
            val stepModifier = if (onStepClick != null && stepNumber < safeStep) {
                Modifier.clickable { onStepClick(stepNumber) }
            } else {
                Modifier
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = stepModifier,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(fillColor)
                            .border(1.5.dp, borderColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        } else {
                            Text(
                                text = stepNumber.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isCurrent) Color.White else AppColors.TextHint,
                            )
                        }
                    }
                    if (index < steps.lastIndex) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(24.dp)
                                .background(AppColors.BorderLight),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(lineProgress)
                                    .height(2.dp)
                                    .background(if (isCompleted) AppColors.SteelBlue else AppColors.BorderLight),
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
