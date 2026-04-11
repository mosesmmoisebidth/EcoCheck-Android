package com.moses.inspectionapp.ui.screens.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.SyncStateStore
import com.moses.inspectionapp.data.sync.SyncManager
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.PrimaryButtonTone
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StatusChip
import com.moses.inspectionapp.ui.components.StatusChipStyle
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun SyncScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onViewConflicts: () -> Unit = {},
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val pending = repository.pendingCounts.collectAsState().value
    val isSyncing = SyncStateStore.isSyncing.collectAsState().value
    val context = LocalContext.current
    val hasPending = pending.facilities + pending.inspections > 0
    val totalPending = pending.facilities + pending.inspections
    val transition = rememberInfiniteTransition(label = "syncRotation")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Restart),
        label = "rotation",
    )
    val recentItems = listOf(
        lastSyncLabel,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.sync_center), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    top = Dimens.itemGap,
                    bottom = Dimens.sectionGap,
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
        ) {
            Surface(
                color = if (isOffline) AppColors.StatusWarningBg else AppColors.NavyDark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                ) {
                    Icon(
                        imageVector = if (isOffline) Icons.Rounded.WifiOff else Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = if (isOffline) AppColors.StatusWarning else AppColors.TextOnDark,
                        modifier = Modifier.size(28.dp),
                    )
                    Column {
                        Text(
                            text = if (isOffline) stringResource(R.string.offline) else stringResource(R.string.connected),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isOffline) AppColors.TextPrimary else AppColors.TextOnDark,
                        )
                        Text(
                            text = if (isOffline) stringResource(R.string.changes_saved_locally) else stringResource(R.string.last_sync, lastSyncLabel),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOffline) AppColors.TextSecondary else AppColors.TextOnDarkMuted,
                        )
                    }
                }
            }

            Surface(
                color = AppColors.CardSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.pending_records, pending.facilities + pending.inspections),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(R.string.pending_breakdown, pending.facilities, pending.inspections),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                    )
                }
            }

            PrimaryButton(
                text = if (isSyncing) {
                    stringResource(R.string.syncing_progress, totalPending)
                } else {
                    stringResource(R.string.sync_now)
                },
                leadingIcon = Icons.Rounded.Sync,
                leadingIconRotation = if (isSyncing) rotation else 0f,
                onClick = { SyncManager.enqueue(context) },
                enabled = !isOffline && hasPending && !isSyncing,
                tone = PrimaryButtonTone.Accent,
            )
            SecondaryButton(text = stringResource(R.string.view_conflicts), onClick = onViewConflicts)

            Surface(
                color = AppColors.CardSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                    Text(
                        text = stringResource(R.string.recent_activity),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextSecondary,
                    )
                    if (recentItems.firstOrNull().isNullOrBlank()) {
                        Text(text = stringResource(R.string.no_recent_activity), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                    } else {
                        recentItems.forEach { label ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = stringResource(R.string.last_sync, label), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                                StatusChip(
                                    text = if (isOffline) stringResource(R.string.status_pending) else stringResource(R.string.status_synced),
                                    style = if (isOffline) StatusChipStyle.Pending else StatusChipStyle.Synced,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
