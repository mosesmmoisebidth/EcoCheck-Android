package com.moses.inspectionapp.ui.screens.inspections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.location.LocationCatalog
import com.moses.inspectionapp.data.location.LocationCatalogStore
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppFilterChip
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.InspectionSkeletonList
import com.moses.inspectionapp.ui.components.InspectionSummaryCard
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.formatDateTime
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import kotlinx.coroutines.delay

@Composable
fun InspectionsScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onSelectInspection: () -> Unit = {},
    onNewAssessment: () -> Unit = {},
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val inspections = repository.inspections.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val user = repository.userProfile.collectAsState().value
    val context = LocalContext.current
    val catalogState = produceState<LocationCatalog?>(initialValue = null, key1 = context) {
        value = LocationCatalogStore.load(context)
    }
    val catalog = catalogState.value
    val scrollState = rememberScrollState()
    val filterAll = stringResource(R.string.filter_all)
    val filterPending = stringResource(R.string.filter_pending)
    val filterSynced = stringResource(R.string.filter_synced)
    val filterToday = stringResource(R.string.filter_today)
    val filterWeek = stringResource(R.string.filter_week)
    val (selectedFilter, setSelectedFilter) = remember { mutableStateOf(filterAll) }
    var selectedCell by remember { mutableStateOf<String?>(null) }
    var selectedVillage by remember { mutableStateOf<String?>(null) }
    var showInitialSkeleton by remember { mutableStateOf(true) }

    LaunchedEffect(inspections.size) {
        if (inspections.isNotEmpty()) {
            showInitialSkeleton = false
        }
    }

    LaunchedEffect(Unit) {
        delay(900)
        showInitialSkeleton = false
    }

    val fallbackCells = facilities
        .mapNotNull { it.cell.takeIf { value -> value.isNotBlank() } }
        .distinct()
        .sorted()
    val sectorMatch = catalog?.sectors?.firstOrNull {
        it.sectorName.equals(user.sector, ignoreCase = true)
    }
    val scopedCells = when {
        catalog != null && sectorMatch != null -> catalog.cellsForSector(sectorMatch.sectorId)
        else -> emptyList()
    }
    val cellOptions = scopedCells
        .map { it.cellName }
        .distinct()
        .sorted()
        .ifEmpty { fallbackCells }
    val selectedCellItem = scopedCells.firstOrNull {
        it.cellName.equals(selectedCell, ignoreCase = true)
    }
    val villageOptions = when {
        catalog != null && selectedCellItem != null ->
            catalog.villagesForCell(selectedCellItem.cellId)
                .map { it.villageName }
        else -> facilities
            .filter { facility ->
                selectedCell != null && facility.cell.equals(selectedCell, ignoreCase = true)
            }
            .mapNotNull { it.village.takeIf { value -> value.isNotBlank() } }
    }
        .distinct()
        .sorted()

    LaunchedEffect(cellOptions, selectedCell) {
        if (selectedCell != null &&
            cellOptions.none { it.equals(selectedCell, ignoreCase = true) }
        ) {
            selectedCell = null
        }
    }

    LaunchedEffect(selectedCell, villageOptions) {
        if (selectedVillage != null &&
            villageOptions.none { it.equals(selectedVillage, ignoreCase = true) }
        ) {
            selectedVillage = null
        }
        if (selectedCell == null && selectedVillage != null) {
            selectedVillage = null
        }
    }

    val sortedInspections = inspections.sortedByDescending { it.createdAt }
    val filteredByStatus = when (selectedFilter) {
        filterPending -> sortedInspections.filter { it.syncStatus.name == "PENDING" }
        filterSynced -> sortedInspections.filter { it.syncStatus.name == "SYNCED" }
        else -> sortedInspections
    }
    val facilityLookup = facilities.associateBy { it.id }
    val filtered = filteredByStatus.filter { inspection ->
        matchesInspectionFilters(
            inspection = inspection,
            facility = facilityLookup[inspection.facilityId],
            cell = selectedCell,
            village = selectedVillage,
        )
    }
    val showSkeleton = inspections.isEmpty() && showInitialSkeleton

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.my_inspections), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    top = Dimens.itemGap,
                    bottom = Dimens.sectionGap,
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    filterAll,
                    filterPending,
                    filterSynced,
                    filterToday,
                    filterWeek,
                ).forEach { filter ->
                    AppFilterChip(
                        label = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { setSelectedFilter(filter) },
                        minHeight = 40.dp,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.filter_cell),
                    value = selectedCell ?: stringResource(R.string.all_cells),
                    options = cellOptions,
                    allLabel = stringResource(R.string.all_cells),
                    onSelected = {
                        selectedCell = it
                        if (it == null) {
                            selectedVillage = null
                        }
                    },
                )
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.filter_village),
                    value = selectedVillage ?: stringResource(R.string.all_villages),
                    options = villageOptions,
                    allLabel = stringResource(R.string.all_villages),
                    onSelected = {
                        selectedVillage = it
                    },
                )
            }
            if (showSkeleton) {
                InspectionSkeletonList()
            } else if (filtered.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.no_inspections),
                    message = stringResource(R.string.create_new_assessment),
                    icon = Icons.Rounded.Assignment,
                )
            } else {
                filtered.forEach { inspection ->
                    val decisionColor = decisionAccentColor(inspection.decision)
                    InspectionSummaryCard(
                        facilityName = inspection.facilityName,
                        dateLabel = formatDateTime(inspection.createdAt),
                        totalFine = stringResource(R.string.rwf_amount, inspection.totalFine),
                        decision = decisionLabel(inspection.decision),
                        decisionColor = decisionColor,
                        onClick = {
                            DraftStore.selectedInspectionId.value = inspection.id
                            onSelectInspection()
                        },
                    )
                }
            }
            PrimaryButton(
                text = stringResource(R.string.new_assessment),
                onClick = onNewAssessment,
            )
        }
    }
}

@Composable
private fun FilterDropdown(
    modifier: Modifier,
    label: String,
    value: String,
    options: List<String>,
    allLabel: String,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val menuWidth = with(density) { textFieldSize.width.toDp() }
    val fieldHeight = with(density) {
        if (textFieldSize.height == 0) 56.dp else textFieldSize.height.toDp()
    }
    val menuModifier = if (textFieldSize.width == 0) {
        Modifier
    } else {
        Modifier.width(menuWidth)
    }
    Box(modifier = modifier) {
        StyledTextField(
            value = value,
            onValueChange = {},
            label = label,
            trailingIcon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                )
            },
            readOnly = true,
            enabled = options.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size
                },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .clickable(enabled = options.isNotEmpty()) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = menuModifier,
        ) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    expanded = false
                    onSelected(null)
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}

private fun matchesInspectionFilters(
    inspection: Inspection,
    facility: Facility?,
    cell: String?,
    village: String?,
): Boolean {
    if (cell == null && village == null) {
        return true
    }
    val matchesCell = cell == null || facility?.cell?.equals(cell, ignoreCase = true) == true
    val matchesVillage = village == null || facility?.village?.equals(village, ignoreCase = true) == true
    return matchesCell && matchesVillage
}

private fun decisionAccentColor(decision: Decision): Color {
    return when (decision) {
        Decision.WARNING -> AppColors.StatusWarning
        Decision.CLOSURE_IMMEDIATE -> AppColors.StatusImmediate
        Decision.CLOSURE_DEADLINE -> AppColors.StatusClosure
        Decision.PROSECUTION_RECOMMENDED -> AppColors.AccentGold
        Decision.NO_ACTION -> AppColors.StatusCompliant
    }
}
