package com.moses.inspectionapp.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.data.model.VisitType

@Composable
fun syncStatusLabel(status: SyncStatus): String {
    return when (status) {
        SyncStatus.PENDING -> stringResource(R.string.status_pending)
        SyncStatus.SYNCED -> stringResource(R.string.status_synced)
        SyncStatus.CONFLICT -> stringResource(R.string.status_conflict)
    }
}

@Composable
fun visitTypeLabel(type: VisitType): String {
    return when (type) {
        VisitType.FIRST_VISIT -> stringResource(R.string.visit_first)
        VisitType.WARNING_VISIT -> stringResource(R.string.visit_warning)
        VisitType.FOLLOW_UP -> stringResource(R.string.visit_follow_up)
        VisitType.COMPLIANCE_CHECK -> stringResource(R.string.visit_compliance)
    }
}

@Composable
fun decisionLabel(decision: Decision): String {
    return when (decision) {
        Decision.WARNING -> stringResource(R.string.decision_warning)
        Decision.CLOSURE_IMMEDIATE -> stringResource(R.string.decision_closure_immediate)
        Decision.CLOSURE_DEADLINE -> stringResource(R.string.decision_closure_deadline)
        Decision.PROSECUTION_RECOMMENDED -> stringResource(R.string.decision_prosecution)
        Decision.NO_ACTION -> stringResource(R.string.decision_no_action)
    }
}

@Composable
fun assessmentStepLabels(): List<String> {
    return listOf(
        stringResource(R.string.step_facility),
        stringResource(R.string.step_visit),
        stringResource(R.string.step_team),
        stringResource(R.string.step_faults),
        stringResource(R.string.step_adjust),
        stringResource(R.string.step_decision),
        stringResource(R.string.step_notes),
        stringResource(R.string.step_review),
    )
}
