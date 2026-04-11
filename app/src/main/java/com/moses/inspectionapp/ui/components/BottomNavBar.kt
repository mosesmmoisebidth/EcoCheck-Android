package com.moses.inspectionapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onSelect: (BottomNavItem) -> Unit,
) {
    Surface(color = AppColors.CardSurface, modifier = Modifier.fillMaxWidth()) {
        Column {
            androidx.compose.material3.Divider(color = AppColors.BorderLight, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val selected = currentRoute?.startsWith(item.route) == true
                    val interactionSource = remember { MutableInteractionSource() }
                    val pressed by interactionSource.collectIsPressedAsState()
                    val iconColor = if (selected) AppColors.SteelBlue else AppColors.TextSecondary
                    val labelColor = if (selected) AppColors.SteelBlue else AppColors.TextSecondary
                    val indicatorColor by animateColorAsState(
                        targetValue = when {
                            selected -> AppColors.SteelBlueTint
                            pressed -> AppColors.SteelBlueTintMid
                            else -> Color.Transparent
                        },
                        label = "navIndicator",
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onSelect(item) },
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(indicatorColor, RoundedCornerShape(50.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = labelColor,
                        )
                    }
                }
            }
        }
    }
}
