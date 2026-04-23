package com.moses.inspectionapp.ui.screens.facilities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import android.util.Log
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.location.LocationCatalog
import com.moses.inspectionapp.data.location.LocationCatalogStore
import com.moses.inspectionapp.data.model.Facility
import com.moses.inspectionapp.data.model.UserRoleType
import com.moses.inspectionapp.data.model.parseUserRole
import com.moses.inspectionapp.data.repository.RoomInspectionRepository
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.FacilitySkeletonList
import com.moses.inspectionapp.ui.components.FacilitySummaryCard
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun FacilitiesScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onSelectFacility: () -> Unit,
    onEnrollNew: () -> Unit,
) {
    val repository = AppContainer.repository
    val facilities = repository.facilities.collectAsState().value
    val user = repository.userProfile.collectAsState().value
    val roleType = parseUserRole(user.role)
    val context = LocalContext.current
    val catalogState = produceState<LocationCatalog?>(initialValue = null, key1 = context) {
        value = LocationCatalogStore.load(context)
    }
    val catalog = catalogState.value
    val scrollState = rememberScrollState()
    var query by remember { mutableStateOf("") }
    var selectedCell by remember { mutableStateOf<String?>(null) }
    var selectedVillage by remember { mutableStateOf<String?>(null) }
    var didAttemptServerRefresh by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(facilities.size, isOffline) {
        if (!isOffline && facilities.isEmpty() && !didAttemptServerRefresh) {
            didAttemptServerRefresh = true
            isRefreshing = true
            try {
                val roomRepository = repository as? RoomInspectionRepository
                if (roomRepository == null) {
                    Log.w("FacilitiesScreen", "Room repository unavailable")
                } else {
                    withContext(Dispatchers.IO) {
                        val count = roomRepository.refreshFacilitiesFromServer()
                        Log.i("FacilitiesScreen", "Facilities refreshed: $count")
                    }
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    val roleScopedFacilities = facilities.filter { facility ->
        when (roleType) {
            UserRoleType.HSO -> {
                facility.createdBy == user.id &&
                    sameName(facility.district, user.district) &&
                    sameName(facility.sector, user.sector)
            }
            UserRoleType.DISTRICT_MANAGER -> sameName(facility.district, user.district)
            UserRoleType.CITY_MANAGER -> isKigaliDistrict(facility.district)
            UserRoleType.OTHER -> true
        }
    }

    val normalizedQuery = query.trim()
    val fallbackCells = roleScopedFacilities
        .mapNotNull { it.cell.takeIf { value -> value.isNotBlank() } }
        .distinct()
        .sorted()
    val scopedCells = when (roleType) {
        UserRoleType.HSO -> {
            val districtId = resolveDistrictId(catalog, user.district)
            val sectorMatch = if (districtId != null) {
                catalog?.sectorsForDistrict(districtId)
                    ?.firstOrNull { sector -> sameName(sector.sectorName, user.sector) }
            } else {
                catalog?.sectors?.firstOrNull { sector -> sameName(sector.sectorName, user.sector) }
            }
            if (sectorMatch == null) emptyList()
            else catalog?.cellsForSector(sectorMatch.sectorId).orEmpty()
        }
        UserRoleType.DISTRICT_MANAGER -> {
            val districtId = resolveDistrictId(catalog, user.district)
            if (districtId == null) {
                emptyList()
            } else {
                catalog?.sectorsForDistrict(districtId)
                    .orEmpty()
                    .flatMap { sector -> catalog?.cellsForSector(sector.sectorId).orEmpty() }
            }
        }
        UserRoleType.CITY_MANAGER -> {
            val kigaliDistrictIds = catalog?.districts
                ?.filter { district -> isKigaliDistrict(district.districtName) }
                ?.map { district -> district.districtId }
                ?.toSet()
                .orEmpty()
            if (kigaliDistrictIds.isEmpty()) {
                emptyList()
            } else {
                catalog?.sectors
                    .orEmpty()
                    .filter { sector -> sector.districtId in kigaliDistrictIds }
                    .flatMap { sector -> catalog?.cellsForSector(sector.sectorId).orEmpty() }
            }
        }
        UserRoleType.OTHER -> catalog?.cells.orEmpty()
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
        else -> roleScopedFacilities
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

    val filtered = roleScopedFacilities
        .sortedByDescending { it.createdAt }
        .filter { facility ->
            matchesFilters(
                facility = facility,
                query = normalizedQuery,
                cell = selectedCell,
                village = selectedVillage,
            )
        }
    val showSkeleton = facilities.isEmpty() && (isRefreshing || !didAttemptServerRefresh)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.facilities_label))
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.screenPadding),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter,
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
                SectionHeader(title = stringResource(R.string.filters))
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
                    StyledTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = stringResource(R.string.search_name_tin),
                        leadingIcon = Icons.Rounded.Search,
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val compactStack = maxWidth <= 360.dp
                        if (compactStack) {
                            Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
                                FilterDropdown(
                                    modifier = Modifier.fillMaxWidth(),
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
                                    modifier = Modifier.fillMaxWidth(),
                                    label = stringResource(R.string.filter_village),
                                    value = selectedVillage ?: stringResource(R.string.all_villages),
                                    options = villageOptions,
                                    allLabel = stringResource(R.string.all_villages),
                                    onSelected = {
                                        selectedVillage = it
                                    },
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
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
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.facility_results_count, filtered.size),
                    color = AppColors.TextSecondary,
                )

                if (showSkeleton) {
                    FacilitySkeletonList()
                } else if (filtered.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.no_facilities),
                        message = stringResource(R.string.try_another_or_enroll),
                        icon = Icons.Rounded.Search,
                    )
                } else {
                    filtered.forEach { facility ->
                        FacilitySummaryCard(
                            name = facility.name,
                            tin = facility.tin,
                            location = "${facility.cell}, ${facility.village}",
                            onClick = {
                                DraftStore.selectedFacilityId.value = facility.id
                                DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                    facilityId = facility.id,
                                    facilityName = facility.name,
                                )
                                onSelectFacility()
                            },
                        )
                    }
                }

                PrimaryButton(
                    text = stringResource(R.string.enroll_new_facility),
                    onClick = onEnrollNew,
                )
            }
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

private fun matchesFilters(
    facility: Facility,
    query: String,
    cell: String?,
    village: String?,
): Boolean {
    val matchesQuery = query.isBlank() ||
        facility.name.contains(query, ignoreCase = true) ||
        facility.tin.contains(query, ignoreCase = true)
    val matchesCell = cell == null || facility.cell.equals(cell, ignoreCase = true)
    val matchesVillage = village == null || facility.village.equals(village, ignoreCase = true)
    return matchesQuery && matchesCell && matchesVillage
}

private fun resolveDistrictId(catalog: LocationCatalog?, districtName: String?): Int? {
    val normalizedTarget = normalizeLocationName(districtName)
    if (catalog == null || normalizedTarget.isBlank()) return null

    val directMatch = catalog.districts.firstOrNull { district ->
        sameName(district.districtName, districtName)
    }
    if (directMatch != null) return directMatch.districtId

    val numericId = districtName?.trim()?.toIntOrNull()
    if (numericId != null && catalog.districts.any { it.districtId == numericId }) {
        return numericId
    }

    val keywordMatch = when {
        normalizedTarget.contains("gasabo") -> "gasabo"
        normalizedTarget.contains("kicukiro") -> "kicukiro"
        normalizedTarget.contains("nyarugenge") -> "nyarugenge"
        else -> null
    }
    if (keywordMatch != null) {
        return catalog.districts
            .firstOrNull { normalizeLocationName(it.districtName) == keywordMatch }
            ?.districtId
    }

    return catalog.districts.firstOrNull { district ->
        val districtNormalized = normalizeLocationName(district.districtName)
        districtNormalized.contains(normalizedTarget) ||
            normalizedTarget.contains(districtNormalized)
    }?.districtId
}

private fun sameName(left: String?, right: String?): Boolean {
    val rawLeft = left.orEmpty().trim()
    val rawRight = right.orEmpty().trim()
    if (rawLeft.equals(rawRight, ignoreCase = true)) return true
    return normalizeLocationName(rawLeft) == normalizeLocationName(rawRight)
}

private fun normalizeLocationName(value: String?): String {
    return value
        .orEmpty()
        .trim()
        .lowercase(Locale.getDefault())
        .replace("district", "")
        .replace("sector", "")
        .replace("cell", "")
        .replace("village", "")
        .replace(Regex("[^a-z0-9]"), "")
}

private fun isKigaliDistrict(name: String?): Boolean {
    return when (normalizeLocationName(name)) {
        "gasabo", "kicukiro", "nyarugenge" -> true
        else -> false
    }
}
