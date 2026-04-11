package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun ErrorState(
    title: String,
    message: String,
    icon: ImageVector? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.large),
        verticalArrangement = Arrangement.spacedBy(Dimens.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.AccentRed,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = AppColors.AccentRed)
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
    }
}
