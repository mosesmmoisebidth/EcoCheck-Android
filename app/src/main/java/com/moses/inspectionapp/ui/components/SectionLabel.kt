package com.moses.inspectionapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.ui.theme.AppColors

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.TextSecondary,
        letterSpacing = 1.2.sp,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
    )
}
