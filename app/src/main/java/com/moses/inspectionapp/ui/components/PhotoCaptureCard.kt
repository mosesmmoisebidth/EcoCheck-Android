package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun PhotoCaptureCard(
    title: String,
    onCapture: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = AppColors.CardSurface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.medium),
            ) {
                Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = null, tint = AppColors.SteelBlue)
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = stringResource(R.string.optional), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                }
            }
            OutlinedButton(onClick = onCapture) {
                Text(text = stringResource(R.string.capture))
            }
        }
    }
}
