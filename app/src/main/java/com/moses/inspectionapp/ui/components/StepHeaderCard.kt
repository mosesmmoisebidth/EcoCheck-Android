package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.CardShape
import com.moses.inspectionapp.ui.theme.LocalAppSpacing
import com.moses.inspectionapp.ui.theme.LocalAppTypography

@Composable
fun StepHeaderCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val sp = LocalAppSpacing.current
    val ty = LocalAppTypography.current
    Surface(
        color = AppColors.CardSurface,
        shape = CardShape,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = sp.cardMaxWidth),
    ) {
        Column(modifier = Modifier.padding(sp.cardPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = ty.titleLarge),
                color = AppColors.SteelBlue,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = ty.bodyMedium),
                    color = AppColors.TextSecondary,
                )
            }
        }
    }
}
