package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.LocalAppSpacing
import com.moses.inspectionapp.ui.theme.LocalAppTypography

@Composable
fun SectionHeader(title: String, trailing: @Composable (() -> Unit)? = null) {
    val sp = LocalAppSpacing.current
    val ty = LocalAppTypography.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = ty.labelSmall,
                ),
                color = AppColors.TextSecondary,
            )
            if (trailing != null) {
                trailing()
            }
        }
        Divider(
            color = AppColors.Divider,
            thickness = 1.dp,
            modifier = Modifier.padding(top = sp.itemSpacing / 2),
        )
    }
}
