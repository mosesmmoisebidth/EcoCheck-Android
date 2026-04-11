package com.moses.inspectionapp.ui.screens.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.ErrorState
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun SyncConflictsScreen(onBack: () -> Unit) {
    val repository = AppContainer.repository
    val inspections = repository.inspections.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val inspectionConflicts = inspections.filter { it.syncStatus == SyncStatus.CONFLICT }
    val facilityConflicts = facilities.filter { it.syncStatus == SyncStatus.CONFLICT }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = stringResource(R.string.sync_conflicts), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium),
        ) {
            if (inspectionConflicts.isEmpty() && facilityConflicts.isEmpty()) {
                ErrorState(
                    title = stringResource(R.string.no_conflicts),
                    message = stringResource(R.string.conflicts_clear),
                )
            } else {
                ErrorState(
                    title = stringResource(R.string.conflict_found),
                    message = stringResource(R.string.conflict_server_kept),
                )
                facilityConflicts.forEach { facility ->
                    Text(text = stringResource(R.string.conflict_facility, facility.name))
                    Text(text = stringResource(R.string.conflict_reason, stringResource(R.string.conflict_reason_tin)))
                    SecondaryButton(
                        text = stringResource(R.string.resolve_conflict),
                        onClick = {
                            scope.launch {
                                repository.resolveConflictForFacility(facility.id)
                            }
                        },
                    )
                }
                inspectionConflicts.forEach { inspection ->
                    Text(text = stringResource(R.string.conflict_facility, inspection.facilityName))
                    Text(text = stringResource(R.string.conflict_reason, stringResource(R.string.conflict_reason_tin)))
                    SecondaryButton(
                        text = stringResource(R.string.resolve_conflict),
                        onClick = {
                            scope.launch {
                                repository.resolveConflictForInspection(inspection.id)
                            }
                        },
                    )
                }
            }
            SecondaryButton(onClick = onBack, text = stringResource(R.string.back))
        }
    }
}
