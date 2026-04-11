package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun AssessmentAdjustmentScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNext: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val faults = repository.faults.collectAsState().value
    val (enabled, setEnabled) = remember { mutableStateOf(draft.adjustmentAmount != 0) }
    val (amount, setAmount) = remember { mutableStateOf(draft.adjustmentAmount.toString()) }
    val (reason, setReason) = remember { mutableStateOf(draft.adjustmentReason) }
    val scrollState = rememberScrollState()
    val amountValue = amount.toIntOrNull()
    val amountValid = !enabled || amountValue != null
    val reasonValid = !enabled || reason.isNotBlank()
    val canContinue = amountValid && reasonValid
    val subtotal = faults.filter { draft.selectedFaultIds.contains(it.id) }.sumOf { it.standardFine }
    val adjustmentAmount = if (enabled) (amountValue ?: 0) else 0
    val totalFine = subtotal + adjustmentAmount
    val steps = assessmentStepLabels()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.manual_adjustment), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            StepProgressBar(
                steps = steps,
                currentStep = 5,
                onStepClick = onStepClick,
            )
            StepHeaderCard(
                title = stringResource(R.string.manual_adjustment),
                subtitle = stringResource(R.string.step_adjust_subtitle),
            )

            Surface(
                color = AppColors.SteelBlueTint,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppColors.BorderLight, RoundedCornerShape(12.dp)),
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                ) {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = AppColors.SteelBlue)
                    Text(
                        text = stringResource(R.string.adjustment_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                    )
                }
            }

            Surface(
                color = AppColors.CardSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.apply_adjustment),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary,
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = { value ->
                            setEnabled(value)
                            if (!value) {
                                setAmount("0")
                                setReason("")
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.SteelBlue,
                            checkedTrackColor = AppColors.SteelBlueLight,
                            uncheckedThumbColor = AppColors.BorderMedium,
                            uncheckedTrackColor = AppColors.BorderLight,
                        ),
                    )
                }
            }

            AnimatedVisibility(visible = enabled) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
                    SectionHeader(title = stringResource(R.string.adjustment_amount_heading))
                    StyledTextField(
                        value = amount,
                        onValueChange = setAmount,
                        label = stringResource(R.string.adjustment_amount),
                        leadingIcon = Icons.Rounded.EditNote,
                        isError = enabled && !amountValid,
                        errorText = if (enabled && !amountValid) stringResource(R.string.amount_invalid) else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("-1000", "-500", "+500", "+1000").forEach { chipValue ->
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = AppColors.SteelBlueTint,
                                modifier = Modifier
                                    .border(1.dp, AppColors.BorderMedium, RoundedCornerShape(50.dp))
                                    .padding(horizontal = 0.dp),
                                onClick = { setAmount(chipValue) },
                            ) {
                                Text(
                                    text = chipValue,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AppColors.SteelBlue,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                    SectionHeader(title = stringResource(R.string.reason_heading))
                    StyledTextField(
                        value = reason,
                        onValueChange = setReason,
                        label = stringResource(R.string.reason),
                        leadingIcon = Icons.Rounded.EditNote,
                        isError = enabled && !reasonValid,
                        errorText = if (enabled && !reasonValid) stringResource(R.string.reason_required) else null,
                        singleLine = false,
                        maxLines = 4,
                    )
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
                    SummaryRow(label = stringResource(R.string.subtotal), value = "${subtotal} RWF")
                    SummaryRow(label = stringResource(R.string.adjustment), value = "${adjustmentAmount} RWF")
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.total_fine),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.TextPrimary,
                        )
                        Text(
                            text = "${totalFine} RWF",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.SteelBlue,
                        )
                    }
                }
            }

            PrimaryButton(
                text = stringResource(R.string.next),
                onClick = {
                    DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                        adjustmentAmount = if (enabled) (amountValue ?: 0) else 0,
                        adjustmentReason = if (enabled) reason.trim() else "",
                    )
                    onNext()
                },
                enabled = canContinue,
            )
            SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = AppColors.TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
    }
}
