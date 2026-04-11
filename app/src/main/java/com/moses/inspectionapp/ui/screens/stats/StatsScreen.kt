package com.moses.inspectionapp.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.StatusChip
import com.moses.inspectionapp.ui.components.StatusChipStyle
import com.moses.inspectionapp.ui.theme.Dimens

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val repository = AppContainer.repository
    val stats = repository.stats.collectAsState().value
    val pending = repository.pendingCounts.collectAsState().value

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = stringResource(R.string.basic_stats), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.small)) {
                StatusChip(
                    text = stringResource(R.string.stat_today, stats.todayInspections),
                    style = StatusChipStyle.Synced,
                )
                StatusChip(
                    text = stringResource(R.string.stat_week, stats.weekInspections),
                    style = StatusChipStyle.Conflict,
                )
                StatusChip(
                    text = stringResource(
                        R.string.stat_pending,
                        pending.facilities + pending.inspections,
                    ),
                    style = StatusChipStyle.Pending,
                )
            }
            Text(text = stringResource(R.string.total_fines_week, stats.totalFines))
            Text(text = stringResource(R.string.top_decision, stringResource(R.string.decision_warning)))
        }
    }
}
