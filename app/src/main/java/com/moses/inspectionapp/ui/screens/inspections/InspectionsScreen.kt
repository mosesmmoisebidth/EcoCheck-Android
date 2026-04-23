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
import com.moses.inspectionapp.data.model.UserRoleType
import com.moses.inspectionapp.data.model.parseUserRole
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
    val roleType = parseUserRole(user.role)
    val reportPrefill = DraftStore.reportFiltersPrefill.collectAsState().value
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    var selectedSector by remember { mutableStateOf<String?>(null) }
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

    val roleScopedFacilities = facilities.filter { facility ->
        when (roleType) {
            UserRoleType.CITY_MANAGER -> isKigaliDistrict(facility.district)
            UserRoleType.DISTRICT_MANAGER -> sameName(facility.district, user.district)
            UserRoleType.HSO -> {
                facility.createdBy == user.id &&
                    sameName(facility.district, user.district) &&
                    sameName(facility.sector, user.sector)
            }
            UserRoleType.OTHER -> true
        }
    }

    val districtOptions = if (roleType == UserRoleType.CITY_MANAGER) {
        val fromCatalog = catalog
            ?.districtsSorted()
            ?.map { it.districtName }
            .orEmpty()
        (fromCatalog + roleScopedFacilities.map { it.district })
            .filter { it.isNotBlank() }
            .distinctBy { normalizeLocationName(it) }
            .sortedBy { normalizeLocationName(it) }
    } else {
        emptyList()
    }
    val selectedDistrictValue = selectedDistrict?.let { requested ->
        districtOptions.firstOrNull { sameName(it, requested) }
    }
    LaunchedEffect(districtOptions, selectedDistrict) {
        if (selectedDistrict != null && selectedDistrictValue == null) {
            selectedDistrict = null
        }
    }
    val districtScopedFacilities = if (roleType == UserRoleType.CITY_MANAGER && selectedDistrictValue != null) {
        roleScopedFacilities.filter { facility -> sameName(facility.district, selectedDistrictValue) }
    } else {
        roleScopedFacilities
    }

    val districtIdForScope = when (roleType) {
        UserRoleType.CITY_MANAGER -> resolveDistrictId(catalog, selectedDistrictValue)
        UserRoleType.DISTRICT_MANAGER,
        UserRoleType.HSO,
        UserRoleType.OTHER,
        -> resolveDistrictId(catalog, user.district)
    }
    val fallbackSectorOptions = districtScopedFacilities.map { it.sector }.filter { it.isNotBlank() }
    val catalogSectorOptions = catalog?.let { locationCatalog ->
        val sectorsInScope = when (roleType) {
            UserRoleType.CITY_MANAGER -> {
                if (districtIdForScope == null) locationCatalog.sectors
                else locationCatalog.sectors.filter { it.districtId == districtIdForScope }
            }
            UserRoleType.DISTRICT_MANAGER,
            UserRoleType.HSO,
            UserRoleType.OTHER,
            -> {
                if (districtIdForScope == null) emptyList()
                else locationCatalog.sectors.filter { it.districtId == districtIdForScope }
            }
        }
        sectorsInScope.map { it.sectorName }
    }.orEmpty()
    val sectorOptions = when (roleType) {
        UserRoleType.HSO -> listOf(user.sector).filter { it.isNotBlank() }
        else -> (fallbackSectorOptions + catalogSectorOptions)
            .distinctBy { normalizeLocationName(it) }
            .sortedBy { normalizeLocationName(it) }
    }
    val selectedSectorValue = when (roleType) {
        UserRoleType.HSO -> sectorOptions.firstOrNull { sameName(it, user.sector) } ?: user.sector
        else -> selectedSector?.let { requested ->
            sectorOptions.firstOrNull { sameName(it, requested) }
        }
    }
    LaunchedEffect(sectorOptions, selectedSector, roleType) {
        if (roleType != UserRoleType.HSO && selectedSector != null && selectedSectorValue == null) {
            selectedSector = null
        }
    }
    val sectorScopedFacilities = when (roleType) {
        UserRoleType.CITY_MANAGER,
        UserRoleType.DISTRICT_MANAGER,
        UserRoleType.OTHER,
        -> {
            if (selectedSectorValue.isNullOrBlank()) districtScopedFacilities
            else districtScopedFacilities.filter { facility -> sameName(facility.sector, selectedSectorValue) }
        }
        UserRoleType.HSO -> districtScopedFacilities.filter { facility -> sameName(facility.sector, user.sector) }
    }

    val fallbackCells = sectorScopedFacilities.mapNotNull { it.cell.takeIf { value -> value.isNotBlank() } }
    val sectorIdsInScope = catalog?.let { locationCatalog ->
        when {
            !selectedSectorValue.isNullOrBlank() -> locationCatalog.sectors
                .filter { sector ->
                    sameName(sector.sectorName, selectedSectorValue) &&
                        (districtIdForScope == null || sector.districtId == districtIdForScope)
                }
                .map { it.sectorId }
                .toSet()
            districtIdForScope != null -> locationCatalog.sectors
                .filter { it.districtId == districtIdForScope }
                .map { it.sectorId }
                .toSet()
            roleType == UserRoleType.CITY_MANAGER -> locationCatalog.sectors.map { it.sectorId }.toSet()
            else -> emptySet()
        }
    }.orEmpty()
    val catalogCellOptions = catalog?.let { locationCatalog ->
        if (sectorIdsInScope.isEmpty()) emptyList()
        else locationCatalog.cells
            .filter { cell -> cell.sectorId in sectorIdsInScope }
            .map { cell -> cell.cellName }
    }.orEmpty()
    val cellOptions = (fallbackCells + catalogCellOptions)
        .distinctBy { normalizeLocationName(it) }
        .sortedBy { normalizeLocationName(it) }
    val selectedCellValue = selectedCell?.let { requested ->
        cellOptions.firstOrNull { it.equals(requested, ignoreCase = true) }
    }
    LaunchedEffect(cellOptions, selectedCell) {
        if (selectedCell != null && selectedCellValue == null) {
            selectedCell = null
        }
    }
    val cellScopedFacilities = if (selectedCellValue == null) {
        sectorScopedFacilities
    } else {
        sectorScopedFacilities.filter { facility -> sameName(facility.cell, selectedCellValue) }
    }

    val cellIdsInScope = catalog?.let { locationCatalog ->
        when {
            !selectedCellValue.isNullOrBlank() -> locationCatalog.cells
                .filter { cell ->
                    sameName(cell.cellName, selectedCellValue) &&
                        (sectorIdsInScope.isEmpty() || cell.sectorId in sectorIdsInScope)
                }
                .map { it.cellId }
                .toSet()
            sectorIdsInScope.isNotEmpty() -> locationCatalog.cells
                .filter { it.sectorId in sectorIdsInScope }
                .map { it.cellId }
                .toSet()
            else -> emptySet()
        }
    }.orEmpty()
    val catalogVillageOptions = catalog?.let { locationCatalog ->
        if (cellIdsInScope.isEmpty()) emptyList()
        else locationCatalog.villages
            .filter { village -> village.cellId in cellIdsInScope }
            .map { village -> village.villageName }
    }.orEmpty()
    val villageOptions = (catalogVillageOptions + (if (selectedCellValue == null) sectorScopedFacilities else cellScopedFacilities).mapNotNull { it.village.takeIf { value -> value.isNotBlank() } })
        .distinctBy { normalizeLocationName(it) }
        .sortedBy { normalizeLocationName(it) }
    val selectedVillageValue = selectedVillage?.let { requested ->
        villageOptions.firstOrNull { it.equals(requested, ignoreCase = true) }
    }
    LaunchedEffect(selectedCellValue, villageOptions, selectedVillage) {
        if (selectedVillage != null && selectedVillageValue == null) {
            selectedVillage = null
        }
        if (selectedCellValue == null && selectedVillage != null) {
            selectedVillage = null
        }
    }
    val villageScopedFacilities = if (selectedVillageValue == null) {
        cellScopedFacilities
    } else {
        cellScopedFacilities.filter { facility -> sameName(facility.village, selectedVillageValue) }
    }
    val visibleFacilityIds = villageScopedFacilities
        .flatMap { facility -> listOfNotNull(facility.id, facility.serverId) }
        .filter { it.isNotBlank() }
        .toSet()
    val roleScopedInspections = inspections.filter { inspection ->
        val facilityInScope = inspection.facilityId in visibleFacilityIds
        if (!facilityInScope) return@filter false
        when (roleType) {
            UserRoleType.HSO -> inspection.createdBy == user.id
            else -> true
        }
    }
    val sortedInspections = roleScopedInspections.sortedByDescending { it.createdAt }

    LaunchedEffect(user.id, roleType, reportPrefill, districtOptions, sectorOptions, cellOptions, villageOptions) {
        val prefill = reportPrefill ?: return@LaunchedEffect
        if (roleType == UserRoleType.CITY_MANAGER) {
            selectedDistrict = prefill.district?.let { requested ->
                districtOptions.firstOrNull { sameName(it, requested) }
            }
        } else {
            selectedDistrict = null
        }
        if (roleType != UserRoleType.HSO) {
            selectedSector = prefill.sector?.let { requested ->
                sectorOptions.firstOrNull { sameName(it, requested) }
            }
        }
        selectedCell = prefill.cell?.let { requested ->
            cellOptions.firstOrNull { sameName(it, requested) }
        }
        selectedVillage = prefill.village?.let { requested ->
            villageOptions.firstOrNull { sameName(it, requested) }
        }
        DraftStore.reportFiltersPrefill.value = null
    }

    val filteredByStatus = when (selectedFilter) {
        filterPending -> sortedInspections.filter { it.syncStatus.name == "PENDING" }
        filterSynced -> sortedInspections.filter { it.syncStatus.name == "SYNCED" }
        filterToday -> sortedInspections.filter { isSameDay(it.createdAt) }
        filterWeek -> sortedInspections.filter { isWithinLastDays(it.createdAt, 7) }
        else -> sortedInspections
    }
    val facilityLookup = villageScopedFacilities
        .flatMap { facility ->
            listOfNotNull(
                facility.id.takeIf { it.isNotBlank() }?.let { it to facility },
                facility.serverId?.takeIf { it.isNotBlank() }?.let { it to facility },
            )
        }
        .toMap()
    val filtered = filteredByStatus.filter { inspection ->
        matchesInspectionFilters(
            facility = facilityLookup[inspection.facilityId],
            district = selectedDistrictValue,
            sector = selectedSectorValue,
            cell = selectedCellValue,
            village = selectedVillageValue,
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
            if (roleType == UserRoleType.CITY_MANAGER) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.district),
                        value = selectedDistrictValue ?: "All Districts",
                        options = districtOptions,
                        allLabel = "All Districts",
                        onSelected = {
                            selectedDistrict = it
                            selectedSector = null
                            selectedCell = null
                            selectedVillage = null
                        },
                    )
                    FilterDropdown(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.filter_sector),
                        value = selectedSectorValue ?: stringResource(R.string.all_sectors),
                        options = sectorOptions,
                        allLabel = stringResource(R.string.all_sectors),
                        onSelected = {
                            selectedSector = it
                            selectedCell = null
                            selectedVillage = null
                        },
                    )
                }
            } else if (roleType == UserRoleType.DISTRICT_MANAGER) {
                FilterDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.filter_sector),
                    value = selectedSectorValue ?: stringResource(R.string.all_sectors),
                    options = sectorOptions,
                    allLabel = stringResource(R.string.all_sectors),
                    onSelected = {
                        selectedSector = it
                        selectedCell = null
                        selectedVillage = null
                    },
                )
            } else if (roleType == UserRoleType.HSO) {
                StyledTextField(
                    value = selectedSectorValue ?: user.sector,
                    onValueChange = {},
                    label = stringResource(R.string.filter_sector),
                    readOnly = true,
                    enabled = false,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.filter_cell),
                    value = selectedCellValue ?: stringResource(R.string.all_cells),
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
                    value = selectedVillageValue ?: stringResource(R.string.all_villages),
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
    facility: Facility?,
    district: String?,
    sector: String?,
    cell: String?,
    village: String?,
): Boolean {
    if (district == null && sector == null && cell == null && village == null) {
        return true
    }
    val matchesDistrict = district == null || facility?.district?.equals(district, ignoreCase = true) == true
    val matchesSector = sector == null || facility?.sector?.equals(sector, ignoreCase = true) == true
    val matchesCell = cell == null || facility?.cell?.equals(cell, ignoreCase = true) == true
    val matchesVillage = village == null || facility?.village?.equals(village, ignoreCase = true) == true
    return matchesDistrict && matchesSector && matchesCell && matchesVillage
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
    val normalizedLeft = normalizeLocationName(rawLeft)
    val normalizedRight = normalizeLocationName(rawRight)
    return normalizedLeft == normalizedRight ||
        normalizedLeft.contains(normalizedRight) ||
        normalizedRight.contains(normalizedLeft)
}

private fun normalizeLocationName(value: String?): String {
    return value
        .orEmpty()
        .trim()
        .lowercase()
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

private fun isSameDay(timestamp: Long): Boolean {
    val zoneId = java.time.ZoneId.systemDefault()
    val date = java.time.Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
    val today = java.time.LocalDate.now(zoneId)
    return date == today
}

private fun isWithinLastDays(timestamp: Long, days: Int): Boolean {
    val zoneId = java.time.ZoneId.systemDefault()
    val date = java.time.Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
    val today = java.time.LocalDate.now(zoneId)
    return !date.isBefore(today.minusDays((days - 1).toLong())) && !date.isAfter(today)
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
