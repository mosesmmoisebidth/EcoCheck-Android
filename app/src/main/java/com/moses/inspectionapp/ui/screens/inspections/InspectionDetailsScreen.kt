package com.moses.inspectionapp.ui.screens.inspections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Storefront
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.InfoRow
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StatusChip
import com.moses.inspectionapp.ui.components.StatusChipStyle
import com.moses.inspectionapp.ui.components.NoImagePlaceholder
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.formatDateTime
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import com.moses.inspectionapp.ui.util.syncStatusLabel
import com.moses.inspectionapp.ui.util.visitTypeLabel
import java.io.File

@Composable
fun InspectionDetailsScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onSharePdf: () -> Unit,
    onEdit: () -> Unit,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val inspections = repository.inspections.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val user = repository.userProfile.collectAsState().value
    val selectedId = DraftStore.selectedInspectionId.collectAsState().value
    val inspection = inspections.firstOrNull { it.id == selectedId }
    val facility = facilities.firstOrNull { it.id == inspection?.facilityId }
    val scrollState = rememberScrollState()
    val photoScrollState = rememberScrollState()
    val now = System.currentTimeMillis()
    val canEdit = inspection != null &&
        inspection.createdBy == user.id &&
        now - inspection.createdAt <= 24 * 60 * 60 * 1000

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.inspection_details), onBack = onBack)
            OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .mouseWheelScroll(scrollState)
                    .verticalScroll(scrollState)
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            ) {
                if (inspection == null) {
                    EmptyState(
                        title = stringResource(R.string.no_inspection_selected),
                        message = stringResource(R.string.select_inspection_from_list),
                        icon = Icons.Rounded.Warning,
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.NavyDark,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(modifier = Modifier.padding(Dimens.cardPadding)) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = inspection.facilityName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = AppColors.TextOnDark,
                                )
                                val coords = if (facility?.latitude != null && facility?.longitude != null) {
                                    "${facility.latitude}, ${facility.longitude}"
                                } else {
                                    ""
                                }
                                Text(
                                    text = "${facility?.tin ?: ""} ${if (coords.isNotBlank()) "| $coords" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextOnDarkMuted,
                                )
                                Text(
                                    text = formatDateTime(inspection.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextOnDarkMuted,
                                )
                            }
                            StatusChip(
                                text = syncStatusLabel(inspection.syncStatus),
                                style = when (inspection.syncStatus) {
                                    SyncStatus.PENDING -> StatusChipStyle.Pending
                                    SyncStatus.SYNCED -> StatusChipStyle.Synced
                                    SyncStatus.CONFLICT -> StatusChipStyle.Conflict
                                },
                                modifier = Modifier.align(Alignment.TopEnd),
                            )
                        }
                    }

                    SummaryCard {
                        Text(text = stringResource(R.string.visit_info), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                        InfoRow(
                            icon = Icons.Rounded.CheckCircle,
                            label = stringResource(R.string.visit),
                            value = visitTypeLabel(inspection.visitType),
                        )
                        InfoRow(
                            icon = Icons.Rounded.Event,
                            label = stringResource(R.string.inspection_date_label),
                            value = formatDateTime(inspection.createdAt),
                        )
                        InfoRow(
                            icon = Icons.Rounded.Group,
                            label = stringResource(R.string.inspection_team),
                            value = inspection.teamMembers.joinToString(),
                        )
                    }

                    SummaryCard {
                        Text(text = stringResource(R.string.faults), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                        if (inspection.faultCount == 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = AppColors.StatusCompliant)
                                Text(text = stringResource(R.string.no_faults_found), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.faults_selected, inspection.faultCount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextPrimary,
                            )
                        }
                    }

                    SummaryCard {
                        Text(text = stringResource(R.string.decision), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                        DecisionBadge(decision = inspection.decision)
                    }

                    SummaryCard {
                        Text(text = stringResource(R.string.financial_breakdown), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                        val subtotal = inspection.totalFine - inspection.adjustmentAmount
                        SummaryRow(label = stringResource(R.string.subtotal), value = "$subtotal RWF")
                        SummaryRow(label = stringResource(R.string.adjustment), value = "${inspection.adjustmentAmount} RWF")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.SteelBlueTint, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(text = stringResource(R.string.total_fine), style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                            Text(
                                text = "${inspection.totalFine} RWF",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = AppColors.SteelBlue,
                            )
                        }
                    }

                    SummaryCard {
                        Text(text = stringResource(R.string.comments), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                        Text(
                            text = inspection.comments.ifBlank { stringResource(R.string.not_set) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary,
                        )
                    }

                    SummaryCard {
                        Text(text = stringResource(R.string.recommendations), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                        Text(
                            text = inspection.recommendations.ifBlank { stringResource(R.string.not_set) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary,
                        )
                    }

                    SummaryCard {
                        Text(
                            text = stringResource(R.string.inspection_photos),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.TextSecondary,
                        )
                        if (inspection.photoPaths.isEmpty()) {
                            NoImagePlaceholder(
                                label = stringResource(R.string.no_photo_captured),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(photoScrollState),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                inspection.photoPaths.forEach { path ->
                                    val model = if (path.startsWith("http", ignoreCase = true)) {
                                        path
                                    } else {
                                        File(path)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AppColors.SteelBlueTint,
                                    ) {
                                        AsyncImage(
                                            model = model,
                                            contentDescription = stringResource(R.string.inspection_photos),
                                            modifier = Modifier.size(140.dp),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (inspection != null) {
            Surface(
                color = AppColors.CardSurface,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screenPadding, vertical = Dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.share_pdf),
                        leadingIcon = Icons.Rounded.PictureAsPdf,
                        onClick = onSharePdf,
                    )
                    SecondaryButton(
                        text = stringResource(R.string.edit_within_24h),
                        leadingIcon = if (canEdit) Icons.Rounded.Edit else Icons.Rounded.Lock,
                        onClick = onEdit,
                        enabled = canEdit,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(12.dp)),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
    }
}

@Composable
private fun DecisionBadge(decision: Decision) {
    val config = decisionBadgeConfig(decision)
    Surface(
        color = config.background,
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = config.icon, contentDescription = null, tint = config.color)
            Text(
                text = decisionLabel(decision),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = config.color,
            )
        }
    }
}

private data class DecisionBadgeConfig(
    val color: Color,
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun decisionBadgeConfig(decision: Decision): DecisionBadgeConfig {
    return when (decision) {
        Decision.WARNING -> DecisionBadgeConfig(AppColors.StatusWarning, AppColors.StatusWarningBg, Icons.Rounded.Warning)
        Decision.CLOSURE_IMMEDIATE -> DecisionBadgeConfig(AppColors.StatusImmediate, AppColors.StatusImmediateBg, Icons.Rounded.Block)
        Decision.CLOSURE_DEADLINE -> DecisionBadgeConfig(AppColors.StatusClosure, AppColors.StatusClosureBg, Icons.Rounded.Schedule)
        Decision.PROSECUTION_RECOMMENDED -> DecisionBadgeConfig(AppColors.StatusProsecution, AppColors.StatusProsecutionBg, Icons.Rounded.Gavel)
        Decision.NO_ACTION -> DecisionBadgeConfig(AppColors.StatusCompliant, AppColors.StatusCompliantBg, Icons.Rounded.CheckCircle)
    }
}
