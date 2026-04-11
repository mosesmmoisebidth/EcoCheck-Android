package com.moses.inspectionapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.ui.components.ActionCard
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.mouseWheelScroll

@Composable
fun HomeScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNewAssessment: () -> Unit,
    onEnrollFacility: () -> Unit,
    onMyInspections: () -> Unit,
    onSyncNow: () -> Unit,
    onStats: () -> Unit,
) {
    val repository = AppContainer.repository
    val user = repository.userProfile.collectAsState().value
    val stats = repository.stats.collectAsState().value
    val pending = repository.pendingCounts.collectAsState().value
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground)
            .mouseWheelScroll(scrollState)
            .verticalScroll(scrollState),
    ) {
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    top = Dimens.itemGap,
                    bottom = Dimens.sectionGap,
                ),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                color = AppColors.NavyDark,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .background(AppColors.NavyDark)
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.good_morning),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextOnDarkMuted,
                        )
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.TextOnDark,
                        )
                        Text(
                            text = "${user.sector} \u2022 ${user.district}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.TextOnDarkMuted,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            StatChip(
                                label = stringResource(R.string.stat_today_label),
                                value = stats.todayInspections.toString(),
                                dotColor = AppColors.AccentGreen,
                            )
                            StatChip(
                                label = stringResource(R.string.stat_pending_label),
                                value = (pending.facilities + pending.inspections).toString(),
                                dotColor = AppColors.AccentOrange,
                            )
                            StatChip(
                                label = stringResource(R.string.stat_week_label),
                                value = stats.weekInspections.toString(),
                                dotColor = AppColors.SteelBlueLight,
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPadding)
                .padding(bottom = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            SectionHeader(title = stringResource(R.string.quick_actions))
            ActionCard(
                title = stringResource(R.string.new_assessment),
                description = stringResource(R.string.start_food_safety),
                icon = Icons.Rounded.Add,
                onClick = onNewAssessment,
            )
            ActionCard(
                title = stringResource(R.string.enroll_facility),
                description = stringResource(R.string.add_new_restobar),
                icon = Icons.Rounded.LocationOn,
                onClick = onEnrollFacility,
            )
            ActionCard(
                title = stringResource(R.string.my_inspections),
                description = stringResource(R.string.view_recent_reports),
                icon = Icons.Rounded.Assignment,
                onClick = onMyInspections,
            )
            ActionCard(
                title = stringResource(R.string.sync_now),
                description = stringResource(R.string.upload_pending),
                icon = Icons.Rounded.Sync,
                onClick = onSyncNow,
            )
            ActionCard(
                title = stringResource(R.string.basic_stats),
                description = stringResource(R.string.daily_weekly_totals),
                icon = Icons.Rounded.Assessment,
                onClick = onStats,
            )
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, dotColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppColors.NavyMid,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, RoundedCornerShape(50)),
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextOnDark,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextOnDarkMuted,
                )
            }
        }
    }
}
