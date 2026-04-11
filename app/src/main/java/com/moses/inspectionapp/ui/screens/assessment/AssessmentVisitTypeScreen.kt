package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.model.VisitType
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
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import com.moses.inspectionapp.ui.util.visitTypeLabel

@Composable
fun AssessmentVisitTypeScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNext: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val options = VisitType.values().toList()
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val (selected, setSelected) = remember { mutableStateOf(draft.visitType ?: options.first()) }
    val scrollState = rememberScrollState()
    val steps = assessmentStepLabels()

    Column(modifier = Modifier.fillMaxSize().background(AppColors.PageBackground)) {
        AppTopBar(title = stringResource(R.string.visit_type), onBack = onBack)
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
                currentStep = 2,
                onStepClick = onStepClick,
            )
            StepHeaderCard(
                title = stringResource(R.string.visit_type),
                subtitle = stringResource(R.string.step_visit_subtitle),
            )
            options.forEach { option ->
                val isSelected = selected == option
                val icon = when (option) {
                    VisitType.FIRST_VISIT -> Icons.Rounded.Flag
                    VisitType.WARNING_VISIT -> Icons.Rounded.Warning
                    VisitType.FOLLOW_UP -> Icons.Rounded.Update
                    VisitType.COMPLIANCE_CHECK -> Icons.Rounded.Verified
                }
                val description = when (option) {
                    VisitType.FIRST_VISIT -> stringResource(R.string.visit_first_desc)
                    VisitType.WARNING_VISIT -> stringResource(R.string.visit_warning_desc)
                    VisitType.FOLLOW_UP -> stringResource(R.string.visit_follow_up_desc)
                    VisitType.COMPLIANCE_CHECK -> stringResource(R.string.visit_compliance_desc)
                }
                SelectionCard(
                    title = visitTypeLabel(option),
                    description = description,
                    icon = icon,
                    isSelected = isSelected,
                    onClick = {
                        setSelected(option)
                        DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(visitType = option)
                    },
                )
            }
            PrimaryButton(
                text = stringResource(R.string.next),
                onClick = {
                    DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                        visitType = selected,
                    )
                    onNext()
                },
            )
            SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
        }
    }
}
