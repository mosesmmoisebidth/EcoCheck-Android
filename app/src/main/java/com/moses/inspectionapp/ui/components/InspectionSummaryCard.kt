package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun InspectionSummaryCard(
    facilityName: String,
    dateLabel: String,
    totalFine: String,
    decision: String,
    decisionColor: Color,
    onClick: () -> Unit,
) {
    ClickableCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .padding(Dimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 56.dp)
                    .background(decisionColor, RoundedCornerShape(50.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = facilityName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .background(decisionColor.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = decision,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = decisionColor,
                        )
                    }
                    Text(
                        text = totalFine,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.SteelBlue,
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = AppColors.TextSecondary,
            )
        }
    }
}
