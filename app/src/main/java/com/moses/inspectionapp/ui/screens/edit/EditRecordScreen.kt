package com.moses.inspectionapp.ui.screens.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.sync.SyncManager
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.FacilityDraft
import com.moses.inspectionapp.data.model.InspectionDraft
import com.moses.inspectionapp.data.model.decisionOptionsInUiOrder
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.data.validator.InputValidators
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.CountryPhoneField
import com.moses.inspectionapp.ui.components.ErrorState
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.formatDateTime
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import kotlinx.coroutines.launch

@Composable
fun EditRecordScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val facilities = repository.facilities.collectAsState().value
    val inspections = repository.inspections.collectAsState().value
    val faults = repository.faults.collectAsState().value
    val isOffline = repository.isOffline.collectAsState().value
    val selectedFacilityId = DraftStore.selectedFacilityId.collectAsState().value
    val selectedInspectionId = DraftStore.selectedInspectionId.collectAsState().value
    val facility = facilities.firstOrNull { it.id == selectedFacilityId }
    val inspection = inspections.firstOrNull { it.id == selectedInspectionId }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val now = System.currentTimeMillis()
    val createdAt = inspection?.createdAt ?: facility?.createdAt ?: now
    val editableUntil = createdAt + 24 * 60 * 60 * 1000
    val canEdit = now <= editableUntil

    val (name, setName) = remember { mutableStateOf(facility?.name.orEmpty()) }
    val (ownerPhone, setOwnerPhone) = remember { mutableStateOf(facility?.ownerPhone.orEmpty()) }
    val (comments, setComments) = remember { mutableStateOf(inspection?.comments.orEmpty()) }
    val (recommendations, setRecommendations) = remember { mutableStateOf(inspection?.recommendations.orEmpty()) }
    val (selectedFaultIds, setSelectedFaultIds) = remember { mutableStateOf(setOf<String>()) }
    val (decision, setDecision) = remember { mutableStateOf(inspection?.decision ?: Decision.NO_ACTION) }
    val (adjustmentEnabled, setAdjustmentEnabled) = remember { mutableStateOf(inspection?.adjustmentAmount != 0) }
    val (adjustmentAmount, setAdjustmentAmount) = remember { mutableStateOf(inspection?.adjustmentAmount?.toString() ?: "0") }
    val (adjustmentReason, setAdjustmentReason) = remember { mutableStateOf(inspection?.adjustmentReason.orEmpty()) }
    var formError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val ownerPhoneE164 = InputValidators.toE164(ownerPhone, "RW")
    val ownerPhoneValid = ownerPhone.isBlank() || InputValidators.isValidInternationalPhone(ownerPhoneE164)

    LaunchedEffect(inspection?.id) {
        if (inspection != null) {
            val ids = repository.getInspectionFaults(inspection.id).map { it.id }.toSet()
            setSelectedFaultIds(ids)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = stringResource(R.string.edit_record), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium),
        ) {
            Text(text = stringResource(R.string.editable_until, formatDateTime(editableUntil)))
            if (!canEdit) {
                ErrorState(
                    title = stringResource(R.string.record_locked),
                    message = stringResource(R.string.locked_message),
                )
            }
            if (facility != null) {
                SectionHeader(title = stringResource(R.string.facility))
                OutlinedTextField(
                    value = name,
                    onValueChange = setName,
                    label = { Text(stringResource(R.string.facility_name)) },
                    enabled = canEdit,
                )
                CountryPhoneField(
                    value = ownerPhone,
                    onValueChange = setOwnerPhone,
                    label = stringResource(R.string.owner_phone),
                    isError = ownerPhone.isNotBlank() && !ownerPhoneValid,
                    errorText = if (ownerPhone.isNotBlank() && !ownerPhoneValid) {
                        stringResource(R.string.phone_invalid)
                    } else {
                        null
                    },
                    enabled = canEdit,
                    defaultCountryIso = "RW",
                )
            }
            if (inspection != null) {
                SectionHeader(title = stringResource(R.string.inspection))
                Text(text = stringResource(R.string.decision_label, decisionLabel(decision)))
                SectionHeader(title = stringResource(R.string.faults))
                faults.forEach { fault ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedFaultIds.contains(fault.id),
                            onCheckedChange = { checked ->
                                setSelectedFaultIds(
                                    if (checked) {
                                        selectedFaultIds + fault.id
                                    } else {
                                        selectedFaultIds - fault.id
                                    },
                                )
                            },
                            enabled = canEdit,
                        )
                        Text(text = "${fault.name} • ${fault.standardFine} RWF")
                    }
                }
                SectionHeader(title = stringResource(R.string.decision))
                decisionOptionsInUiOrder().forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = decision == option,
                            onClick = { setDecision(option) },
                            enabled = canEdit,
                        )
                        Text(text = decisionLabel(option))
                    }
                }
                SectionHeader(title = stringResource(R.string.manual_adjustment))
                Switch(
                    checked = adjustmentEnabled,
                    onCheckedChange = { value ->
                        setAdjustmentEnabled(value)
                        if (!value) {
                            setAdjustmentAmount("0")
                            setAdjustmentReason("")
                        }
                    },
                    enabled = canEdit,
                )
                OutlinedTextField(
                    value = adjustmentAmount,
                    onValueChange = setAdjustmentAmount,
                    label = { Text(stringResource(R.string.adjustment_amount)) },
                    enabled = canEdit && adjustmentEnabled,
                )
                OutlinedTextField(
                    value = adjustmentReason,
                    onValueChange = setAdjustmentReason,
                    label = { Text(stringResource(R.string.reason)) },
                    enabled = canEdit && adjustmentEnabled,
                )
                OutlinedTextField(
                    value = comments,
                    onValueChange = setComments,
                    label = { Text(stringResource(R.string.comments)) },
                    enabled = canEdit,
                    minLines = 3,
                )
                OutlinedTextField(
                    value = recommendations,
                    onValueChange = setRecommendations,
                    label = { Text(stringResource(R.string.recommendations)) },
                    enabled = canEdit,
                    minLines = 3,
                )
            }
            PrimaryButton(
                text = stringResource(R.string.save_changes),
                onClick = {
                    scope.launch {
                        formError = null
                        if (!ownerPhoneValid) {
                            formError = context.getString(R.string.phone_invalid)
                            return@launch
                        }
                        if (facility != null) {
                            repository.updateFacility(
                                facility.id,
                                FacilityDraft(
                                    name = name.trim(),
                                    tin = facility.tin,
                                    ownerName = facility.ownerName,
                                    ownerPhone = ownerPhoneE164,
                                    ownerEmail = facility.ownerEmail,
                                    district = facility.district,
                                    sector = facility.sector,
                                    cell = facility.cell,
                                    village = facility.village,
                                    latitude = facility.latitude,
                                    longitude = facility.longitude,
                                    photoPath = facility.photoPath,
                                ),
                            )
                        }
                        if (inspection != null) {
                            val faultIds = selectedFaultIds
                            repository.updateInspection(
                                inspection.id,
                                InspectionDraft(
                                    id = inspection.id,
                                    facilityId = inspection.facilityId,
                                    facilityName = inspection.facilityName,
                                    visitType = inspection.visitType,
                                    inspectionTypeId = inspection.inspectionTypeId,
                                    teamMembers = inspection.teamMembers,
                                    selectedFaultIds = faultIds,
                                    adjustmentAmount = adjustmentAmount.toIntOrNull() ?: 0,
                                    adjustmentReason = adjustmentReason.trim(),
                                    decision = decision,
                                    comments = comments.trim(),
                                    recommendations = recommendations.trim(),
                                    createdAt = inspection.createdAt,
                                    createdBy = inspection.createdBy,
                                ),
                            )
                        }
                        if (!isOffline) {
                            SyncManager.enqueue(context)
                        }
                        onDone()
                    }
                },
                enabled = canEdit,
            )
            if (!formError.isNullOrBlank()) {
                Text(
                    text = formError.orEmpty(),
                    color = com.moses.inspectionapp.ui.theme.AppColors.AccentRed,
                )
            }
        }
    }
}


