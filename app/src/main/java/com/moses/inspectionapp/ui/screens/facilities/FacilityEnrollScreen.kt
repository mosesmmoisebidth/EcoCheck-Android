package com.moses.inspectionapp.ui.screens.facilities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.draw.drawBehind
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.location.LocationCatalog
import com.moses.inspectionapp.data.location.LocationCatalogStore
import com.moses.inspectionapp.data.model.FacilityDraft
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.data.validator.InputValidators
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.CountryPhoneField
import com.moses.inspectionapp.ui.components.ErrorState
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.theme.InputShape
import com.moses.inspectionapp.ui.util.copyUriToPhotoFile
import com.moses.inspectionapp.ui.util.getCurrentLocation
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import com.moses.inspectionapp.ui.util.NotificationHelper
import com.moses.inspectionapp.data.sync.SyncManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import android.location.Geocoder
import android.util.Log
import java.util.Locale

@Composable
fun FacilityEnrollScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    onCapturePhoto: () -> Unit,
) {
    val repository = AppContainer.repository
    val user = repository.userProfile.collectAsState().value
    val location = DraftStore.facilityLocation.collectAsState().value
    val photoPath = DraftStore.facilityPhotoPath.collectAsState().value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            scope.launch {
                DraftStore.facilityLocation.value = getCurrentLocation(context)
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = copyUriToPhotoFile(context, uri, "facility")
                if (path != null) {
                    DraftStore.facilityPhotoPath.value = path
                }
            }
        }
    }
    val (name, setName) = rememberSaveable { mutableStateOf("") }
    val (tin, setTin) = rememberSaveable { mutableStateOf("") }
    val (ownerName, setOwnerName) = rememberSaveable { mutableStateOf("") }
    val (ownerPhone, setOwnerPhone) = rememberSaveable { mutableStateOf("") }
    val (ownerEmail, setOwnerEmail) = rememberSaveable { mutableStateOf("") }
    var selectedDistrictId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedSectorId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedCellId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedVillageId by rememberSaveable { mutableStateOf<Int?>(null) }
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val role = user.role.uppercase()
    val canPickDistrict = role != "HSO" && role != "DISTRICT_MANAGER"
    val canPickSector = role != "HSO"
    val catalogState = produceState<LocationCatalog?>(initialValue = null, key1 = context) {
        value = LocationCatalogStore.load(context)
    }
    val catalog = catalogState.value

    val allDistricts = catalog?.districtsSorted().orEmpty()
    val userDistrictMatch = allDistricts.firstOrNull {
        it.districtName.equals(user.district, ignoreCase = true)
    }
    val scopedDistricts = if (canPickDistrict) {
        allDistricts
    } else {
        listOfNotNull(userDistrictMatch).ifEmpty { allDistricts }
    }

    val selectedDistrict = scopedDistricts.firstOrNull { it.districtId == selectedDistrictId }
        ?: scopedDistricts.firstOrNull()
    val districtName = selectedDistrict?.districtName ?: user.district

    val sectorOptions = if (selectedDistrict?.districtId != null) {
        catalog?.sectorsForDistrict(selectedDistrict.districtId).orEmpty()
    } else {
        emptyList()
    }
    val userSectorMatch = sectorOptions.firstOrNull {
        it.sectorName.equals(user.sector, ignoreCase = true)
    }
    val selectedSector = sectorOptions.firstOrNull { it.sectorId == selectedSectorId }
        ?: if (!canPickSector) userSectorMatch else null
        ?: sectorOptions.firstOrNull()
    val sectorName = selectedSector?.sectorName ?: user.sector

    val cellOptions = if (selectedSector?.sectorId != null) {
        catalog?.cellsForSector(selectedSector.sectorId).orEmpty()
    } else {
        emptyList()
    }
    val selectedCell = cellOptions.firstOrNull { it.cellId == selectedCellId }
        ?: cellOptions.firstOrNull()
    val cellName = selectedCell?.cellName.orEmpty()

    val villageOptions = if (selectedCell?.cellId != null) {
        catalog?.villagesForCell(selectedCell.cellId).orEmpty()
    } else {
        emptyList()
    }
    val selectedVillage = villageOptions.firstOrNull { it.villageId == selectedVillageId }
        ?: villageOptions.firstOrNull()
    val villageName = selectedVillage?.villageName.orEmpty()

    var locationLabel by remember { mutableStateOf<String?>(null) }
    var locationLookup by remember { mutableStateOf(false) }

    LaunchedEffect(catalog, role) {
        if (catalog == null || scopedDistricts.isEmpty()) {
            return@LaunchedEffect
        }
        if (selectedDistrictId == null) {
            selectedDistrictId = userDistrictMatch?.districtId ?: scopedDistricts.first().districtId
        }
    }

    LaunchedEffect(selectedDistrictId, catalog, role) {
        if (catalog == null) return@LaunchedEffect
        val districtId = selectedDistrictId ?: return@LaunchedEffect
        val sectors = catalog.sectorsForDistrict(districtId)
        if (sectors.isEmpty()) {
            selectedSectorId = null
            return@LaunchedEffect
        }
        val fallbackSectorId = if (!canPickSector) {
            userSectorMatch?.sectorId
        } else {
            null
        } ?: sectors.first().sectorId
        if (selectedSectorId !in sectors.map { it.sectorId }) {
            selectedSectorId = fallbackSectorId
        }
    }

    LaunchedEffect(selectedSectorId, catalog) {
        if (catalog == null) return@LaunchedEffect
        val sectorId = selectedSectorId ?: return@LaunchedEffect
        val cells = catalog.cellsForSector(sectorId)
        if (cells.isEmpty()) {
            selectedCellId = null
            return@LaunchedEffect
        }
        if (selectedCellId !in cells.map { it.cellId }) {
            selectedCellId = cells.first().cellId
        }
    }

    LaunchedEffect(selectedCellId, catalog) {
        if (catalog == null) return@LaunchedEffect
        val cellId = selectedCellId ?: return@LaunchedEffect
        val villages = catalog.villagesForCell(cellId)
        if (villages.isEmpty()) {
            selectedVillageId = null
            return@LaunchedEffect
        }
        if (selectedVillageId !in villages.map { it.villageId }) {
            selectedVillageId = villages.first().villageId
        }
    }

    LaunchedEffect(location) {
        if (location == null) {
            locationLabel = null
            locationLookup = false
            return@LaunchedEffect
        }
        locationLookup = true
        val address = withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (!Geocoder.isPresent()) {
                    return@runCatching null
                }
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()
            }.getOrNull()
        }
        locationLabel = address?.getAddressLine(0)
            ?: listOfNotNull(
                address?.subLocality,
                address?.locality,
                address?.adminArea,
            ).joinToString(", ").ifBlank { null }
        locationLookup = false
    }

    val tinNormalized = InputValidators.normalizeTin(tin)
    val ownerNameNormalized = InputValidators.normalizeName(ownerName)
    val phoneNormalized = InputValidators.normalizePhone(ownerPhone)
    val phoneE164 = InputValidators.toE164(phoneNormalized, fallbackRegionIso = "RW")
    val ownerEmailNormalized = InputValidators.normalizeEmail(ownerEmail)
    val tinValid = tinNormalized.isNotBlank() && InputValidators.isValidTin(tinNormalized)
    val phoneValid = phoneE164.isNotBlank() && InputValidators.isValidInternationalPhone(phoneE164)
    val ownerNameValid = ownerNameNormalized.isNotBlank() && InputValidators.isValidName(ownerNameNormalized)
    val ownerEmailValid = ownerEmailNormalized.isBlank() || InputValidators.isValidEmail(ownerEmailNormalized)
    val requiredValid = name.isNotBlank() &&
        ownerNameValid &&
        districtName.isNotBlank() &&
        sectorName.isNotBlank() &&
        cellName.isNotBlank() &&
        villageName.isNotBlank()
    val canSave = tinValid && phoneValid && ownerEmailValid && requiredValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = stringResource(R.string.enroll_facility_title),
                onBack = onCancel,
            )
            OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = Dimens.cardMaxWidth)
                    .align(Alignment.CenterHorizontally)
                    .mouseWheelScroll(scrollState)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            ) {
                if (isOffline) {
                    Text(
                        text = stringResource(R.string.saving_locally),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextSecondary,
                    )
                    LinearProgressIndicator(
                        color = AppColors.SteelBlue,
                        trackColor = AppColors.SteelBlueTint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(AppColors.SteelBlueTint, RoundedCornerShape(50)),
                    )
                }

                SectionHeader(title = stringResource(R.string.location))
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        if (canPickDistrict) {
                            LocationDropdownField(
                                value = districtName,
                                label = stringResource(R.string.district),
                                leadingIcon = Icons.Rounded.LocationCity,
                                options = scopedDistricts,
                                optionLabel = { it.districtName },
                                enabled = scopedDistricts.isNotEmpty(),
                                onSelected = {
                                    selectedDistrictId = it.districtId
                                    selectedSectorId = null
                                    selectedCellId = null
                                    selectedVillageId = null
                                },
                            )
                        } else {
                            StyledTextField(
                                value = districtName,
                                onValueChange = {},
                                label = stringResource(R.string.district),
                                leadingIcon = Icons.Rounded.LocationCity,
                                readOnly = true,
                            )
                        }
                        if (canPickSector) {
                            LocationDropdownField(
                                value = sectorName,
                                label = stringResource(R.string.sector),
                                leadingIcon = Icons.Rounded.Map,
                                options = sectorOptions,
                                optionLabel = { it.sectorName },
                                enabled = sectorOptions.isNotEmpty(),
                                onSelected = {
                                    selectedSectorId = it.sectorId
                                    selectedCellId = null
                                    selectedVillageId = null
                                },
                            )
                        } else {
                            StyledTextField(
                                value = sectorName,
                                onValueChange = {},
                                label = stringResource(R.string.sector),
                                leadingIcon = Icons.Rounded.Map,
                                readOnly = true,
                            )
                        }
                        LocationDropdownField(
                            value = cellName,
                            label = stringResource(R.string.cell),
                            leadingIcon = Icons.Rounded.GridView,
                            placeholder = stringResource(R.string.select_cell),
                            options = cellOptions,
                            optionLabel = { it.cellName },
                            enabled = cellOptions.isNotEmpty(),
                            onSelected = {
                                selectedCellId = it.cellId
                                selectedVillageId = null
                            },
                        )
                        LocationDropdownField(
                            value = villageName,
                            label = stringResource(R.string.village),
                            leadingIcon = Icons.Rounded.Home,
                            placeholder = stringResource(R.string.select_village),
                            options = villageOptions,
                            optionLabel = { it.villageName },
                            enabled = villageOptions.isNotEmpty(),
                            onSelected = { selectedVillageId = it.villageId },
                        )
                    }
                }

                SectionHeader(title = stringResource(R.string.facility_details))
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        StyledTextField(
                            value = name,
                            onValueChange = setName,
                            label = stringResource(R.string.establishment_name),
                            leadingIcon = Icons.Rounded.Storefront,
                        )
                        StyledTextField(
                            value = tin,
                            onValueChange = { setTin(InputValidators.normalizeTin(it)) },
                            label = stringResource(R.string.tin),
                            leadingIcon = Icons.Rounded.Badge,
                            isError = tin.isNotBlank() && !tinValid,
                            errorText = if (tin.isNotBlank() && !tinValid) {
                                stringResource(R.string.tin_invalid)
                            } else {
                                null
                            },
                        )
                    }
                }

                SectionHeader(title = stringResource(R.string.owner_manager))
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        StyledTextField(
                            value = ownerName,
                            onValueChange = { setOwnerName(InputValidators.normalizeName(it)) },
                            label = stringResource(R.string.owner_name),
                            leadingIcon = Icons.Rounded.Person,
                            isError = ownerName.isNotBlank() && !ownerNameValid,
                            errorText = if (ownerName.isNotBlank() && !ownerNameValid) {
                                stringResource(R.string.user_name_validation)
                            } else {
                                null
                            },
                        )
                        CountryPhoneField(
                            value = ownerPhone,
                            onValueChange = setOwnerPhone,
                            label = stringResource(R.string.owner_phone),
                            isError = ownerPhone.isNotBlank() && !phoneValid,
                            errorText = if (ownerPhone.isNotBlank() && !phoneValid) {
                                stringResource(R.string.phone_invalid)
                            } else {
                                null
                            },
                            defaultCountryIso = "RW",
                        )
                        StyledTextField(
                            value = ownerEmail,
                            onValueChange = { setOwnerEmail(InputValidators.normalizeEmail(it)) },
                            label = stringResource(R.string.owner_email_optional),
                            leadingIcon = Icons.Rounded.Email,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                            ),
                            isError = ownerEmail.isNotBlank() && !ownerEmailValid,
                            errorText = if (ownerEmail.isNotBlank() && !ownerEmailValid) {
                                stringResource(R.string.user_email_validation)
                            } else {
                                null
                            },
                            trailingIcon = {
                                Surface(
                                    color = AppColors.SteelBlueTint,
                                    shape = RoundedCornerShape(50.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.optional),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppColors.SteelBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            },
                        )
                    }
                }

                SectionHeader(title = stringResource(R.string.geo_location))
                Surface(
                    color = AppColors.SteelBlueTint,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.BorderLight, RoundedCornerShape(12.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        Icon(imageVector = Icons.Rounded.GpsFixed, contentDescription = null, tint = AppColors.SteelBlue)
                        Column(modifier = Modifier.weight(1f)) {
                            if (location != null) {
                                Text(
                                    text = stringResource(R.string.gps_captured),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.TextPrimary,
                                )
                                when {
                                    locationLookup -> {
                                        Text(
                                            text = stringResource(R.string.location_lookup),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.TextSecondary,
                                        )
                                    }
                                    !locationLabel.isNullOrBlank() -> {
                                        Text(
                                            text = locationLabel.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppColors.TextPrimary,
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = stringResource(R.string.location_unknown),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.TextSecondary,
                                        )
                                    }
                                }
                                Text(
                                    text = "${location.latitude}, ${location.longitude}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextSecondary,
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.capture_gps),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.TextPrimary,
                                )
                                Text(
                                    text = stringResource(R.string.capture_gps_hint),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextSecondary,
                                )
                            }
                        }
                        if (location != null) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = AppColors.AccentGreen,
                            )
                        } else {
                            SecondaryButton(
                                text = stringResource(R.string.use_gps),
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        scope.launch {
                                            DraftStore.facilityLocation.value = getCurrentLocation(context)
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                },
                                fullWidth = false,
                                modifier = Modifier.height(44.dp),
                            )
                        }
                    }
                }

                SectionHeader(title = stringResource(R.string.photo))
                if (photoPath == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                                )
                                drawRoundRect(
                                    color = AppColors.SteelBlue,
                                    size = size,
                                    style = stroke,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                                )
                            }
                            .padding(Dimens.cardPadding)
                            .background(Color.Transparent)
                            .clickable { onCapturePhoto() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                tint = AppColors.SteelBlue,
                                modifier = Modifier.size(40.dp),
                            )
                            Text(
                                text = stringResource(R.string.tap_add_photo),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextSecondary,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                SecondaryButton(
                                    text = stringResource(R.string.capture),
                                    onClick = onCapturePhoto,
                                    fullWidth = false,
                                    modifier = Modifier.weight(1f),
                                )
                                SecondaryButton(
                                    text = stringResource(R.string.upload),
                                    onClick = { galleryLauncher.launch("image/*") },
                                    fullWidth = false,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = File(photoPath),
                                contentDescription = stringResource(R.string.facility_photo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(AppColors.CardSurface, InputShape),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .background(AppColors.SteelBlue, RoundedCornerShape(18.dp))
                                    .padding(10.dp)
                                    .clickable { onCapturePhoto() },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SecondaryButton(
                                text = stringResource(R.string.capture),
                                onClick = onCapturePhoto,
                                fullWidth = false,
                                modifier = Modifier.weight(1f),
                            )
                            SecondaryButton(
                                text = stringResource(R.string.upload),
                                onClick = { galleryLauncher.launch("image/*") },
                                fullWidth = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    ErrorState(
                        title = stringResource(R.string.save_failed),
                        message = errorMessage ?: "",
                    )
                }
            }
        }

        Surface(
            color = AppColors.CardSurface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = Dimens.cardMaxWidth)
                        .padding(horizontal = Dimens.screenPadding, vertical = Dimens.medium),
                ) {
                    if (isSaving) {
                        LinearProgressIndicator(
                            color = AppColors.SteelBlue,
                            trackColor = AppColors.SteelBlueTint,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    PrimaryButton(
                        text = stringResource(R.string.save_facility),
                        onClick = {
                            if (isSaving) return@PrimaryButton
                            scope.launch {
                                isSaving = true
                                try {
                                    setErrorMessage(null)
                                    val existing = repository.findFacilityByTin(tinNormalized)
                                    if (existing != null) {
                                        setErrorMessage(context.getString(R.string.tin_exists))
                                        return@launch
                                    }
                                    val draft = FacilityDraft(
                                        name = name.trim(),
                                        tin = tinNormalized,
                                        ownerName = ownerNameNormalized,
                                        ownerPhone = phoneE164,
                                        ownerEmail = ownerEmailNormalized,
                                        district = districtName.trim(),
                                        sector = sectorName.trim(),
                                        cell = cellName.trim(),
                                        village = villageName.trim(),
                                        latitude = location?.latitude,
                                        longitude = location?.longitude,
                                        photoPath = photoPath,
                                    )
                                    val facilityId = runCatching { repository.saveFacility(draft) }
                                        .onFailure { error ->
                                            Log.e("FacilityEnroll", "Failed to save facility", error)
                                            setErrorMessage(error.message ?: context.getString(R.string.save_failed))
                                        }
                                        .getOrNull()
                                    if (facilityId == null) {
                                        return@launch
                                    }
                                    DraftStore.selectedFacilityId.value = facilityId
                                    DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                        facilityId = facilityId,
                                        facilityName = draft.name,
                                    )
                                    DraftStore.facilityLocation.value = null
                                    DraftStore.facilityPhotoPath.value = null
                                    setErrorMessage(null)
                                    NotificationHelper.notifyFacilitySaved(context, draft.name, isOffline)
                                    if (!isOffline) {
                                        SyncManager.enqueue(context)
                                    }
                                    onSaved()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = canSave,
                        isLoading = isSaving,
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> LocationDropdownField(
    value: String,
    label: String,
    leadingIcon: ImageVector,
    placeholder: String? = null,
    options: List<T>,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onSelected: (T) -> Unit,
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
    Box(modifier = Modifier.fillMaxWidth()) {
        StyledTextField(
            value = value,
            onValueChange = {},
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = if (enabled) AppColors.TextSecondary else AppColors.TextHint,
                )
            },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> textFieldSize = coordinates.size },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .clickable(enabled = enabled) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = menuModifier,
        ) {
            val menuScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .heightIn(max = 260.dp)
                    .verticalScroll(menuScroll),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
