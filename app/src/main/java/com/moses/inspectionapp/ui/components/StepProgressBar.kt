package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.LocalAppSpacing
import com.moses.inspectionapp.ui.theme.LocalAppTypography

@Composable
fun StepProgressBar(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    onStepClick: ((Int) -> Unit)? = null,
) {
    if (steps.isEmpty()) return

    val sp = LocalAppSpacing.current
    val ty = LocalAppTypography.current
    val safeStep = currentStep.coerceIn(1, steps.size)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val slotWidth = maxWidth / steps.size
        val showLabels = maxWidth > 340.dp && slotWidth >= 44.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
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
                val stepModifier = if (onStepClick != null && stepNumber < safeStep) {
                    Modifier.clickable { onStepClick(stepNumber) }
                } else {
                    Modifier
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(max = slotWidth)
                        .then(stepModifier),
                ) {
                    Box(
                        modifier = Modifier
                            .size(sp.stepDotSize)
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
                                modifier = Modifier.size(sp.iconSize * 0.72f),
                            )
                        } else {
                            Text(
                                text = stepNumber.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = ty.labelSmall,
                                ),
                                color = if (isCurrent) Color.White else AppColors.TextHint,
                            )
                        }
                    }
                    if (showLabels) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = ty.labelSmall),
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .widthIn(max = slotWidth),
                        )
                    }
                }

                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = (sp.stepDotSize / 2) - 1.dp)
                            .height(2.dp)
                            .background(if (isCompleted) AppColors.SteelBlue else AppColors.BorderLight),
                    )
                }
            }
        }
    }
}
