package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.decisionOptionsInUiOrder
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.SelectionCard
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.mouseWheelScroll

@Composable
fun AssessmentDecisionScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNext: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val options = decisionOptionsInUiOrder()
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val (selected, setSelected) = remember { mutableStateOf(draft.decision ?: options.first()) }
    val scrollState = rememberScrollState()
    val steps = assessmentStepLabels()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.decision), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.screenPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = Dimens.cardMaxWidth)
                    .mouseWheelScroll(scrollState)
                    .verticalScroll(scrollState)
                    .padding(vertical = Dimens.sectionGap),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
            ) {
                StepProgressBar(
                    steps = steps,
                    currentStep = 6,
                    onStepClick = onStepClick,
                )
                StepHeaderCard(
                    title = stringResource(R.string.decision),
                    subtitle = stringResource(R.string.step_decision_subtitle),
                )
                options.forEach { option ->
                    val isSelected = selected == option
                    val config = decisionConfig(option)
                    SelectionCard(
                        title = decisionLabel(option),
                        description = config.description,
                        icon = config.icon,
                        isSelected = isSelected,
                        accentColor = config.accent,
                        accentBgColor = config.accentBg,
                        onClick = {
                            setSelected(option)
                            DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(decision = option)
                        },
                    )
                }
                PrimaryButton(
                    text = stringResource(R.string.next),
                    onClick = {
                        DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(decision = selected)
                        onNext()
                    },
                )
                SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            }
        }
    }
}

private data class DecisionUiConfig(
    val accent: androidx.compose.ui.graphics.Color,
    val accentBg: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
)

@Composable
private fun decisionConfig(decision: Decision): DecisionUiConfig {
    return when (decision) {
        Decision.WARNING -> DecisionUiConfig(
            accent = AppColors.StatusWarning,
            accentBg = AppColors.StatusWarningBg,
            icon = Icons.Rounded.Warning,
            description = stringResource(R.string.decision_warning_desc),
        )
        Decision.CLOSURE_IMMEDIATE -> DecisionUiConfig(
            accent = AppColors.StatusImmediate,
            accentBg = AppColors.StatusImmediateBg,
            icon = Icons.Rounded.Block,
            description = stringResource(R.string.decision_closure_immediate_desc),
        )
        Decision.CLOSURE_DEADLINE -> DecisionUiConfig(
            accent = AppColors.StatusClosure,
            accentBg = AppColors.StatusClosureBg,
            icon = Icons.Rounded.Schedule,
            description = stringResource(R.string.decision_closure_deadline_desc),
        )
        Decision.PROSECUTION_RECOMMENDED -> DecisionUiConfig(
            accent = AppColors.StatusProsecution,
            accentBg = AppColors.StatusProsecutionBg,
            icon = Icons.Rounded.Payments,
            description = stringResource(R.string.decision_prosecution_desc),
        )
        Decision.NO_ACTION -> DecisionUiConfig(
            accent = AppColors.StatusCompliant,
            accentBg = AppColors.StatusCompliantBg,
            icon = Icons.Rounded.CheckCircle,
            description = stringResource(R.string.decision_no_action_desc),
        )
    }
}
