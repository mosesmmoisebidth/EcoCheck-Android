package com.moses.inspectionapp.ui.screens.inspections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Comment
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.LabelValueRow
import com.moses.inspectionapp.ui.components.NoImagePlaceholder
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.SectionLabel
import com.moses.inspectionapp.ui.components.StatusChip
import com.moses.inspectionapp.ui.components.StatusChipStyle
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.CardShape
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.formatDateTime
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
    val inspectionTypes = repository.inspectionTypes.collectAsState().value
    val user = repository.userProfile.collectAsState().value
    val selectedId = DraftStore.selectedInspectionId.collectAsState().value
    val inspection = inspections.firstOrNull { it.id == selectedId }
    val facility = facilities.firstOrNull { facilityItem ->
        facilityItem.id == inspection?.facilityId || facilityItem.serverId == inspection?.facilityId
    }
    val inspectionType = inspectionTypes.firstOrNull { it.id == inspection?.inspectionTypeId }
    val photoScrollState = rememberScrollState()
    val now = System.currentTimeMillis()
    val canEdit = inspection != null &&
        inspection.createdBy == user.id &&
        now - inspection.createdAt <= 24 * 60 * 60 * 1000

    val inspectionTypeLabel = inspectionType?.name ?: stringResource(R.string.inspection)
    val inspectionDateLabel = inspection?.createdAt?.let { formatDateTime(it) }.orEmpty()
    val locationLabel = listOfNotNull(facility?.sector, facility?.district)
        .filter { it.isNotBlank() }
        .joinToString(", ")
    val teamMembers = inspection?.teamMembers?.filter { it.isNotBlank() } ?: emptyList()
    val teamLabel = if (teamMembers.size == 1) "1 Team member" else "${teamMembers.size} Team members"
    val subtotal = (inspection?.totalFine ?: 0) - (inspection?.adjustmentAmount ?: 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.inspection_details), onBack = onBack)
            OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                contentPadding = PaddingValues(bottom = 160.dp),
            ) {
                if (inspection == null) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.no_inspection_selected),
                            message = stringResource(R.string.select_inspection_from_list),
                            icon = Icons.Rounded.Warning,
                        )
                    }
                } else {
                    item {
                        val statusStyle = when (inspection.syncStatus) {
                            SyncStatus.PENDING -> StatusChipStyle.Pending
                            SyncStatus.SYNCED -> StatusChipStyle.Synced
                            SyncStatus.CONFLICT -> StatusChipStyle.Conflict
                        }
                        HeroSummaryCard(
                            facilityName = inspection.facilityName,
                            facilityTin = facility?.tin.orEmpty(),
                            locationLabel = locationLabel,
                            inspectionTypeLabel = inspectionTypeLabel,
                            inspectionDateLabel = inspectionDateLabel,
                            teamLabel = teamLabel,
                            statusText = syncStatusLabel(inspection.syncStatus),
                            statusStyle = statusStyle,
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Visit Details")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.visit_info),
                                    icon = Icons.Rounded.Schedule,
                                )
                                DividerLine()
                                LabelValueRow(
                                    label = stringResource(R.string.visit_type),
                                    value = visitTypeLabel(inspection.visitType),
                                )
                                LabelValueRow(
                                    label = stringResource(R.string.inspection_date_label),
                                    value = inspectionDateLabel,
                                )
                                LabelValueRow(
                                    label = stringResource(R.string.inspection_type_label),
                                    value = inspectionTypeLabel,
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Inspection Team")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.inspection_team),
                                    icon = Icons.Rounded.Group,
                                )
                                DividerLine()
                                if (teamMembers.isEmpty()) {
                                    Text(
                                        text = "No team members listed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextSecondary,
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        teamMembers.forEachIndexed { index, member ->
                                            TeamMemberRow(
                                                name = member,
                                                isLead = index == 0,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Faults Found")
                            ReviewCard {
                                val hasFaults = inspection.faultCount > 0
                                val headerColor = if (hasFaults) AppColors.AccentRed else AppColors.AccentGreen
                                CardHeader(
                                    title = stringResource(R.string.faults),
                                    icon = if (hasFaults) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                                    iconTint = headerColor,
                                    textColor = headerColor,
                                )
                                DividerLine()
                                if (!hasFaults) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(AppColors.AccentGreenBg, RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = AppColors.AccentGreen,
                                            modifier = Modifier.size(48.dp),
                                        )
                                        Text(
                                            text = stringResource(R.string.no_faults_found),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = AppColors.AccentGreen,
                                        )
                                        Text(
                                            text = "Facility is compliant",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.faults_selected, inspection.faultCount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextPrimary,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = AppColors.SteelBlueTint,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = stringResource(R.string.subtotal),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = AppColors.SteelBlue,
                                            )
                                            Text(
                                                text = stringResource(R.string.rwf_amount, subtotal),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = AppColors.SteelBlue,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Decision")
                            val config = decisionCardConfig(inspection.decision)
                            Surface(
                                color = config.background,
                                shape = CardShape,
                                border = BorderStroke(1.5.dp, config.color),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(Dimens.cardPadding),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = config.icon,
                                        contentDescription = null,
                                        tint = config.color,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "DECISION",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.2.sp,
                                            ),
                                            color = AppColors.TextSecondary,
                                        )
                                        Text(
                                            text = decisionLabel(inspection.decision),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = config.color,
                                        )
                                        Text(
                                            text = config.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Financial Summary")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.financial_breakdown),
                                    icon = Icons.Rounded.Payments,
                                )
                                DividerLine()
                                LabelValueRow(
                                    label = stringResource(R.string.subtotal),
                                    value = stringResource(R.string.rwf_amount, subtotal),
                                )
                                DividerLine()
                                val adjustmentColor = when {
                                    inspection.adjustmentAmount > 0 -> AppColors.AccentGreen
                                    inspection.adjustmentAmount < 0 -> AppColors.AccentRed
                                    else -> AppColors.TextPrimary
                                }
                                LabelValueRow(
                                    label = stringResource(R.string.adjustment),
                                    value = stringResource(R.string.rwf_amount, inspection.adjustmentAmount),
                                    valueColor = adjustmentColor,
                                )
                                DividerLine(stronger = true)
                                Surface(
                                    color = AppColors.SteelBlueTint,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.total_fine),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = AppColors.TextPrimary,
                                        )
                                        Text(
                                            text = stringResource(R.string.rwf_amount, inspection.totalFine),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = AppColors.SteelBlue,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Comments")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.comments),
                                    icon = Icons.Rounded.Comment,
                                )
                                DividerLine()
                                Text(
                                    text = inspection.comments.ifBlank { stringResource(R.string.not_set) },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextPrimary,
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Recommendations")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.recommendations),
                                    icon = Icons.Rounded.Lightbulb,
                                    iconTint = AppColors.AccentGold,
                                )
                                DividerLine()
                                Text(
                                    text = inspection.recommendations.ifBlank { stringResource(R.string.not_set) },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextPrimary,
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Inspection Photos")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.inspection_photos),
                                    icon = Icons.Rounded.Assignment,
                                )
                                DividerLine()
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
private fun HeroSummaryCard(
    facilityName: String,
    facilityTin: String,
    locationLabel: String,
    inspectionTypeLabel: String,
    inspectionDateLabel: String,
    teamLabel: String,
    statusText: String,
    statusStyle: StatusChipStyle,
) {
    Surface(
        color = AppColors.NavyDark,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "INSPECTION SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = Color.White,
                )
                StatusChip(
                    text = statusText,
                    style = statusStyle,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Storefront,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = facilityName.ifBlank { "Facility" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                    if (facilityTin.isNotBlank()) {
                        Text(
                            text = facilityTin,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }
            if (locationLabel.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeroChip(icon = Icons.Rounded.Event, label = inspectionDateLabel)
                HeroChip(icon = Icons.Rounded.Assignment, label = inspectionTypeLabel)
                HeroChip(icon = Icons.Rounded.Group, label = teamLabel)
            }
        }
    }
}

@Composable
private fun HeroChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ReviewCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = AppColors.CardSurface,
        shape = CardShape,
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun CardHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = AppColors.SteelBlue,
    textColor: Color = AppColors.TextPrimary,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
        Icon(imageVector = icon, contentDescription = null, tint = iconTint)
    }
}

@Composable
private fun DividerLine(stronger: Boolean = false) {
    val color = if (stronger) AppColors.BorderMedium else AppColors.BorderLight
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}

@Composable
private fun TeamMemberRow(name: String, isLead: Boolean) {
    val initials = initialsForName(name)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = AppColors.NavyDark,
            shape = CircleShape,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextPrimary,
                )
                if (isLead) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = AppColors.AccentGold,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Text(
                text = if (isLead) "Lead Officer" else "Team Member",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
            )
        }
    }
}

private data class DecisionCardConfig(
    val color: Color,
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
)

private fun decisionCardConfig(decision: Decision): DecisionCardConfig {
    return when (decision) {
        Decision.WARNING -> DecisionCardConfig(
            color = AppColors.StatusWarning,
            background = AppColors.StatusWarningBg,
            icon = Icons.Rounded.Warning,
            description = "Formal warning issued to facility owner",
        )
        Decision.CLOSURE_IMMEDIATE -> DecisionCardConfig(
            color = AppColors.StatusImmediate,
            background = AppColors.StatusImmediateBg,
            icon = Icons.Rounded.Block,
            description = "Facility must close immediately",
        )
        Decision.CLOSURE_DEADLINE -> DecisionCardConfig(
            color = AppColors.StatusClosure,
            background = AppColors.StatusClosureBg,
            icon = Icons.Rounded.Schedule,
            description = "Facility has a deadline to comply",
        )
        Decision.PROSECUTION_RECOMMENDED -> DecisionCardConfig(
            color = AppColors.AccentGold,
            background = AppColors.StatusWarningBg,
            icon = Icons.Rounded.Payments,
            description = "Monetary fine issued",
        )
        Decision.NO_ACTION -> DecisionCardConfig(
            color = AppColors.StatusCompliant,
            background = AppColors.StatusCompliantBg,
            icon = Icons.Rounded.CheckCircle,
            description = "Facility is compliant - no action needed",
        )
    }
}

private fun initialsForName(name: String): String {
    val parts = name.split(" ").filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    val first = parts.first().firstOrNull()?.toString().orEmpty()
    val last = if (parts.size > 1) parts.last().firstOrNull()?.toString().orEmpty() else ""
    return (first + last).uppercase()
}
