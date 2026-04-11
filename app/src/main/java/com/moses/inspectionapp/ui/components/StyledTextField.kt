package com.moses.inspectionapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 360
                    0f at 0
                    -6f at 60
                    6f at 120
                    -4f at 180
                    4f at 240
                    0f at 300
                },
            )
        }
    }

    val readOnlyContainer = Color(0xFFF0F4FA)
    val focusedContainer = if (readOnly) readOnlyContainer else AppColors.FocusedInput
    val unfocusedContainer = if (readOnly) readOnlyContainer else AppColors.InputSurface
    val focusedBorder = if (readOnly) AppColors.BorderLight else AppColors.SteelBlue
    val unfocusedBorder = AppColors.BorderLight

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = AppColors.TextHint) } },
        leadingIcon = leadingIcon?.let {
            { androidx.compose.material3.Icon(it, contentDescription = null, tint = AppColors.TextSecondary) }
        },
        trailingIcon = trailingIcon,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusedBorder,
            unfocusedBorderColor = unfocusedBorder,
            focusedLabelColor = AppColors.SteelBlue,
            unfocusedLabelColor = AppColors.TextSecondary,
            cursorColor = AppColors.SteelBlue,
            focusedContainerColor = focusedContainer,
            unfocusedContainerColor = unfocusedContainer,
            errorBorderColor = AppColors.AccentRed,
            errorLabelColor = AppColors.AccentRed,
            selectionColors = TextSelectionColors(
                handleColor = AppColors.SteelBlue,
                backgroundColor = AppColors.SteelBlueTint,
            ),
        ),
        modifier = modifier
            .offset(x = shakeOffset.value.dp)
            .fillMaxWidth(),
        supportingText = {
            if (isError && !errorText.isNullOrBlank()) {
                Text(text = errorText, color = AppColors.AccentRed, style = MaterialTheme.typography.labelSmall)
            }
        },
    )
}
