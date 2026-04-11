package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun FacilitySummaryCard(
    name: String,
    tin: String,
    location: String,
    onClick: () -> Unit,
) {
    ClickableCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(Dimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            Row(
                modifier = Modifier
                    .size(48.dp)
                    .background(AppColors.SteelBlueTint, RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(imageVector = Icons.Rounded.Storefront, contentDescription = null, tint = AppColors.SteelBlue)
            }
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                Text(text = "TIN: $tin", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                Text(text = location, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            }
        }
    }
}
