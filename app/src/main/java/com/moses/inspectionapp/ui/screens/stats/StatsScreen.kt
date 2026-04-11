package com.moses.inspectionapp.ui.screens.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.SectionLabel
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.formatDateTime
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val repository = AppContainer.repository
    val stats = repository.stats.collectAsState().value
    val pending = repository.pendingCounts.collectAsState().value
    val inspections = repository.inspections.collectAsState().value
    val facilities = repository.facilities.collectAsState().value

    val now = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L
    val weekStart = now - 7 * dayMillis
    val monthStart = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val weekFine = inspections.filter { it.createdAt >= weekStart }.sumOf { it.totalFine }
    val monthFine = inspections.filter { it.createdAt >= monthStart }.sumOf { it.totalFine }
    val allTimeFine = inspections.sumOf { it.totalFine }
    val topDecision = inspections
        .groupingBy { it.decision }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
    val recentInspections = inspections
        .sortedByDescending { it.createdAt }
        .take(5)
    val facilityMap = facilities.associateBy { it.id }
    val pendingCount = pending.facilities + pending.inspections

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.basic_stats), onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            contentPadding = PaddingValues(vertical = Dimens.sectionGap),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ActivityStatCard(
                        value = stats.todayInspections,
                        label = stringResource(R.string.stat_today_label),
                        color = AppColors.AccentGreen,
                        modifier = Modifier.weight(1f),
                    )
                    ActivityStatCard(
                        value = stats.weekInspections,
                        label = stringResource(R.string.stat_week_label),
                        color = AppColors.SteelBlue,
                        modifier = Modifier.weight(1f),
                    )
                    ActivityStatCard(
                        value = pendingCount,
                        label = stringResource(R.string.stat_pending_label),
                        color = AppColors.AccentOrange,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                FinancialHighlightCard(
                    weekFine = weekFine,
                    monthFine = monthFine,
                    allTimeFine = allTimeFine,
                )
            }

            item {
                TopDecisionCard(topDecision = topDecision)
            }

            item {
                SectionLabel(text = stringResource(R.string.recent_inspections))
            }

            if (recentInspections.isEmpty()) {
                item { EmptyRecentState() }
            } else {
                items(recentInspections, key = { it.id }) { inspection ->
                    val facility = facilityMap[inspection.facilityId]
                    RecentInspectionCard(
                        inspection = inspection,
                        facilitySector = facility?.sector.orEmpty(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityStatCard(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(50)),
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                ),
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = AppColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun FinancialHighlightCard(
    weekFine: Int,
    monthFine: Int,
    allTimeFine: Int,
) {
    val weekColor = if (weekFine > 0) AppColors.TextOnDark else AppColors.TextOnDarkMuted
    val monthColor = if (monthFine > 0) AppColors.TextOnDark else AppColors.TextOnDarkMuted
    val allTimeColor = if (allTimeFine > 0) AppColors.TextOnDark else AppColors.TextOnDarkMuted

    Surface(
        color = AppColors.NavyDark,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.total_fines_week_label),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
                color = AppColors.TextOnDarkMuted,
                fontSize = 10.sp,
            )
            Text(
                text = formatRwf(weekFine),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = weekColor,
                fontSize = 28.sp,
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f)),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.this_month),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextOnDarkMuted,
                    )
                    Text(
                        text = formatRwf(monthFine),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = monthColor,
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color.White.copy(alpha = 0.15f)),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = stringResource(R.string.total_all_time),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextOnDarkMuted,
                    )
                    Text(
                        text = formatRwf(allTimeFine),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = allTimeColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun TopDecisionCard(topDecision: Decision?) {
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Gavel,
                    contentDescription = null,
                    tint = AppColors.SteelBlue,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.top_decision_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
            }
            if (topDecision == null) {
                Text(
                    text = stringResource(R.string.no_inspections_yet),
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DecisionBadgeLarge(decision = topDecision)
            }
        }
    }
}

@Composable
private fun RecentInspectionCard(
    inspection: Inspection,
    facilitySector: String,
) {
    val style = decisionStyle(inspection.decision)
    val subtitle = listOfNotNull(
        formatDateTime(inspection.createdAt).ifBlank { null },
        facilitySector.ifBlank { null },
    ).joinToString(" \u2022 ")

    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight()
                    .background(style.color),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inspection.facilityName.ifBlank { stringResource(R.string.facility) },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.TextPrimary,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DecisionBadgeSmall(decision = inspection.decision)
                    Text(
                        text = formatRwf(inspection.totalFine),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRecentState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            color = AppColors.SteelBlueTint,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Assignment,
                    contentDescription = null,
                    tint = AppColors.SteelBlue,
                )
            }
        }
        Text(
            text = stringResource(R.string.no_inspections_yet),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.TextPrimary,
        )
        Text(
            text = stringResource(R.string.start_new_assessment_hint),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DecisionBadgeLarge(decision: Decision) {
    val style = decisionStyle(decision)
    Surface(
        color = style.color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.5.dp, style.color),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.color,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = decisionLabel(decision),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = style.color,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun DecisionBadgeSmall(decision: Decision) {
    val style = decisionStyle(decision)
    Surface(
        color = style.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.color,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = decisionLabel(decision),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = style.color,
                fontSize = 11.sp,
            )
        }
    }
}

private data class DecisionStyle(
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private fun decisionStyle(decision: Decision): DecisionStyle {
    return when (decision) {
        Decision.WARNING -> DecisionStyle(AppColors.StatusWarning, Icons.Rounded.Warning)
        Decision.CLOSURE_IMMEDIATE -> DecisionStyle(AppColors.AccentRed, Icons.Rounded.Block)
        Decision.CLOSURE_DEADLINE -> DecisionStyle(AppColors.AccentOrange, Icons.Rounded.Schedule)
        Decision.PROSECUTION_RECOMMENDED -> DecisionStyle(Color(0xFF6B21A8), Icons.Rounded.Gavel)
        Decision.NO_ACTION -> DecisionStyle(AppColors.AccentGreen, Icons.Rounded.CheckCircle)
    }
}

private fun formatRwf(amount: Int): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault())
    return "${formatter.format(amount)} RWF"
}
