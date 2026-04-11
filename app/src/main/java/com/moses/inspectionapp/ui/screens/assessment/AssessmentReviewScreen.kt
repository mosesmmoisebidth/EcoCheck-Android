package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import com.moses.inspectionapp.ui.util.NotificationHelper
import com.moses.inspectionapp.ui.util.visitTypeLabel
import com.moses.inspectionapp.data.sync.SyncManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentReviewScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val faults = repository.faults.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val inspectionTypes = repository.inspectionTypes.collectAsState().value
    val facility = facilities.firstOrNull { it.id == draft.facilityId }
    val inspectionType = inspectionTypes.firstOrNull { it.id == draft.inspectionTypeId }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val canSubmit = draft.facilityId != null && draft.visitType != null && draft.decision != null
    val showConfirm = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val steps = assessmentStepLabels()
    val subtotal = faults.filter { draft.selectedFaultIds.contains(it.id) }.sumOf { it.standardFine }
    val totalFine = subtotal + draft.adjustmentAmount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.review_submit), onBack = onBack)
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
                StepProgressBar(
                    steps = steps,
                    currentStep = 8,
                    onStepClick = onStepClick,
                )
                StepHeaderCard(
                    title = stringResource(R.string.inspection_summary),
                    subtitle = stringResource(R.string.step_review_subtitle),
                )

                SummaryCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Rounded.Storefront, contentDescription = null, tint = AppColors.SteelBlue)
                        Column {
                            Text(text = facility?.name ?: draft.facilityName, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(text = facility?.tin ?: "", style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = AppColors.TextSecondary)
                        Text(
                            text = "${facility?.sector ?: ""}, ${facility?.district ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                        )
                    }
                }

                SummaryCard {
                    Text(text = stringResource(R.string.visit_info), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = stringResource(R.string.visit_type), style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
                            Surface(
                                color = AppColors.SteelBlueTint,
                                shape = RoundedCornerShape(50.dp),
                            ) {
                                Text(
                                    text = draft.visitType?.let { visitTypeLabel(it) } ?: stringResource(R.string.not_set),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AppColors.SteelBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = stringResource(R.string.inspection_date_label), style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
                            Text(
                                text = stringResource(R.string.today_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextPrimary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = stringResource(R.string.inspection_type_label), style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
                    Text(
                        text = inspectionType?.name ?: stringResource(R.string.not_set),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextPrimary,
                    )
                }

                SummaryCard {
                    Text(text = stringResource(R.string.inspection_team), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                    if (draft.teamMembers.isEmpty()) {
                        Text(
                            text = stringResource(R.string.not_set),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            draft.teamMembers.forEach { member ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(AppColors.SteelBlueTint, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = member.firstOrNull()?.uppercase() ?: "",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AppColors.SteelBlue,
                                        )
                                    }
                                    Text(
                                        text = member,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextPrimary,
                                    )
                                }
                            }
                        }
                    }
                }

                SummaryCard {
                    Text(text = stringResource(R.string.faults), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                    if (draft.selectedFaultIds.isEmpty()) {
                        Text(text = stringResource(R.string.no_faults_found), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                    } else {
                        draft.selectedFaultIds.forEach { id ->
                            val fault = faults.firstOrNull { it.id == id }
                            if (fault != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = AppColors.SteelBlue,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = fault.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppColors.TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = AppColors.SteelBlueTint,
                                        shape = RoundedCornerShape(50.dp),
                                    ) {
                                        Text(
                                            text = "${fault.standardFine} RWF",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.SteelBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip,
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.subtotal_label, "${subtotal} RWF"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.TextPrimary,
                        )
                    }
                }

                if (draft.adjustmentAmount != 0) {
                    SummaryCard {
                        Text(text = stringResource(R.string.manual_adjustment), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                        Text(text = "${draft.adjustmentAmount} RWF", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                        if (draft.adjustmentReason.isNotBlank()) {
                            Text(
                                text = draft.adjustmentReason,
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextSecondary,
                            )
                        }
                    }
                }

                SummaryCard {
                    Text(text = stringResource(R.string.decision), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                    DecisionBadge(decision = draft.decision)
                }

                SummaryCard {
                    Text(text = stringResource(R.string.financial_summary), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                    SummaryRow(label = stringResource(R.string.subtotal), value = "${subtotal} RWF")
                    SummaryRow(label = stringResource(R.string.adjustment), value = "${draft.adjustmentAmount} RWF")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.SteelBlueTint, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = stringResource(R.string.total_fine), style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                        Text(
                            text = "${totalFine} RWF",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.SteelBlue,
                        )
                    }
                }

                SummaryCard {
                    Text(text = stringResource(R.string.comments), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                    Text(
                        text = draft.comments.ifBlank { stringResource(R.string.not_set) },
                        color = AppColors.TextPrimary,
                    )
                }

                SummaryCard {
                    Text(text = stringResource(R.string.recommendations), style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
                    Text(
                        text = draft.recommendations.ifBlank { stringResource(R.string.not_set) },
                        color = AppColors.TextPrimary,
                    )
                }
            }
        }

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
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.submit_inspection),
                    leadingIcon = Icons.Rounded.Send,
                    onClick = { showConfirm.value = true },
                    enabled = canSubmit,
                )
                Text(
                    text = stringResource(R.string.submit_offline_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }

    if (showConfirm.value) {
        ModalBottomSheet(
            onDismissRequest = { showConfirm.value = false },
            sheetState = sheetState,
            containerColor = AppColors.CardSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
            ) {
                Text(text = stringResource(R.string.confirm_submission), style = MaterialTheme.typography.headlineMedium, color = AppColors.TextPrimary)
                Text(text = stringResource(R.string.confirm_submission_hint), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                SummaryRow(label = stringResource(R.string.facility), value = facility?.name ?: draft.facilityName)
                SummaryRow(label = stringResource(R.string.total_fine), value = "${totalFine} RWF")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = stringResource(R.string.cancel),
                        onClick = { showConfirm.value = false },
                        fullWidth = false,
                    )
                    PrimaryButton(
                        text = stringResource(R.string.confirm),
                        onClick = {
                            scope.launch {
                                val inspectionId = repository.saveInspection(draft)
                                DraftStore.selectedInspectionId.value = inspectionId
                                DraftStore.resetInspectionDraft()
                                showConfirm.value = false
                                val facilityName = facility?.name ?: draft.facilityName
                                NotificationHelper.notifyInspectionSubmitted(
                                    context,
                                    facilityName,
                                    isOffline,
                                )
                                if (!isOffline) {
                                    SyncManager.enqueue(context)
                                }
                                onSubmit()
                            }
                        },
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
        shadowElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
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
private fun DecisionBadge(decision: Decision?) {
    if (decision == null) {
        Text(text = stringResource(R.string.not_set), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
        return
    }
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
