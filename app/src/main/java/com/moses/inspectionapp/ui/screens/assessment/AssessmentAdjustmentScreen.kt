package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.LabelValueRow
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.SectionLabel
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.components.StaggeredAnimatedItem
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.CardShape
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels

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
    val amountValue = amount.toIntOrNull()
    val amountValid = !enabled || amountValue != null
    val reasonValid = !enabled || reason.isNotBlank()
    val canContinue = amountValid && reasonValid
    val subtotal = faults.filter { draft.selectedFaultIds.contains(it.id) }.sumOf { it.standardFine }
    val adjustmentAmount = if (enabled) (amountValue ?: 0) else 0
    val totalFineTarget = subtotal + adjustmentAmount
    val totalFine by animateIntAsState(targetValue = totalFineTarget, label = "totalFine")
    val steps = assessmentStepLabels()
    val toggleTrack by animateColorAsState(
        targetValue = if (enabled) AppColors.SteelBlue else AppColors.BorderMedium,
        label = "toggleTrack",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.manual_adjustment), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            item {
                StepProgressBar(
                    steps = steps,
                    currentStep = 5,
                    onStepClick = onStepClick,
                )
            }

            item {
                StaggeredAnimatedItem(index = 0) {
                    Surface(
                        color = AppColors.SteelBlueTint,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, AppColors.BorderLight),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.cardPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                        ) {
                            Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = AppColors.SteelBlue)
                            Text(
                                text = "Manual adjustments modify the total fine. A reason is mandatory when applying an adjustment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary,
                            )
                        }
                    }
                }
            }

            item {
                StaggeredAnimatedItem(index = 1) {
                    Surface(
                        color = AppColors.CardSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.5.dp, AppColors.BorderLight),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.cardPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = "Apply Manual Adjustment",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.TextPrimary,
                                )
                                Text(
                                    text = "Toggle to add a fine adjustment",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextSecondary,
                                )
                            }
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
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = toggleTrack,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = AppColors.BorderLight,
                                ),
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = enabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
                        Surface(
                            color = AppColors.CardSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.5.dp, AppColors.BorderLight),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimens.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SectionLabel(text = "Adjustment Amount")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        color = AppColors.PageBackground,
                                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                                        border = BorderStroke(1.dp, AppColors.BorderLight),
                                    ) {
                                        Text(
                                            text = "RWF",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppColors.TextSecondary,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                        )
                                    }
                                    OutlinedTextField(
                                        value = amount,
                                        onValueChange = setAmount,
                                        isError = enabled && !amountValid,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                        ),
                                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.SteelBlue,
                                            unfocusedBorderColor = AppColors.BorderLight,
                                            errorBorderColor = AppColors.AccentRed,
                                            focusedContainerColor = AppColors.CardSurface,
                                            unfocusedContainerColor = AppColors.CardSurface,
                                            cursorColor = AppColors.SteelBlue,
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("-5000", "-1000", "+1000", "+5000").forEach { chipValue ->
                                        val selected = amount == chipValue
                                        val chipBg = if (selected) AppColors.SteelBlue else Color.Transparent
                                        val chipText = if (selected) Color.White else AppColors.SteelBlue
                                        val chipBorder = if (selected) AppColors.SteelBlue else AppColors.SteelBlue
                                        Surface(
                                            color = chipBg,
                                            shape = RoundedCornerShape(50.dp),
                                            border = BorderStroke(1.dp, chipBorder),
                                            modifier = Modifier.clickable { setAmount(chipValue) },
                                        ) {
                                            Text(
                                                text = chipValue,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = chipText,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Surface(
                            color = AppColors.CardSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.5.dp, AppColors.BorderLight),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimens.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SectionLabel(text = "Reason for Adjustment")
                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = setReason,
                                    minLines = 3,
                                    maxLines = 5,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.EditNote,
                                            contentDescription = null,
                                            tint = AppColors.SteelBlue,
                                        )
                                    },
                                    isError = enabled && !reasonValid,
                                    placeholder = {
                                        Text(
                                            text = "Explain why this adjustment is being made",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppColors.TextHint,
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.SteelBlue,
                                        unfocusedBorderColor = AppColors.BorderLight,
                                        errorBorderColor = AppColors.AccentRed,
                                        focusedContainerColor = AppColors.CardSurface,
                                        unfocusedContainerColor = AppColors.CardSurface,
                                        cursorColor = AppColors.SteelBlue,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                if (enabled && !reasonValid) {
                                    Text(
                                        text = stringResource(R.string.reason_required),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppColors.AccentRed,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = AppColors.CardSurface,
                    shape = CardShape,
                    border = BorderStroke(0.5.dp, AppColors.BorderLight),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Fine Breakdown",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LabelValueRow(label = stringResource(R.string.subtotal), value = "${subtotal} RWF")
                        DividerLine()
                        val adjustmentColor = when {
                            adjustmentAmount > 0 -> AppColors.AccentGreen
                            adjustmentAmount < 0 -> AppColors.AccentRed
                            else -> AppColors.TextPrimary
                        }
                        LabelValueRow(
                            label = stringResource(R.string.adjustment),
                            value = "${adjustmentAmount} RWF",
                            valueColor = adjustmentColor,
                        )
                        DividerLine(stronger = true)
                        Surface(
                            color = AppColors.SteelBlueTint,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.total_fine),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = AppColors.TextPrimary,
                                )
                                Text(
                                    text = "${totalFine} RWF",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AppColors.SteelBlue,
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.smallGap),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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
private fun DividerLine(stronger: Boolean = false) {
    val color = if (stronger) AppColors.BorderMedium else AppColors.BorderLight
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}
