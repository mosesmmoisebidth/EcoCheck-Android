package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors

@Composable
fun AdaptiveBottomNav(
    windowSizeClass: WindowSizeClass,
    items: List<BottomNavItem>,
    currentRoute: String?,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        NavigationBar(
            modifier = modifier,
            containerColor = AppColors.CardSurface,
        ) {
            items.forEach { item ->
                val selected = currentRoute?.startsWith(item.route) == true
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(item) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = { Text(text = item.label) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.SteelBlue,
                        selectedTextColor = AppColors.SteelBlue,
                        indicatorColor = AppColors.SteelBlueTint,
                        unselectedIconColor = AppColors.TextSecondary,
                        unselectedTextColor = AppColors.TextSecondary,
                    ),
                )
            }
        }
    } else {
        NavigationRail(
            modifier = modifier,
            containerColor = AppColors.CardSurface,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                val selected = currentRoute?.startsWith(item.route) == true
                NavigationRailItem(
                    selected = selected,
                    onClick = { onSelect(item) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = { Text(text = item.label) },
                    alwaysShowLabel = true,
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = AppColors.SteelBlue,
                        selectedTextColor = AppColors.SteelBlue,
                        indicatorColor = AppColors.SteelBlueTint,
                        unselectedIconColor = AppColors.TextSecondary,
                        unselectedTextColor = AppColors.TextSecondary,
                    ),
                )
            }
        }
    }
}

