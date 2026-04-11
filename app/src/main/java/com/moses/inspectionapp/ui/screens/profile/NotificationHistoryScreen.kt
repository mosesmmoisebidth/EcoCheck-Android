package com.moses.inspectionapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.store.NotificationLogStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.formatDateTime

@Composable
fun NotificationHistoryScreen(onBack: () -> Unit) {
    val logs by NotificationLogStore.logs.collectAsState()

    LaunchedEffect(Unit) {
        NotificationLogStore.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = "Notifications", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                SectionHeader(title = "History")
                SecondaryButton(
                    text = "Clear history",
                    onClick = { NotificationLogStore.clear() },
                    enabled = logs.isNotEmpty(),
                )
            }

            if (logs.isEmpty()) {
                item {
                    EmptyState(
                        title = "No notifications yet",
                        message = "Notifications will appear here after they are sent.",
                        icon = Icons.Rounded.Notifications,
                    )
                }
            } else {
                items(logs) { entry ->
                    Surface(
                        color = AppColors.CardSurface,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = AppColors.TextPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatDateTime(entry.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextSecondary,
                                )
                            }
                            Text(
                                text = entry.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
