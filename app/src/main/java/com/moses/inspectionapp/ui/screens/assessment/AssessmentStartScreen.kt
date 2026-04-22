package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppFilterChip
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.ClickableCard
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.mouseWheelScroll

@Composable
fun AssessmentStartScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onContinue: () -> Unit,
    onEnrollNew: () -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val facilities = repository.facilities.collectAsState().value
    val inspectionTypes = repository.inspectionTypes.collectAsState().value
    val selectedFacilityId = DraftStore.selectedFacilityId.collectAsState().value
    val selectedFacility = facilities.firstOrNull { it.id == selectedFacilityId }
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val activeTypes = remember(inspectionTypes) { dedupeInspectionTypes(inspectionTypes) }
    val (query, setQuery) = remember { mutableStateOf("") }
    val (searchByTin, setSearchByTin) = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val steps = assessmentStepLabels()
    val normalizedQuery = query.trim()
    val filtered = facilities
        .sortedByDescending { it.createdAt }
        .filter { facility ->
            if (normalizedQuery.isBlank()) {
                true
            } else if (searchByTin) {
                facility.tin.contains(normalizedQuery, ignoreCase = true)
            } else {
                facility.name.contains(normalizedQuery, ignoreCase = true) ||
                    facility.tin.contains(normalizedQuery, ignoreCase = true)
            }
        }
    val canContinue = selectedFacility != null && draft.inspectionTypeId != null

    LaunchedEffect(activeTypes, draft.inspectionTypeId) {
        if (activeTypes.isEmpty()) return@LaunchedEffect
        val currentId = draft.inspectionTypeId
        val activeIds = activeTypes.map { it.id }.toSet()
        if (currentId == null) {
            DraftStore.inspectionDraft.value = draft.copy(inspectionTypeId = activeTypes.first().id)
            return@LaunchedEffect
        }
        if (!activeIds.contains(currentId)) {
            val normalized = normalizeInspectionTypeCode(currentId)
            val matched = activeTypes.firstOrNull {
                normalizeInspectionTypeCode(it.code) == normalized
            }
            if (matched != null && matched.id != currentId) {
                DraftStore.inspectionDraft.value = draft.copy(inspectionTypeId = matched.id)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.new_assessment_title), onBack = onBack)
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
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            ) {
                StepProgressBar(
                    steps = steps,
                    currentStep = 1,
                    onStepClick = onStepClick,
                )
                StepHeaderCard(
                    title = stringResource(R.string.select_facility),
                    subtitle = stringResource(R.string.step_facility_subtitle),
                )
                Text(
                    text = stringResource(R.string.select_inspection_type),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextSecondary,
                )
                if (activeTypes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_inspection_types),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        activeTypes.forEach { type ->
                            AppFilterChip(
                                label = type.name,
                                isSelected = type.id == draft.inspectionTypeId,
                                onClick = {
                                    DraftStore.inspectionDraft.value = draft.copy(inspectionTypeId = type.id)
                                },
                            )
                        }
                    }
                }
                StyledTextField(
                    value = query,
                    onValueChange = setQuery,
                    label = stringResource(R.string.search_name_tin),
                    leadingIcon = Icons.Rounded.Search,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppFilterChip(
                        label = stringResource(R.string.search_by_tin),
                        isSelected = searchByTin,
                        onClick = { setSearchByTin(!searchByTin) },
                    )
                    SecondaryButton(
                        text = stringResource(R.string.open_facility_search),
                        onClick = onSearch,
                        fullWidth = false,
                    )
                }
                if (filtered.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.no_facilities),
                        message = stringResource(R.string.search_or_enroll),
                        icon = Icons.Rounded.Storefront,
                    )
                } else {
                    filtered.forEach { facility ->
                        FacilitySelectCard(
                            name = facility.name,
                            tin = facility.tin,
                            location = "${facility.sector}, ${facility.district}",
                            selected = facility.id == selectedFacilityId,
                            onClick = {
                                DraftStore.selectedFacilityId.value = facility.id
                                DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                    facilityId = facility.id,
                                    facilityName = facility.name,
                                )
                            },
                        )
                    }
                }
                PrimaryButton(
                    text = stringResource(R.string.continue_label),
                    onClick = onContinue,
                    enabled = canContinue,
                )
                SecondaryButton(
                    text = stringResource(R.string.enroll_new_facility),
                    onClick = onEnrollNew,
                    leadingIcon = Icons.Rounded.Add,
                )
            }
        }
    }
}

private fun dedupeInspectionTypes(types: List<com.moses.inspectionapp.data.model.InspectionType>):
    List<com.moses.inspectionapp.data.model.InspectionType> {
    return types
        .filter { it.active }
        .groupBy { normalizeInspectionTypeCode(it.code) }
        .mapNotNull { (_, grouped) ->
            grouped.maxByOrNull { if (isUuid(it.id)) 1 else 0 }
        }
        .sortedBy { it.name }
}

private fun isUuid(value: String): Boolean {
    return Regex(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
    ).matches(value)
}

private fun normalizeInspectionTypeCode(value: String): String {
    return value.trim().uppercase().replace(Regex("[\\s-]+"), "_")
}

@Composable
private fun FacilitySelectCard(
    name: String,
    tin: String,
    location: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ClickableCard(onClick = onClick, isSelected = selected) {
        Row(
            modifier = Modifier.padding(Dimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AppColors.SteelBlueTint, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Rounded.Storefront, contentDescription = null, tint = AppColors.SteelBlue)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                Text(text = "TIN: $tin", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                Text(text = location, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
            }
            Icon(
                imageVector = if (selected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) AppColors.SteelBlue else AppColors.TextSecondary,
            )
        }
    }
}
