package com.moses.inspectionapp.ui.screens.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import com.moses.inspectionapp.data.model.VisitType
import com.moses.inspectionapp.data.model.displayLabel
import com.moses.inspectionapp.data.model.isManager
import com.moses.inspectionapp.data.model.parseDecision
import com.moses.inspectionapp.data.model.parseUserRole
import com.moses.inspectionapp.data.remote.DashboardCompliancePoint
import com.moses.inspectionapp.data.remote.DashboardCountPoint
import com.moses.inspectionapp.data.remote.DashboardDecisionBreakdown
import com.moses.inspectionapp.data.remote.DashboardInsights
import com.moses.inspectionapp.data.remote.DashboardRepository
import com.moses.inspectionapp.data.remote.DashboardTopOffender
import com.moses.inspectionapp.data.remote.ManagedUser
import com.moses.inspectionapp.data.remote.UserManagementRepository
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.data.store.ReportFiltersPrefill
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.PrimaryButtonTone
import com.moses.inspectionapp.ui.components.SectionLabel
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onManageUsers: () -> Unit = {},
    onOpenReports: () -> Unit = {},
) {
    val repository = AppContainer.repository
    val user = repository.userProfile.collectAsState().value
    val inspections = repository.inspections.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val faults = repository.faults.collectAsState().value
    val roleType = parseUserRole(user.role)
    val dashboardRepository = remember { DashboardRepository() }
    val userManagementRepository = remember { UserManagementRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(AnalyticsUiState()) }
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.LAST_7_DAYS) }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    var selectedSector by remember { mutableStateOf<String?>(null) }
    var selectedCell by remember { mutableStateOf<String?>(null) }
    var selectedVillage by remember { mutableStateOf<String?>(null) }
    var selectedFacilityId by remember { mutableStateOf<String?>(null) }
    var selectedOfficerId by remember { mutableStateOf<String?>(null) }
    var selectedVisitType by remember { mutableStateOf<VisitType?>(null) }
    val locationCatalog = produceState<LocationCatalog?>(initialValue = null, key1 = context) {
        value = runCatching { LocationCatalogStore.load(context) }.getOrNull()
    }

    fun refresh(forceRefreshIndicator: Boolean) {
        scope.launch {
            uiState = uiState.copy(
                isLoading = uiState.insights == null,
                isRefreshing = forceRefreshIndicator,
                errorMessage = null,
            )
            val dashboardResult = dashboardRepository.loadInsights(topOffendersLimit = 6)
            val usersResult = if (roleType.isManager()) {
                userManagementRepository.getUsers()
            } else {
                null
            }
            val managedUsers = usersResult?.getOrNull().orEmpty()
            val activeInspectorCount = managedUsers.count { managed ->
                managed.role.equals("HSO", ignoreCase = true) && managed.isActive
            }

            uiState = if (dashboardResult.isSuccess) {
                uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    insights = dashboardResult.getOrNull(),
                    inspectorCount = activeInspectorCount,
                    managedUsers = managedUsers,
                    errorMessage = null,
                )
            } else {
                val fallbackMessage = dashboardResult.exceptionOrNull()?.message
                    ?: "Unable to load live analytics"
                uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    inspectorCount = activeInspectorCount,
                    managedUsers = managedUsers,
                    errorMessage = fallbackMessage,
                )
            }
        }
    }

    LaunchedEffect(user.id, user.role) {
        selectedDistrict = null
        selectedSector = null
        selectedCell = null
        selectedVillage = null
        selectedFacilityId = null
        selectedOfficerId = null
        selectedVisitType = null
        refresh(forceRefreshIndicator = false)
    }
    val zoneId = ZoneId.systemDefault()
    val facilitiesById = facilities
        .flatMap { facility ->
            listOfNotNull(
                facility.id.takeIf { it.isNotBlank() }?.let { it to facility },
                facility.serverId?.takeIf { it.isNotBlank() }?.let { it to facility },
            )
        }
        .toMap()
    val checklistCountByType = faults
        .filter { it.active }
        .groupingBy { it.inspectionTypeId }
        .eachCount()
    val defaultChecklistSize = resolveChecklistBaseline(
        checklistCountByType = checklistCountByType,
        inspections = inspections,
    )

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
    val roleScopedFacilityIds = roleScopedFacilities
        .flatMap { facility -> listOfNotNull(facility.id, facility.serverId) }
        .filter { it.isNotBlank() }
        .toSet()
    val roleScopedInspections = inspections.filter { inspection ->
        val facilityInScope = inspection.facilityId in roleScopedFacilityIds
        if (!facilityInScope) return@filter false
        when (roleType) {
            UserRoleType.HSO -> inspection.createdBy == user.id
            else -> true
        }
    }
    val catalog = locationCatalog.value
    val districtOptions = if (roleType == UserRoleType.CITY_MANAGER) {
        val fromCatalog = catalog
            ?.districtsSorted()
            ?.map { it.districtName }
            .orEmpty()
        val fallback = roleScopedFacilities.map { it.district }
        (fromCatalog + fallback + listOf("Gasabo", "Kicukiro", "Nyarugenge"))
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

    val sectorOptionsFromCatalog = catalog?.let { loadedCatalog ->
        val sectorsInScope = when (roleType) {
            UserRoleType.CITY_MANAGER -> {
                if (districtIdForScope == null) loadedCatalog.sectors
                else loadedCatalog.sectors.filter { it.districtId == districtIdForScope }
            }
            UserRoleType.DISTRICT_MANAGER,
            UserRoleType.HSO,
            UserRoleType.OTHER,
            -> {
                if (districtIdForScope == null) emptyList()
                else loadedCatalog.sectors.filter { it.districtId == districtIdForScope }
            }
        }
        sectorsInScope.map { it.sectorName }
    }.orEmpty()

    val sectorOptions = when (roleType) {
        UserRoleType.HSO -> {
            val catalogSector = sectorOptionsFromCatalog.firstOrNull { sameName(it, user.sector) }
            listOf(catalogSector ?: user.sector).filter { it.isNotBlank() }
        }
        UserRoleType.CITY_MANAGER,
        UserRoleType.DISTRICT_MANAGER,
        UserRoleType.OTHER,
        -> (sectorOptionsFromCatalog + districtScopedFacilities.map { it.sector })
            .filter { it.isNotBlank() }
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
        -> {
            if (selectedSectorValue.isNullOrBlank()) districtScopedFacilities
            else districtScopedFacilities.filter { facility -> sameName(facility.sector, selectedSectorValue) }
        }
        UserRoleType.HSO -> districtScopedFacilities.filter { facility -> sameName(facility.sector, user.sector) }
        UserRoleType.OTHER -> districtScopedFacilities
    }

    val fallbackCellOptions = sectorScopedFacilities
        .map { it.cell }
        .filter { it.isNotBlank() }
    val sectorIdsInScope = catalog?.let { loadedCatalog ->
        when {
            !selectedSectorValue.isNullOrBlank() -> {
                loadedCatalog.sectors
                    .filter { sector ->
                        sameName(sector.sectorName, selectedSectorValue) &&
                            (districtIdForScope == null || sector.districtId == districtIdForScope)
                    }
                    .map { it.sectorId }
                    .toSet()
            }
            districtIdForScope != null -> {
                loadedCatalog.sectors
                    .filter { sector -> sector.districtId == districtIdForScope }
                    .map { it.sectorId }
                    .toSet()
            }
            roleType == UserRoleType.CITY_MANAGER -> loadedCatalog.sectors.map { it.sectorId }.toSet()
            else -> emptySet()
        }
    }.orEmpty()

    val cellOptionsFromCatalog = catalog?.let { loadedCatalog ->
        if (sectorIdsInScope.isEmpty()) {
            emptyList()
        } else {
            loadedCatalog.cells
                .filter { cell -> cell.sectorId in sectorIdsInScope }
                .map { cell -> cell.cellName }
        }
    }.orEmpty()
    val cellOptions = (cellOptionsFromCatalog + fallbackCellOptions)
        .filter { it.isNotBlank() }
        .distinctBy { normalizeLocationName(it) }
        .sortedBy { normalizeLocationName(it) }
    val selectedCellValue = selectedCell?.let { requested ->
        cellOptions.firstOrNull { sameName(it, requested) }
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

    val cellIdsInScope = catalog?.let { loadedCatalog ->
        when {
            !selectedCellValue.isNullOrBlank() -> loadedCatalog.cells
                .filter { cell ->
                    sameName(cell.cellName, selectedCellValue) &&
                        (sectorIdsInScope.isEmpty() || cell.sectorId in sectorIdsInScope)
                }
                .map { it.cellId }
                .toSet()
            sectorIdsInScope.isNotEmpty() -> loadedCatalog.cells
                .filter { it.sectorId in sectorIdsInScope }
                .map { it.cellId }
                .toSet()
            else -> emptySet()
        }
    }.orEmpty()
    val villageOptionsFromCatalog = catalog?.let { loadedCatalog ->
        if (cellIdsInScope.isEmpty()) {
            emptyList()
        } else {
            loadedCatalog.villages
                .filter { village -> village.cellId in cellIdsInScope }
                .map { village -> village.villageName }
        }
    }.orEmpty()
    val villageOptions = (villageOptionsFromCatalog + (if (selectedCellValue == null) sectorScopedFacilities else cellScopedFacilities).map { it.village })
        .filter { it.isNotBlank() }
        .distinctBy { normalizeLocationName(it) }
        .sortedBy { normalizeLocationName(it) }
    val selectedVillageValue = selectedVillage?.let { requested ->
        villageOptions.firstOrNull { sameName(it, requested) }
    }
    LaunchedEffect(villageOptions, selectedVillage) {
        if (selectedVillage != null && selectedVillageValue == null) {
            selectedVillage = null
        }
    }
    val villageScopedFacilities = if (selectedVillageValue == null) {
        cellScopedFacilities
    } else {
        cellScopedFacilities.filter { facility -> sameName(facility.village, selectedVillageValue) }
    }

    val spatialScopedFacilityIds = villageScopedFacilities
        .flatMap { facility -> listOfNotNull(facility.id, facility.serverId) }
        .filter { it.isNotBlank() }
        .toSet()
    val spatialScopedInspections = roleScopedInspections.filter { it.facilityId in spatialScopedFacilityIds }
    val facilityOptions = villageScopedFacilities.sortedBy { it.name.lowercase(Locale.getDefault()) }
    val selectedFacilityItem = selectedFacilityId?.let { id ->
        facilityOptions.firstOrNull { facility ->
            facility.id == id || facility.serverId == id
        }
    }
    val selectedFacilityValue = selectedFacilityItem?.id
    val selectedFacilityKeys = listOfNotNull(
        selectedFacilityItem?.id?.takeIf { it.isNotBlank() },
        selectedFacilityItem?.serverId?.takeIf { it.isNotBlank() },
    ).toSet()
    LaunchedEffect(facilityOptions, selectedFacilityId) {
        if (selectedFacilityId != null && selectedFacilityValue == null) {
            selectedFacilityId = null
        }
    }
    val facilityScopedInspections = if (selectedFacilityKeys.isEmpty()) {
        spatialScopedInspections
    } else {
        spatialScopedInspections.filter { it.facilityId in selectedFacilityKeys }
    }

    val scopedManagedHso = uiState.managedUsers
        .filter { managed -> managed.role.equals("HSO", ignoreCase = true) && managed.isActive }
        .filter { managed ->
            when (roleType) {
                UserRoleType.CITY_MANAGER -> {
                    (selectedDistrictValue?.let { sameName(managed.district, it) } ?: true) &&
                        (selectedSectorValue?.let { sameName(managed.sector, it) } ?: true)
                }
                UserRoleType.DISTRICT_MANAGER -> {
                    sameName(managed.district, user.district) &&
                        (selectedSectorValue?.let { sameName(managed.sector, it) } ?: true)
                }
                UserRoleType.HSO -> {
                    managed.id == user.id ||
                        sameName(managed.email, user.email) ||
                        sameName(managed.fullName, user.fullName)
                }
                UserRoleType.OTHER -> true
            }
        }
    val showOfficerFilter = roleType != UserRoleType.HSO
    LaunchedEffect(showOfficerFilter) {
        if (!showOfficerFilter) {
            selectedOfficerId = null
        }
    }
    val officerOptions = if (showOfficerFilter) scopedManagedHso
        .map { managed -> OfficerFilterOption(id = managed.id, label = managed.fullName) }
        .sortedBy { it.label.lowercase(Locale.getDefault()) } else emptyList()
    val selectedOfficerValue = if (!showOfficerFilter) null else selectedOfficerId?.let { requested ->
        officerOptions.firstOrNull { it.id == requested }?.id
    }
    LaunchedEffect(officerOptions, selectedOfficerId) {
        if (selectedOfficerId != null && selectedOfficerValue == null) {
            selectedOfficerId = null
        }
    }
    val selectedOfficer = officerOptions.firstOrNull { it.id == selectedOfficerValue }
    val officerScopedInspections = if (selectedOfficer == null) {
        facilityScopedInspections
    } else {
        facilityScopedInspections.filter { inspection ->
            inspection.createdBy == selectedOfficer.id ||
                sameName(inspection.createdBy, selectedOfficer.label) ||
                inspection.teamMembers.any { member -> sameName(member, selectedOfficer.label) }
        }
    }

    val visitTypeOptions = officerScopedInspections
        .map { it.visitType }
        .distinct()
        .sortedBy { it.ordinal }
    val selectedVisitTypeValue = selectedVisitType?.let { requested ->
        visitTypeOptions.firstOrNull { it == requested }
    }
    LaunchedEffect(visitTypeOptions, selectedVisitType) {
        if (selectedVisitType != null && selectedVisitTypeValue == null) {
            selectedVisitType = null
        }
    }
    val visitTypeScopedInspections = if (selectedVisitTypeValue == null) {
        officerScopedInspections
    } else {
        officerScopedInspections.filter { it.visitType == selectedVisitTypeValue }
    }
    val filteredInspections = visitTypeScopedInspections.filter { inspection ->
        isWithinSelectedPeriod(
            createdAt = inspection.createdAt,
            period = selectedPeriod,
            zoneId = zoneId,
        )
    }

    val filteredFacilityIds = filteredInspections.map { it.facilityId }.toSet()
    val filteredFacilities = villageScopedFacilities.filter { facility ->
        facility.id in filteredFacilityIds || facility.serverId in filteredFacilityIds
    }

    val decisionBreakdown = buildLocalDecisionBreakdown(filteredInspections)
    val topOffenders = buildLocalTopOffenders(
        inspections = filteredInspections,
        facilitiesById = facilitiesById,
    )
    val bestFacilities = buildBestFacilities(
        inspections = filteredInspections,
        facilitiesById = facilitiesById,
        checklistCountByType = checklistCountByType,
        defaultChecklistSize = defaultChecklistSize,
    )
    val inspectionsTrend = buildLocalInspectionTrend(
        inspections = filteredInspections,
        windowDays = selectedPeriod.chartWindowDays,
    )
    val complianceTrend = buildLocalComplianceTrend(
        inspections = filteredInspections,
        checklistCountByType = checklistCountByType,
        defaultChecklistSize = defaultChecklistSize,
        windowDays = selectedPeriod.chartWindowDays,
    )
    val activeInspectorCount = if (selectedOfficerValue != null) {
        if (officerOptions.any { it.id == selectedOfficerValue }) 1 else 0
    } else {
        scopedManagedHso.size
    }
    val metrics = buildMetricCards(
        roleType = roleType,
        period = selectedPeriod,
        localInspections = filteredInspections,
        periodComparisonInspections = visitTypeScopedInspections,
        localFacilitiesCount = filteredFacilities.size,
        inspectorCount = activeInspectorCount,
        localTopOffenders = topOffenders,
        checklistCountByType = checklistCountByType,
        defaultChecklistSize = defaultChecklistSize,
    )
    val districtSummaries = buildDistrictSummaries(
        facilities = villageScopedFacilities,
        inspections = filteredInspections,
        checklistCountByType = checklistCountByType,
        defaultChecklistSize = defaultChecklistSize,
    )
    val officerSummaries = buildOfficerSummaries(scopedManagedHso, filteredInspections)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(
            title = stringResource(R.string.analytics_hub),
            onBack = onBack,
            actions = {
                IconButton(onClick = { refresh(forceRefreshIndicator = true) }) {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            color = AppColors.TextOnDark,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh_analytics),
                            tint = AppColors.TextOnDark,
                        )
                    }
                }
            },
        )
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = AppColors.SteelBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
            ) {
                item {
                    AnalyticsHeroCard(
                        roleType = roleType,
                        district = user.district,
                        sector = user.sector,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it },
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { refresh(forceRefreshIndicator = true) },
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    )
                }

                item {
                    AnalyticsFilterBar(
                        roleType = roleType,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it },
                        selectedDistrict = selectedDistrictValue,
                        districtOptions = districtOptions,
                        onDistrictSelected = {
                            selectedDistrict = it
                            selectedSector = null
                            selectedCell = null
                            selectedVillage = null
                        },
                        selectedSector = selectedSectorValue,
                        sectorOptions = sectorOptions,
                        onSectorSelected = {
                            if (roleType != UserRoleType.HSO) {
                                selectedSector = it
                                selectedCell = null
                                selectedVillage = null
                            }
                        },
                        selectedCell = selectedCellValue,
                        cellOptions = cellOptions,
                        onCellSelected = {
                            selectedCell = it
                            selectedVillage = null
                        },
                        selectedVillage = selectedVillageValue,
                        villageOptions = villageOptions,
                        onVillageSelected = { selectedVillage = it },
                        selectedFacilityId = selectedFacilityValue,
                        facilityOptions = facilityOptions,
                        onFacilitySelected = { selectedFacilityId = it },
                        selectedOfficerId = selectedOfficerValue,
                        officerOptions = officerOptions,
                        onOfficerSelected = { selectedOfficerId = it },
                        showOfficerFilter = showOfficerFilter,
                        selectedVisitType = selectedVisitTypeValue,
                        visitTypeOptions = visitTypeOptions,
                        onVisitTypeSelected = { selectedVisitType = it },
                    )
                }

                if (!uiState.errorMessage.isNullOrBlank()) {
                    item {
                        OfflineAnalyticsNotice(
                            message = uiState.errorMessage.orEmpty(),
                            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                        )
                    }
                }

                item {
                    MetricsGrid(
                        cards = metrics,
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    )
                }

                if (roleType == UserRoleType.CITY_MANAGER) {
                    item {
                        SectionLabel(text = "DISTRICT OVERVIEW")
                    }
                    item {
                        DistrictOverviewRow(districts = districtSummaries)
                    }
                }

                item {
                    AnalyticsCard(
                        title = "Inspections Over Time",
                        subtitle = "Daily inspection volume",
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    ) {
                        if (inspectionsTrend.isEmpty()) {
                            EmptyAnalyticsHint(stringResource(R.string.no_inspection_data))
                        } else {
                            InspectionTrendChart(points = inspectionsTrend)
                        }
                    }
                }

                item {
                    AnalyticsCard(
                        title = stringResource(R.string.compliance_target_title),
                        subtitle = stringResource(R.string.compliance_target_subtitle, 70),
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    ) {
                        if (complianceTrend.isEmpty()) {
                            EmptyAnalyticsHint(stringResource(R.string.no_compliance_data))
                        } else {
                            ComplianceTrendChart(
                                points = complianceTrend,
                                target = 70,
                            )
                        }
                    }
                }

                item {
                    AnalyticsCard(
                        title = stringResource(R.string.decision_mix),
                        subtitle = stringResource(R.string.decision_mix_subtitle),
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    ) {
                        if (decisionBreakdown.isEmpty()) {
                            EmptyAnalyticsHint(stringResource(R.string.no_decision_data))
                        } else {
                            DecisionDonutSection(decisionBreakdown = decisionBreakdown)
                        }
                    }
                }

                item {
                    if (roleType == UserRoleType.CITY_MANAGER) {
                        AnalyticsCard(
                            title = "District Comparison",
                            subtitle = "Side-by-side district performance",
                            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                        ) {
                            DistrictComparisonChart(districts = districtSummaries)
                        }
                    } else if (roleType == UserRoleType.DISTRICT_MANAGER) {
                        AnalyticsCard(
                            title = "Officer Performance",
                            subtitle = "Inspections completed per HSO",
                            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                        ) {
                            OfficerPerformanceList(officers = officerSummaries)
                        }
                    }
                }

                item {
                    AnalyticsCard(
                        title = stringResource(R.string.high_risk_facilities),
                        subtitle = stringResource(R.string.high_risk_facilities_subtitle),
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    ) {
                        if (topOffenders.isEmpty()) {
                            EmptyAnalyticsHint(stringResource(R.string.no_offenders_data))
                        } else {
                            TopOffendersList(topOffenders = topOffenders)
                        }
                    }
                }

                item {
                    AnalyticsCard(
                        title = "Best-Performing Facilities",
                        subtitle = "Facilities with the strongest weighted compliance",
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    ) {
                        if (bestFacilities.isEmpty()) {
                            EmptyAnalyticsHint("No top-performing facilities in this filter scope yet.")
                        } else {
                            BestFacilitiesList(facilities = bestFacilities)
                        }
                    }
                }

                if (roleType.isManager()) {
                    item {
                        PrimaryButton(
                            text = if (roleType == UserRoleType.DISTRICT_MANAGER) {
                                stringResource(R.string.manage_hso_team)
                            } else {
                                stringResource(R.string.manage_city_users)
                            },
                            onClick = onManageUsers,
                            leadingIcon = Icons.Rounded.Groups,
                            tone = PrimaryButtonTone.Accent,
                            modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                        )
                    }
                }

                item {
                    PrimaryButton(
                        text = "Download Report",
                        onClick = {
                            DraftStore.reportFiltersPrefill.value = ReportFiltersPrefill(
                                district = when (roleType) {
                                    UserRoleType.CITY_MANAGER -> selectedDistrictValue
                                    UserRoleType.DISTRICT_MANAGER,
                                    UserRoleType.HSO,
                                    UserRoleType.OTHER
                                    -> user.district
                                },
                                sector = when (roleType) {
                                    UserRoleType.CITY_MANAGER -> selectedSectorValue
                                    UserRoleType.DISTRICT_MANAGER -> selectedSectorValue
                                    UserRoleType.HSO -> user.sector
                                    UserRoleType.OTHER -> selectedSectorValue
                                },
                                cell = selectedCellValue,
                                village = selectedVillageValue,
                            )
                            onOpenReports()
                        },
                        leadingIcon = Icons.Rounded.Assessment,
                        modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScopeCard(
    roleType: UserRoleType,
    district: String,
    sector: String,
) {
    val subtitle = when (roleType) {
        UserRoleType.HSO -> stringResource(R.string.analytics_scope_hso, sector, district)
        UserRoleType.DISTRICT_MANAGER -> stringResource(R.string.analytics_scope_dm, district)
        UserRoleType.CITY_MANAGER -> stringResource(R.string.analytics_scope_cm)
        UserRoleType.OTHER -> stringResource(R.string.analytics_scope_default, sector, district)
    }
    Surface(
        color = AppColors.NavyDark,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Assessment,
                    contentDescription = null,
                    tint = AppColors.TextOnDark,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.analytics_role_label, roleType.displayLabel()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextOnDark,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextOnDarkMuted,
            )
        }
    }
}

@Composable
private fun AnalyticsHeroCard(
    roleType: UserRoleType,
    district: String,
    sector: String,
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCityManager = roleType == UserRoleType.CITY_MANAGER
    var periodExpanded by remember { mutableStateOf(false) }
    val scopeText = when (roleType) {
        UserRoleType.CITY_MANAGER -> "Kigali City - All Districts"
        UserRoleType.DISTRICT_MANAGER -> "District scope: $district"
        UserRoleType.HSO -> "Sector scope: $sector, $district"
        UserRoleType.OTHER -> "Scope: $sector, $district"
    }
    Surface(
        color = AppColors.NavyDark,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Analytics,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = if (isCityManager) "City Manager Analytics" else "${roleType.displayLabel()} Analytics",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = scopeText,
                color = AppColors.TextOnDarkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                            .clickable { periodExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = selectedPeriod.label,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false },
                    ) {
                        AnalyticsPeriod.values().forEach { period ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = period.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (period == selectedPeriod) AppColors.SteelBlue else AppColors.TextPrimary,
                                    )
                                },
                                onClick = {
                                    periodExpanded = false
                                    onPeriodSelected(period)
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = onRefresh) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.85f),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh_analytics),
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsFilterBar(
    roleType: UserRoleType,
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit,
    selectedDistrict: String?,
    districtOptions: List<String>,
    onDistrictSelected: (String?) -> Unit,
    selectedSector: String?,
    sectorOptions: List<String>,
    onSectorSelected: (String?) -> Unit,
    selectedCell: String?,
    cellOptions: List<String>,
    onCellSelected: (String?) -> Unit,
    selectedVillage: String?,
    villageOptions: List<String>,
    onVillageSelected: (String?) -> Unit,
    selectedFacilityId: String?,
    facilityOptions: List<Facility>,
    onFacilitySelected: (String?) -> Unit,
    selectedOfficerId: String?,
    officerOptions: List<OfficerFilterOption>,
    onOfficerSelected: (String?) -> Unit,
    showOfficerFilter: Boolean,
    selectedVisitType: VisitType?,
    visitTypeOptions: List<VisitType>,
    onVisitTypeSelected: (VisitType?) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardSurface)
            .padding(horizontal = Dimens.screenPadding, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterDropdownChip(
                icon = Icons.Rounded.DateRange,
                label = selectedPeriod.label,
                isActive = true,
                options = AnalyticsPeriod.values().map { period ->
                    FilterOption(id = period.key, label = period.label)
                },
                selectedId = selectedPeriod.key,
                onSelected = { selectedId ->
                    val selected = AnalyticsPeriod.values().firstOrNull { it.key == selectedId } ?: AnalyticsPeriod.LAST_7_DAYS
                    onPeriodSelected(selected)
                },
            )
        }
        if (roleType == UserRoleType.CITY_MANAGER) {
            item {
                FilterDropdownChip(
                    icon = Icons.Rounded.LocationOn,
                    label = selectedDistrict ?: "All Districts",
                    isActive = selectedDistrict != null,
                    options = listOf(FilterOption(id = FILTER_ALL_ID, label = "All Districts")) +
                        districtOptions.map { option -> FilterOption(id = option, label = option) },
                    selectedId = selectedDistrict ?: FILTER_ALL_ID,
                    onSelected = { selectedId ->
                        onDistrictSelected(selectedId.takeUnless { it == FILTER_ALL_ID })
                    },
                )
            }
        }
        if (roleType == UserRoleType.CITY_MANAGER || roleType == UserRoleType.DISTRICT_MANAGER) {
            item {
                FilterDropdownChip(
                    icon = Icons.Rounded.LocationOn,
                    label = selectedSector ?: "All Sectors",
                    isActive = selectedSector != null,
                    options = listOf(FilterOption(id = FILTER_ALL_ID, label = "All Sectors")) +
                        sectorOptions.map { option -> FilterOption(id = option, label = option) },
                    selectedId = selectedSector ?: FILTER_ALL_ID,
                    onSelected = { selectedId ->
                        onSectorSelected(selectedId.takeUnless { it == FILTER_ALL_ID })
                    },
                )
            }
        }
        if (roleType == UserRoleType.HSO) {
            item {
                LockedScopeChip(
                    icon = Icons.Rounded.LocationOn,
                    label = selectedSector ?: "Sector",
                )
            }
        }
        item {
            FilterDropdownChip(
                icon = Icons.Rounded.LocationOn,
                label = selectedCell ?: "All Cells",
                isActive = selectedCell != null,
                options = listOf(FilterOption(id = FILTER_ALL_ID, label = "All Cells")) +
                    cellOptions.map { option -> FilterOption(id = option, label = option) },
                selectedId = selectedCell ?: FILTER_ALL_ID,
                onSelected = { selectedId ->
                    onCellSelected(selectedId.takeUnless { it == FILTER_ALL_ID })
                },
            )
        }
        item {
            FilterDropdownChip(
                icon = Icons.Rounded.LocationOn,
                label = selectedVillage ?: "All Villages",
                isActive = selectedVillage != null,
                options = listOf(FilterOption(id = FILTER_ALL_ID, label = "All Villages")) +
                    villageOptions.map { option -> FilterOption(id = option, label = option) },
                selectedId = selectedVillage ?: FILTER_ALL_ID,
                onSelected = { selectedId ->
                    onVillageSelected(selectedId.takeUnless { it == FILTER_ALL_ID })
                },
            )
        }
        item {
            FilterDropdownChip(
                icon = Icons.Rounded.Storefront,
                label = facilityOptions.firstOrNull { facility ->
                    facility.id == selectedFacilityId || facility.serverId == selectedFacilityId
                }?.name ?: "All Facilities",
                isActive = selectedFacilityId != null,
                options = listOf(FilterOption(id = FILTER_ALL_ID, label = "All Facilities")) +
                    facilityOptions.map { facility ->
                        FilterOption(id = facility.id, label = facility.name)
                    },
                selectedId = selectedFacilityId ?: FILTER_ALL_ID,
                onSelected = { selectedId ->
                    onFacilitySelected(selectedId.takeUnless { it == FILTER_ALL_ID })
                },
            )
        }
        if (showOfficerFilter) {
            item {
                FilterDropdownChip(
                    icon = Icons.Rounded.Badge,
                    label = officerOptions.firstOrNull { it.id == selectedOfficerId }?.label ?: "All Officers",
                    isActive = selectedOfficerId != null,
                    options = listOf(FilterOption(id = FILTER_ALL_ID, label = "All Officers")) +
                        officerOptions.map { officer ->
                            FilterOption(id = officer.id, label = officer.label)
                        },
                    selectedId = selectedOfficerId ?: FILTER_ALL_ID,
                    onSelected = { selectedId ->
                        onOfficerSelected(selectedId.takeUnless { it == FILTER_ALL_ID })
                    },
                )
            }
        }
        item {
            FilterDropdownChip(
                icon = Icons.Rounded.AssignmentTurnedIn,
                label = selectedVisitType?.displayLabel() ?: "All Visits",
                isActive = selectedVisitType != null,
                options = listOf(FilterOption(id = FILTER_ALL_ID, label = "All Visits")) +
                    visitTypeOptions.map { visitType ->
                        FilterOption(id = visitType.name, label = visitType.displayLabel())
                    },
                selectedId = selectedVisitType?.name ?: FILTER_ALL_ID,
                onSelected = { selectedId ->
                    onVisitTypeSelected(
                        selectedId.takeUnless { it == FILTER_ALL_ID }?.let { VisitType.valueOf(it) },
                    )
                },
            )
        }
    }
}

@Composable
private fun LockedScopeChip(
    icon: ImageVector,
    label: String,
) {
    Row(
        modifier = Modifier
            .background(AppColors.PageBackground, RoundedCornerShape(50.dp))
            .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = AppColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 112.dp),
        )
    }
}

@Composable
private fun FilterDropdownChip(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    options: List<FilterOption>,
    selectedId: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChipButton(
            icon = icon,
            label = label,
            isActive = isActive,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(AppColors.CardSurface)
                .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(12.dp)),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (option.id == selectedId) AppColors.SteelBlue else AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(option.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                if (isActive) AppColors.SteelBlue else AppColors.PageBackground,
                RoundedCornerShape(50.dp),
            )
            .border(
                width = 0.5.dp,
                color = if (isActive) AppColors.SteelBlue else AppColors.BorderLight,
                shape = RoundedCornerShape(50.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color.White else AppColors.TextSecondary,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isActive) Color.White else AppColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 112.dp),
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = if (isActive) Color.White.copy(alpha = 0.8f) else AppColors.TextSecondary,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun DistrictOverviewRow(districts: List<DistrictSummary>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(districts) { district ->
            DistrictMiniCard(district = district)
        }
    }
}

@Composable
private fun DistrictMiniCard(district: DistrictSummary) {
    val districtColor = districtColor(district.name)
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.width(168.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(districtColor, RoundedCornerShape(2.dp)),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = district.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = AppColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Compliance",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
            )
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { district.complianceRate / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = complianceColor(district.complianceRate),
                trackColor = AppColors.BorderLight,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${district.complianceRate}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = AppColors.BorderLight)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = formatInt(district.inspections),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = districtColor,
                    )
                    Text(
                        text = "Inspections",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RF ${formatShort(district.fines)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = "Fines",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineAnalyticsNotice(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = AppColors.StatusWarningBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppColors.StatusWarning),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = AppColors.StatusWarning,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.analytics_fallback_notice, message),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun MetricsGrid(
    cards: List<MetricCardModel>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 360.dp) 1 else 2
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            cards.chunked(columns).forEach { rowCards ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowCards.forEach { metric ->
                        MetricCard(
                            metric = metric,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                    if (rowCards.size < columns) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    metric: MetricCardModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = modifier.height(168.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(metric.iconBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = metric.icon,
                        contentDescription = null,
                        tint = metric.accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (!metric.trend.isNullOrBlank() && metric.trendUp != null) {
                    Row(
                        modifier = Modifier
                            .background(
                                if (metric.trendUp) Color(0xFFDCFCE7) else Color(0xFFFDECEB),
                                RoundedCornerShape(6.dp),
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            imageVector = if (metric.trendUp) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (metric.trendUp) Color(0xFF16A34A) else AppColors.AccentRed,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = metric.trend,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (metric.trendUp) Color(0xFF16A34A) else AppColors.AccentRed,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = metric.value,
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = metric.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!metric.subtitle.isNullOrBlank()) {
                Text(
                    text = metric.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                SectionLabel(text = title)
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                    )
                }
                content()
            },
        )
    }
}

@Composable
private fun EmptyAnalyticsHint(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = AppColors.TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    )
}

@Composable
private fun InspectionTrendChart(points: List<DashboardCountPoint>) {
    val visiblePoints = points
    val maxCount = max(1, visiblePoints.maxOfOrNull { it.count } ?: 1)
    val total = visiblePoints.sumOf { it.count }
    val average = if (visiblePoints.isNotEmpty()) {
        (total / visiblePoints.size.toFloat()).roundToInt()
    } else {
        0
    }
    val peak = visiblePoints.maxOfOrNull { it.count } ?: 0
    val axisWidth = 36.dp
    val chartHeight = 196.dp
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    var chartSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChartInfoChip(
                label = "Total",
                value = formatInt(total),
                modifier = Modifier.weight(1f),
            )
            ChartInfoChip(
                label = "Peak",
                value = formatInt(peak),
                modifier = Modifier.weight(1f),
            )
            ChartInfoChip(
                label = "Avg/day",
                value = formatInt(average),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .width(axisWidth)
                    .height(chartHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                for (step in 4 downTo 0) {
                    val yValue = ((maxCount * step) / 4f).roundToInt()
                    Text(
                        text = formatInt(yValue),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(chartHeight)
                    .onSizeChanged { chartSize = it }
                    .pointerInput(visiblePoints) {
                        detectTapGestures(
                            onTap = { offset ->
                                highlightedIndex = nearestDataPointIndex(
                                    x = offset.x,
                                    width = size.width.toFloat(),
                                    pointCount = visiblePoints.size,
                                )
                            },
                        )
                    }
                    .pointerInput(visiblePoints) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                highlightedIndex = nearestDataPointIndex(
                                    x = offset.x,
                                    width = size.width.toFloat(),
                                    pointCount = visiblePoints.size,
                                )
                            },
                            onDragEnd = { highlightedIndex = null },
                            onDragCancel = { highlightedIndex = null },
                            onDrag = { change, _ ->
                                highlightedIndex = nearestDataPointIndex(
                                    x = change.position.x,
                                    width = size.width.toFloat(),
                                    pointCount = visiblePoints.size,
                                )
                            },
                        )
                    },
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    if (visiblePoints.isEmpty()) return@Canvas

                    val gridColor = AppColors.BorderLight.copy(alpha = 0.82f)
                    for (index in 0..4) {
                        val y = size.height * index / 4f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    fun yFor(value: Int): Float {
                        val clamped = value.coerceAtLeast(0)
                        val normalized = (clamped.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                        return size.height * (1f - normalized)
                    }

                    val xStep = if (visiblePoints.size > 1) {
                        size.width / (visiblePoints.size - 1).toFloat()
                    } else {
                        0f
                    }
                    val pointsOffsets = visiblePoints.mapIndexed { index, point ->
                        val x = if (visiblePoints.size > 1) xStep * index else size.width / 2f
                        Offset(x, yFor(point.count))
                    }

                    if (pointsOffsets.isNotEmpty()) {
                        val areaPath = Path().apply {
                            moveTo(pointsOffsets.first().x, size.height)
                            lineTo(pointsOffsets.first().x, pointsOffsets.first().y)
                            pointsOffsets.drop(1).forEach { offset -> lineTo(offset.x, offset.y) }
                            lineTo(pointsOffsets.last().x, size.height)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AppColors.SteelBlue.copy(alpha = 0.22f),
                                    Color.Transparent,
                                ),
                                startY = 0f,
                                endY = size.height,
                            ),
                        )
                    }

                    val curvePath = Path()
                    pointsOffsets.forEachIndexed { index, offset ->
                        if (index == 0) {
                            curvePath.moveTo(offset.x, offset.y)
                        } else {
                            val prev = pointsOffsets[index - 1]
                            val midX = (prev.x + offset.x) / 2f
                            curvePath.cubicTo(midX, prev.y, midX, offset.y, offset.x, offset.y)
                        }
                    }
                    drawPath(
                        path = curvePath,
                        color = AppColors.SteelBlue,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )

                    pointsOffsets.forEachIndexed { index, offset ->
                        val highlighted = highlightedIndex == index
                        drawCircle(
                            color = if (highlighted) AppColors.AccentOrange else AppColors.SteelBlue,
                            radius = if (highlighted) 5.dp.toPx() else 4.dp.toPx(),
                            center = offset,
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = offset,
                        )
                    }

                    val selectedIndex = highlightedIndex
                    if (selectedIndex != null && selectedIndex in pointsOffsets.indices) {
                        val point = pointsOffsets[selectedIndex]
                        drawLine(
                            color = AppColors.TextSecondary.copy(alpha = 0.35f),
                            start = Offset(point.x, 0f),
                            end = Offset(point.x, size.height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                        )
                    }
                }

                val selectedIndex = highlightedIndex
                if (selectedIndex != null && selectedIndex in visiblePoints.indices && chartSize.width > 0) {
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val point = visiblePoints[selectedIndex]
                    val fraction = if (visiblePoints.size > 1) {
                        selectedIndex.toFloat() / (visiblePoints.lastIndex).toFloat()
                    } else {
                        0.5f
                    }
                    val tooltipWidthPx = with(density) { 124.dp.toPx() }
                    val topOffsetPx = with(density) { 6.dp.roundToPx() }
                    val tooltipX = (chartSize.width * fraction) - (tooltipWidthPx / 2f)
                    Surface(
                        color = AppColors.NavyDark.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.22f)),
                        modifier = Modifier.offset {
                            val x = tooltipX
                                .coerceAtLeast(0f)
                                .coerceAtMost(chartSize.width - tooltipWidthPx)
                            IntOffset(x.roundToInt(), topOffsetPx)
                        },
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = shortDateLabel(point.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                            Text(
                                text = "${point.count} inspections",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = axisWidth + 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            visiblePoints.forEach { point ->
                Text(
                    text = shortDateLabel(point.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ComplianceTrendChart(
    points: List<DashboardCompliancePoint>,
    target: Int,
) {
    val visiblePoints = points
    val clampedTarget = target.coerceIn(0, 100)
    val axisWidth = 36.dp
    val chartHeight = 196.dp
    val latest = visiblePoints.lastOrNull()?.complianceRate ?: 0
    val first = visiblePoints.firstOrNull()?.complianceRate ?: latest
    val delta = latest - first

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChartInfoChip(
                label = "Latest",
                value = "$latest%",
                modifier = Modifier.weight(1f),
            )
            ChartInfoChip(
                label = "Target",
                value = "$clampedTarget%",
                modifier = Modifier.weight(1f),
            )
            ChartInfoChip(
                label = "Trend",
                value = if (delta >= 0) "+$delta pts" else "$delta pts",
                accentColor = if (delta >= 0) AppColors.AccentGreen else AppColors.AccentRed,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .width(axisWidth)
                    .height(chartHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                for (step in 4 downTo 0) {
                    val yValue = ((100 * step) / 4f).roundToInt()
                    Text(
                        text = "$yValue%",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(chartHeight),
            ) {
                if (visiblePoints.isEmpty()) return@Canvas

                val gridColor = AppColors.BorderLight.copy(alpha = 0.8f)
                for (index in 0..4) {
                    val y = size.height * index / 4f
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                fun yFor(value: Int): Float {
                    val clamped = value.coerceIn(0, 100)
                    return size.height * (1f - clamped / 100f)
                }

                val targetY = yFor(clampedTarget)
                drawLine(
                    color = AppColors.AccentOrange,
                    start = Offset(0f, targetY),
                    end = Offset(size.width, targetY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                )

                val xStep = if (visiblePoints.size > 1) {
                    size.width / (visiblePoints.size - 1).toFloat()
                } else {
                    0f
                }

                val pointsOffsets = visiblePoints.mapIndexed { index, point ->
                    val x = if (visiblePoints.size > 1) xStep * index else size.width / 2f
                    Offset(x, yFor(point.complianceRate))
                }

                if (pointsOffsets.isNotEmpty()) {
                    val areaPath = Path().apply {
                        moveTo(pointsOffsets.first().x, size.height)
                        lineTo(pointsOffsets.first().x, pointsOffsets.first().y)
                        pointsOffsets.drop(1).forEach { offset ->
                            lineTo(offset.x, offset.y)
                        }
                        lineTo(pointsOffsets.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AppColors.SteelBlue.copy(alpha = 0.28f),
                                AppColors.SteelBlue.copy(alpha = 0.06f),
                            ),
                            startY = 0f,
                            endY = size.height,
                        ),
                    )
                }

                val linePath = Path()
                pointsOffsets.forEachIndexed { index, offset ->
                    if (index == 0) {
                        linePath.moveTo(offset.x, offset.y)
                    } else {
                        val previous = pointsOffsets[index - 1]
                        val midX = (previous.x + offset.x) / 2f
                        linePath.cubicTo(midX, previous.y, midX, offset.y, offset.x, offset.y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = AppColors.SteelBlue,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )

                pointsOffsets.forEach { offset ->
                    drawCircle(
                        color = AppColors.SteelBlue,
                        radius = 4.5.dp.toPx(),
                        center = offset,
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.4.dp.toPx(),
                        center = offset,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = axisWidth + 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            visiblePoints.forEach { point ->
                Text(
                    text = shortDateLabel(point.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartLegendItem(
                color = AppColors.SteelBlue,
                label = stringResource(R.string.compliance_rate),
            )
            ChartLegendItem(
                color = AppColors.AccentOrange,
                label = stringResource(R.string.target_percent, clampedTarget),
                dashed = true,
            )
        }
    }
}

@Composable
private fun ChartInfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = AppColors.SteelBlue,
) {
    Surface(
        color = AppColors.PageBackground,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = accentColor,
            )
        }
    }
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String,
    dashed: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(
            modifier = Modifier
                .width(16.dp)
                .height(10.dp),
        ) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2.2.dp.toPx(),
                pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f) else null,
                cap = StrokeCap.Round,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextSecondary,
        )
    }
}

@Composable
private fun DecisionDonutSection(decisionBreakdown: List<DashboardDecisionBreakdown>) {
    val decisions = decisionBreakdown
        .filter { it.count > 0 }
        .sortedByDescending { it.count }
        .map { item ->
            val decision = parseDecision(item.decision)
            DecisionSlice(
                label = decisionTitleText(decision),
                count = item.count,
                color = decisionColor(decision),
            )
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DecisionDonutChart(
            decisions = decisions,
            modifier = Modifier.size(138.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            decisions.forEach { decision ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(decision.color, CircleShape),
                    )
                    Text(
                        text = decision.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatInt(decision.count),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = decision.color,
                    )
                }
            }
        }
    }
}

@Composable
private fun DecisionDonutChart(
    decisions: List<DecisionSlice>,
    modifier: Modifier = Modifier,
) {
    val total = decisions.sumOf { it.count }.toFloat()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 28.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            var startAngle = -90f
            decisions.forEach { decision ->
                val sweep = if (total > 0f) (decision.count / total) * 360f else 0f
                drawArc(
                    color = decision.color,
                    startAngle = startAngle,
                    sweepAngle = (sweep - 2f).coerceAtLeast(0f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatInt(total.toInt()),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = AppColors.TextPrimary,
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun OfficerPerformanceList(officers: List<OfficerSummary>) {
    if (officers.isEmpty()) {
        EmptyAnalyticsHint("No active HSO performance data yet.")
        return
    }
    officers.take(5).forEachIndexed { index, officer ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
                modifier = Modifier.width(16.dp),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AppColors.NavyDark, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials(officer.name),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = officer.name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = officer.sector,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .background(AppColors.SteelBlueTint, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = formatInt(officer.inspections),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.SteelBlue,
                )
            }
        }
        if (index < officers.take(5).lastIndex) {
            HorizontalDivider(thickness = 0.5.dp, color = AppColors.BorderLight)
        }
    }
}

@Composable
private fun DistrictComparisonChart(districts: List<DistrictSummary>) {
    val maxInspections = max(1, districts.maxOfOrNull { it.inspections } ?: 1)
    val maxFines = max(1, districts.maxOfOrNull { it.fines } ?: 1)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            "Inspections" to districts.map { it.name to (it.inspections.toFloat() / maxInspections) },
            "Compliance" to districts.map { it.name to (it.complianceRate.toFloat() / 100f) },
            "Fines" to districts.map { it.name to (it.fines.toFloat() / maxFines) },
        ).forEach { (metric, values) ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = metric,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
                values.forEach { (districtName, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = districtName,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.width(78.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(AppColors.BorderLight, RoundedCornerShape(4.dp)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(value.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(districtColor(districtName), RoundedCornerShape(4.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DecisionBreakdownList(decisionBreakdown: List<DashboardDecisionBreakdown>) {
    val ordered = decisionBreakdown.sortedByDescending { it.count }
    val maxCount = max(1, ordered.maxOfOrNull { it.count } ?: 1)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ordered.forEach { item ->
            val decision = parseDecision(item.decision)
            val color = decisionColor(decision)
            val progress = item.count.toFloat() / maxCount.toFloat()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = decisionTitle(decision),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = item.count.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = color,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(AppColors.SteelBlueTint, RoundedCornerShape(10.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .background(color, RoundedCornerShape(10.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopOffendersList(topOffenders: List<DashboardTopOffender>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        topOffenders.take(5).forEachIndexed { index, offender ->
            Surface(
                color = if (index == 0) AppColors.StatusImmediateBg else AppColors.CardSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (index == 0) AppColors.AccentRed else AppColors.SteelBlueTint,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (index == 0) Color.White else AppColors.SteelBlue,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = offender.facilityName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val scopeText = offender.sector.orEmpty()
                        if (scopeText.isNotBlank()) {
                            Text(
                                text = scopeText,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.faults_count_short, offender.totalFaults),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.AccentRed,
                        )
                        Text(
                            text = stringResource(R.string.inspections_count_short, offender.inspectionCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextSecondary,
                        )
                    }
                }
            }
            if (index < topOffenders.take(5).lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun BestFacilitiesList(facilities: List<BestFacilitySummary>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        facilities.take(5).forEachIndexed { index, facility ->
            Surface(
                color = if (index == 0) AppColors.AccentGreenBg else AppColors.CardSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (index == 0) Color(0xFF16A34A) else AppColors.SteelBlueTint,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (index == 0) Color.White else AppColors.SteelBlue,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = facility.facilityName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val scopeText = facility.sector.orEmpty()
                        if (scopeText.isNotBlank()) {
                            Text(
                                text = scopeText,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${facility.complianceRate}%",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF16A34A),
                        )
                        Text(
                            text = stringResource(R.string.inspections_count_short, facility.inspectionCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextSecondary,
                        )
                    }
                }
            }
            if (index < facilities.take(5).lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun buildMetricCards(
    roleType: UserRoleType,
    period: AnalyticsPeriod,
    localInspections: List<Inspection>,
    periodComparisonInspections: List<Inspection>,
    localFacilitiesCount: Int,
    inspectorCount: Int?,
    localTopOffenders: List<DashboardTopOffender>,
    checklistCountByType: Map<String, Int>,
    defaultChecklistSize: Int,
): List<MetricCardModel> {
    val totalInspections = localInspections.size
    val premisesInspected = max(localFacilitiesCount, localInspections.map { it.facilityId }.distinct().count())
    val complianceRate = computeCompliance(
        inspections = localInspections,
        checklistCountByType = checklistCountByType,
        defaultChecklistSize = defaultChecklistSize,
    )
    val faultsFound = localInspections.sumOf { it.faultCount }
    val repeatOffenders = localTopOffenders.count { it.inspectionCount > 1 }
    val periodChange = computePeriodChangePercent(
        inspections = periodComparisonInspections,
        period = period,
        zoneId = ZoneId.systemDefault(),
    )
    val complianceAccent = complianceColor(complianceRate)

    val coreCards = mutableListOf(
        MetricCardModel(
            title = stringResource(R.string.total_inspections_title),
            value = formatInt(totalInspections),
            subtitle = periodChange?.let { stringResource(R.string.period_change_percent, it) },
            accentColor = AppColors.SteelBlue,
            icon = Icons.Rounded.AssignmentTurnedIn,
            iconBg = AppColors.SteelBlueTint,
            trend = periodChange?.let { if (it >= 0) "+$it%" else "$it%" },
            trendUp = periodChange?.let { it >= 0 },
        ),
        MetricCardModel(
            title = stringResource(R.string.facilities_covered_title),
            value = formatInt(premisesInspected),
            subtitle = stringResource(R.string.scope_coverage_label),
            accentColor = Color(0xFF16A34A),
            icon = Icons.Rounded.Storefront,
            iconBg = Color(0xFFDCFCE7),
        ),
        MetricCardModel(
            title = stringResource(R.string.compliance_score_title),
            value = stringResource(R.string.percent_value, complianceRate),
            subtitle = stringResource(R.string.compliance_rate),
            accentColor = complianceAccent,
            icon = Icons.Rounded.CheckCircle,
            iconBg = complianceAccent.copy(alpha = 0.12f),
        ),
        MetricCardModel(
            title = stringResource(R.string.faults_found_title),
            value = formatInt(faultsFound),
            subtitle = stringResource(R.string.non_compliant_items),
            accentColor = AppColors.StatusWarning,
            icon = Icons.Rounded.WarningAmber,
            iconBg = AppColors.StatusWarningBg,
        ),
    )

    if (roleType.isManager()) {
        coreCards += MetricCardModel(
            title = stringResource(R.string.active_inspectors_title),
            value = formatInt(inspectorCount ?: 0),
            subtitle = stringResource(R.string.hso_accounts_label),
            accentColor = AppColors.SteelBlue,
            icon = Icons.Rounded.Groups,
            iconBg = AppColors.SteelBlueTint,
        )
        coreCards += MetricCardModel(
            title = stringResource(R.string.repeat_offenders_title),
            value = formatInt(repeatOffenders),
            subtitle = stringResource(R.string.facilities_with_multiple_issues),
            accentColor = AppColors.AccentOrange,
            icon = Icons.Rounded.Refresh,
            iconBg = AppColors.StatusClosureBg,
        )
    }
    return coreCards
}

private fun buildLocalInspectionTrend(
    inspections: List<Inspection>,
    windowDays: Int,
): List<DashboardCountPoint> {
    val zoneId = ZoneId.systemDefault()
    val grouped = inspections.groupingBy { inspection ->
        Instant.ofEpochMilli(inspection.createdAt).atZone(zoneId).toLocalDate()
    }.eachCount()
    val today = LocalDate.now(zoneId)
    val totalDays = windowDays.coerceIn(1, 120)
    return (totalDays - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        DashboardCountPoint(
            date = date.toString(),
            count = grouped[date] ?: 0,
        )
    }
}

private fun buildLocalComplianceTrend(
    inspections: List<Inspection>,
    checklistCountByType: Map<String, Int>,
    defaultChecklistSize: Int,
    windowDays: Int,
): List<DashboardCompliancePoint> {
    val zoneId = ZoneId.systemDefault()
    val grouped = inspections.groupBy { inspection ->
        Instant.ofEpochMilli(inspection.createdAt).atZone(zoneId).toLocalDate()
    }
    val today = LocalDate.now(zoneId)
    val totalDays = windowDays.coerceIn(1, 120)
    return (totalDays - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        val items = grouped[date].orEmpty()
        val rate = computeCompliance(
            inspections = items,
            checklistCountByType = checklistCountByType,
            defaultChecklistSize = defaultChecklistSize,
        )
        DashboardCompliancePoint(
            date = date.toString(),
            complianceRate = rate,
        )
    }
}

private fun buildLocalDecisionBreakdown(inspections: List<Inspection>): List<DashboardDecisionBreakdown> {
    val decisionOrder = listOf(
        Decision.NO_ACTION,
        Decision.WARNING,
        Decision.CLOSURE_DEADLINE,
        Decision.CLOSURE_IMMEDIATE,
        Decision.PROSECUTION_RECOMMENDED,
    )
    return decisionOrder.map { decision ->
        DashboardDecisionBreakdown(
            decision = decision.name,
            count = inspections.count { it.decision == decision },
        )
    }.filter { it.count > 0 }
}

private fun buildLocalTopOffenders(
    inspections: List<Inspection>,
    facilitiesById: Map<String, Facility>,
): List<DashboardTopOffender> {
    return inspections
        .groupBy { it.facilityId }
        .map { (facilityId, items) ->
            val first = items.first()
            val facility = facilitiesById[facilityId]
            DashboardTopOffender(
                facilityId = facilityId,
                facilityName = first.facilityName,
                sector = facility?.sector,
                totalFaults = items.sumOf { it.faultCount },
                totalFines = items.sumOf { it.totalFine },
                inspectionCount = items.size,
            )
        }
        .sortedWith(
            compareByDescending<DashboardTopOffender> { it.totalFaults }
                .thenByDescending { it.inspectionCount },
        )
}

private fun buildDistrictSummaries(
    facilities: List<Facility>,
    inspections: List<Inspection>,
    checklistCountByType: Map<String, Int>,
    defaultChecklistSize: Int,
): List<DistrictSummary> {
    val facilityById = facilities
        .flatMap { facility ->
            listOfNotNull(
                facility.id.takeIf { it.isNotBlank() }?.let { it to facility },
                facility.serverId?.takeIf { it.isNotBlank() }?.let { it to facility },
            )
        }
        .toMap()
    return listOf("Gasabo", "Kicukiro", "Nyarugenge").map { district ->
        val districtInspections = inspections.filter { inspection ->
            sameName(facilityById[inspection.facilityId]?.district, district)
        }
        DistrictSummary(
            name = district,
            inspections = districtInspections.size,
            complianceRate = computeCompliance(
                inspections = districtInspections,
                checklistCountByType = checklistCountByType,
                defaultChecklistSize = defaultChecklistSize,
            ),
            fines = districtInspections.sumOf { it.totalFine },
        )
    }
}

private fun buildOfficerSummaries(
    managedUsers: List<ManagedUser>,
    inspections: List<Inspection>,
): List<OfficerSummary> {
    return managedUsers
        .filter { it.role.equals("HSO", ignoreCase = true) && it.isActive }
        .map { officer ->
            val completed = inspections.count { inspection ->
                inspection.createdBy == officer.id ||
                    inspection.teamMembers.any { member -> sameName(member, officer.fullName) }
            }
            OfficerSummary(
                name = officer.fullName,
                sector = officer.sector,
                inspections = completed,
            )
        }
        .filter { it.inspections > 0 }
        .sortedByDescending { it.inspections }
}

private fun buildBestFacilities(
    inspections: List<Inspection>,
    facilitiesById: Map<String, Facility>,
    checklistCountByType: Map<String, Int>,
    defaultChecklistSize: Int,
): List<BestFacilitySummary> {
    return inspections
        .groupBy { it.facilityId }
        .mapNotNull { (facilityId, facilityInspections) ->
            val facility = facilitiesById[facilityId] ?: return@mapNotNull null
            val compliance = computeCompliance(
                inspections = facilityInspections,
                checklistCountByType = checklistCountByType,
                defaultChecklistSize = defaultChecklistSize,
            )
            BestFacilitySummary(
                facilityId = facilityId,
                facilityName = facility.name,
                sector = facility.sector,
                complianceRate = compliance,
                inspectionCount = facilityInspections.size,
                faultCount = facilityInspections.sumOf { it.faultCount },
            )
        }
        .sortedWith(
            compareByDescending<BestFacilitySummary> { it.complianceRate }
                .thenByDescending { it.inspectionCount }
                .thenBy { it.faultCount },
        )
}

private fun computeCompliance(
    inspections: List<Inspection>,
    checklistCountByType: Map<String, Int>,
    defaultChecklistSize: Int,
): Int {
    if (inspections.isEmpty()) return 0
    var compliantItems = 0
    var expectedItems = 0
    inspections.forEach { inspection ->
        val checklistSize = expectedChecklistSize(
            inspection = inspection,
            checklistCountByType = checklistCountByType,
            defaultChecklistSize = defaultChecklistSize,
        )
        expectedItems += checklistSize
        compliantItems += (checklistSize - inspection.faultCount).coerceAtLeast(0)
    }
    if (expectedItems <= 0) return 0
    return ((compliantItems * 100f) / expectedItems.toFloat())
        .roundToInt()
        .coerceIn(0, 100)
}

private fun expectedChecklistSize(
    inspection: Inspection,
    checklistCountByType: Map<String, Int>,
    defaultChecklistSize: Int,
): Int {
    val fromType = inspection.inspectionTypeId?.let { checklistCountByType[it] } ?: 0
    val baseline = if (fromType > 0) fromType else defaultChecklistSize
    return max(1, max(baseline, inspection.faultCount + 1))
}

private fun resolveChecklistBaseline(
    checklistCountByType: Map<String, Int>,
    inspections: List<Inspection>,
): Int {
    val fromCatalog = checklistCountByType.values.filter { it > 0 }
    if (fromCatalog.isNotEmpty()) {
        return fromCatalog.maxOrNull() ?: 20
    }
    val fromInspectionHistory = inspections.maxOfOrNull { it.faultCount + 5 } ?: 20
    return fromInspectionHistory.coerceAtLeast(10)
}

private fun computePeriodChangePercent(
    inspections: List<Inspection>,
    period: AnalyticsPeriod,
    zoneId: ZoneId,
): Int? {
    val windowDays = period.windowDays ?: return null
    val today = LocalDate.now(zoneId)
    val currentStart = today.minusDays((windowDays - 1).toLong())
    val previousStart = currentStart.minusDays(windowDays.toLong())
    val previousEnd = currentStart.minusDays(1)

    val currentCount = inspections.count { inspection ->
        val date = Instant.ofEpochMilli(inspection.createdAt).atZone(zoneId).toLocalDate()
        !date.isBefore(currentStart) && !date.isAfter(today)
    }
    val previousCount = inspections.count { inspection ->
        val date = Instant.ofEpochMilli(inspection.createdAt).atZone(zoneId).toLocalDate()
        !date.isBefore(previousStart) && !date.isAfter(previousEnd)
    }
    if (previousCount == 0) return if (currentCount == 0) 0 else 100
    return (((currentCount - previousCount) * 100f) / previousCount.toFloat()).roundToInt()
}

private fun isWithinSelectedPeriod(
    createdAt: Long,
    period: AnalyticsPeriod,
    zoneId: ZoneId,
): Boolean {
    val days = period.windowDays ?: return true
    val date = Instant.ofEpochMilli(createdAt).atZone(zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)
    return !date.isBefore(today.minusDays((days - 1).toLong())) && !date.isAfter(today)
}

@Composable
private fun decisionTitle(decision: Decision): String {
    return when (decision) {
        Decision.NO_ACTION -> stringResource(R.string.decision_no_action)
        Decision.WARNING -> stringResource(R.string.decision_warning)
        Decision.CLOSURE_DEADLINE -> stringResource(R.string.decision_closure_deadline)
        Decision.CLOSURE_IMMEDIATE -> stringResource(R.string.decision_closure_immediate)
        Decision.PROSECUTION_RECOMMENDED -> stringResource(R.string.decision_prosecution)
    }
}

private fun decisionTitleText(decision: Decision): String {
    return when (decision) {
        Decision.NO_ACTION -> "Compliant"
        Decision.WARNING -> "Warning"
        Decision.CLOSURE_DEADLINE -> "Temporary Closure"
        Decision.CLOSURE_IMMEDIATE -> "Permanent Closure"
        Decision.PROSECUTION_RECOMMENDED -> "Fine"
    }
}

private fun decisionColor(decision: Decision): Color {
    return when (decision) {
        Decision.NO_ACTION -> Color(0xFF16A34A)
        Decision.WARNING -> Color(0xFFB45309)
        Decision.CLOSURE_DEADLINE -> AppColors.AccentOrange
        Decision.CLOSURE_IMMEDIATE -> AppColors.AccentRed
        Decision.PROSECUTION_RECOMMENDED -> AppColors.StatusProsecution
    }
}

private fun complianceColor(value: Int): Color {
    return when {
        value >= 80 -> Color(0xFF16A34A)
        value >= 50 -> Color(0xFFB45309)
        else -> Color(0xFFC0392B)
    }
}

private fun districtColor(name: String): Color {
    return when {
        sameName(name, "Gasabo") -> Color(0xFF1F5BB4)
        sameName(name, "Kicukiro") -> Color(0xFF16A34A)
        sameName(name, "Nyarugenge") -> Color(0xFFE8650A)
        else -> Color(0xFF0D2B5E)
    }
}

private fun shortDateLabel(raw: String): String {
    return if (raw.length >= 10) {
        raw.substring(5, 10).replace('-', '/')
    } else {
        raw
    }
}

private fun formatInt(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(value)
}

private fun formatShort(value: Int): String {
    return when {
        value >= 1_000_000 -> "${value / 1_000_000}M"
        value >= 1_000 -> "${value / 1_000}k"
        else -> value.toString()
    }
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

private fun initials(fullName: String): String {
    val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return "?"
    if (words.size == 1) return words.first().take(1).uppercase(Locale.getDefault())
    return (words.first().take(1) + words.last().take(1)).uppercase(Locale.getDefault())
}

private fun nearestDataPointIndex(
    x: Float,
    width: Float,
    pointCount: Int,
): Int? {
    if (pointCount <= 0 || width <= 0f) return null
    if (pointCount == 1) return 0
    val step = width / (pointCount - 1).toFloat()
    return (x / step).roundToInt().coerceIn(0, pointCount - 1)
}

private fun VisitType.displayLabel(): String {
    return when (this) {
        VisitType.FIRST_VISIT -> "First Visit"
        VisitType.WARNING_VISIT -> "Warning Visit"
        VisitType.FOLLOW_UP -> "Follow-up"
        VisitType.COMPLIANCE_CHECK -> "Compliance Check"
    }
}

private const val FILTER_ALL_ID = "__all__"

private enum class AnalyticsPeriod(
    val key: String,
    val label: String,
    val windowDays: Int?,
    val chartWindowDays: Int,
) {
    TODAY(
        key = "today",
        label = "Today",
        windowDays = 1,
        chartWindowDays = 1,
    ),
    LAST_7_DAYS(
        key = "last_7",
        label = "Last 7d",
        windowDays = 7,
        chartWindowDays = 7,
    ),
    LAST_14_DAYS(
        key = "last_14",
        label = "Last 14d",
        windowDays = 14,
        chartWindowDays = 14,
    ),
    LAST_30_DAYS(
        key = "last_30",
        label = "Last 30d",
        windowDays = 30,
        chartWindowDays = 30,
    ),
    LAST_90_DAYS(
        key = "last_90",
        label = "Last 90d",
        windowDays = 90,
        chartWindowDays = 90,
    ),
    ALL_TIME(
        key = "all_time",
        label = "All time",
        windowDays = null,
        chartWindowDays = 30,
    ),
}

private data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val insights: DashboardInsights? = null,
    val inspectorCount: Int? = null,
    val managedUsers: List<ManagedUser> = emptyList(),
)

private data class MetricCardModel(
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val accentColor: Color,
    val icon: ImageVector,
    val iconBg: Color,
    val trend: String? = null,
    val trendUp: Boolean? = null,
)

private data class FilterOption(
    val id: String,
    val label: String,
)

private data class OfficerFilterOption(
    val id: String,
    val label: String,
)

private data class DistrictSummary(
    val name: String,
    val inspections: Int,
    val complianceRate: Int,
    val fines: Int,
)

private data class OfficerSummary(
    val name: String,
    val sector: String,
    val inspections: Int,
)

private data class BestFacilitySummary(
    val facilityId: String,
    val facilityName: String,
    val sector: String?,
    val complianceRate: Int,
    val inspectionCount: Int,
    val faultCount: Int,
)

private data class DecisionSlice(
    val label: String,
    val count: Int,
    val color: Color,
)
