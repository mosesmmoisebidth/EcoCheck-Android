package com.moses.inspectionapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.ButtonShape
import com.moses.inspectionapp.ui.theme.LocalAppSpacing
import com.moses.inspectionapp.ui.theme.LocalAppTypography

enum class PrimaryButtonTone {
    Primary,
    Accent,
    Danger,
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    leadingIconRotation: Float = 0f,
    tone: PrimaryButtonTone = PrimaryButtonTone.Primary,
    modifier: Modifier = Modifier,
) {
    val sp = LocalAppSpacing.current
    val ty = LocalAppTypography.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val baseColor = when (tone) {
        PrimaryButtonTone.Primary -> AppColors.SteelBlue
        PrimaryButtonTone.Accent -> AppColors.AccentOrange
        PrimaryButtonTone.Danger -> AppColors.AccentRed
    }
    val pressedColor = when (tone) {
        PrimaryButtonTone.Primary -> AppColors.PressedButton
        PrimaryButtonTone.Accent -> AppColors.AccentOrangePress
        PrimaryButtonTone.Danger -> AppColors.AccentRedPress
    }
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> AppColors.BorderLight
            pressed -> pressedColor
            else -> baseColor
        },
        label = "primaryButtonBg",
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = AppColors.BorderLight,
            disabledContentColor = AppColors.TextHint,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(sp.buttonHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.itemSpacing / 1.5f),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(sp.iconSize * 0.75f).graphicsLayer(rotationZ = leadingIconRotation),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = ty.bodyLarge,
                    letterSpacing = 0.3.sp,
                ),
                color = Color.White,
            )
            AnimatedVisibility(visible = isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(start = sp.itemSpacing / 2)
                        .size(sp.iconSize * 0.75f),
                )
            }
        }
    }
}
