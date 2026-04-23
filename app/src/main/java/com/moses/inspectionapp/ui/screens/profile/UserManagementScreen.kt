package com.moses.inspectionapp.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.location.LocationCatalog
import com.moses.inspectionapp.data.location.LocationCatalogStore
import com.moses.inspectionapp.data.model.UserRoleType
import com.moses.inspectionapp.data.model.displayLabel
import com.moses.inspectionapp.data.model.isManager
import com.moses.inspectionapp.data.model.parseUserRole
import com.moses.inspectionapp.data.remote.CreateUserManagementRequest
import com.moses.inspectionapp.data.remote.ManagedUser
import com.moses.inspectionapp.data.remote.UserManagementRepository
import com.moses.inspectionapp.data.validator.InputValidators
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.CountryPhoneField
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.PrimaryButtonTone
import com.moses.inspectionapp.ui.components.SectionLabel
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repository = AppContainer.repository
    val profile = repository.userProfile.collectAsState().value
    val roleType = parseUserRole(profile.role)
    val managementRepository = remember { UserManagementRepository() }
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<ManagedUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var actionUserId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    var locationCatalog by remember { mutableStateOf<LocationCatalog?>(null) }
    var currentPage by remember { mutableStateOf(0) }

    val allowedRoles = when (roleType) {
        UserRoleType.DISTRICT_MANAGER -> listOf("HSO")
        UserRoleType.CITY_MANAGER -> listOf("HSO", "DISTRICT_MANAGER")
        else -> emptyList()
    }
    val fallbackDistrictOptions = listOf("Gasabo", "Kicukiro", "Nyarugenge")
    val districtOptions = locationCatalog?.districtsSorted()
        ?.map { displayLocationName(it.districtName) }
        ?.ifEmpty { fallbackDistrictOptions }
        ?: fallbackDistrictOptions

    var fullNameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var sectorInput by remember { mutableStateOf(profile.sector) }
    var selectedRole by remember(allowedRoles) {
        mutableStateOf(allowedRoles.firstOrNull().orEmpty())
    }
    var selectedDistrict by remember(roleType, profile.district) {
        mutableStateOf(
            if (roleType == UserRoleType.CITY_MANAGER) {
                districtOptions.firstOrNull { it.equals(profile.district, ignoreCase = true) }
                    ?: districtOptions.first()
            } else {
                profile.district
            },
        )
    }
    val selectedDistrictItem = locationCatalog?.districts?.firstOrNull {
        namesEqual(displayLocationName(it.districtName), selectedDistrict)
    }
    val sectorOptions = selectedDistrictItem
        ?.let { district -> locationCatalog?.sectorsForDistrict(district.districtId).orEmpty() }
        .orEmpty()
        .map { displayLocationName(it.sectorName) }
    val pageSize = 5
    val totalPages = ((users.size + pageSize - 1) / pageSize).coerceAtLeast(1)
    val pagedUsers = users.drop(currentPage * pageSize).take(pageSize)
    val fullNameNormalized = InputValidators.normalizeName(fullNameInput)
    val emailNormalized = InputValidators.normalizeEmail(emailInput)
    val phoneNormalized = InputValidators.normalizePhone(phoneInput)
    val phoneE164 = if (phoneNormalized.isBlank()) "" else InputValidators.toE164(phoneNormalized, "RW")
    val fullNameValid = fullNameNormalized.isBlank() || InputValidators.isValidName(fullNameNormalized)
    val emailValid = emailNormalized.isBlank() || InputValidators.isValidEmail(emailNormalized)
    val phoneValid = phoneNormalized.isBlank() || InputValidators.isValidInternationalPhone(phoneE164)

    LaunchedEffect(Unit) {
        runCatching { LocationCatalogStore.load(context) }
            .onSuccess { locationCatalog = it }
    }

    LaunchedEffect(selectedDistrict, sectorOptions) {
        if (sectorOptions.isNotEmpty() && sectorOptions.none { namesEqual(it, sectorInput) }) {
            sectorInput = if (roleType == UserRoleType.DISTRICT_MANAGER) {
                sectorOptions.firstOrNull { namesEqual(it, profile.sector) } ?: sectorOptions.first()
            } else {
                sectorOptions.first()
            }
        }
    }

    LaunchedEffect(users.size, currentPage) {
        val lastPage = (totalPages - 1).coerceAtLeast(0)
        if (currentPage > lastPage) {
            currentPage = lastPage
        }
    }

    fun reloadUsers(showLoader: Boolean) {
        scope.launch {
            if (showLoader) {
                isLoading = true
            }
            errorMessage = null
            val result = managementRepository.getUsers()
            if (result.isSuccess) {
                users = result.getOrNull().orEmpty().sortedWith(
                    compareBy<ManagedUser> { roleSortOrder(it.role) }
                        .thenBy { it.fullName.lowercase() },
                )
            } else {
                errorMessage = result.exceptionOrNull()?.message
                    ?: context.getString(R.string.failed_load)
            }
            isLoading = false
        }
    }

    LaunchedEffect(profile.id, profile.role) {
        if (roleType.isManager()) {
            reloadUsers(showLoader = true)
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(
            title = if (showCreateForm) {
                if (roleType == UserRoleType.DISTRICT_MANAGER) "Add HSO" else stringResource(R.string.show_add_user_form)
            } else {
                stringResource(R.string.user_management_title)
            },
            onBack = {
                if (showCreateForm) {
                    showCreateForm = false
                } else {
                    onBack()
                }
            },
        )

        if (!roleType.isManager()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.user_management_access_denied),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                )
            }
            return@Column
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = AppColors.SteelBlue)
            }
        } else if (showCreateForm) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
                contentPadding = PaddingValues(top = Dimens.sectionGap, bottom = 96.dp),
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    item {
                        ErrorBanner(message = errorMessage.orEmpty())
                    }
                }
                if (!successMessage.isNullOrBlank()) {
                    item {
                        SuccessBanner(message = successMessage.orEmpty())
                    }
                }
                item {
                    CreateUserCard(
                        roleType = roleType,
                        selectedRole = selectedRole,
                        allowedRoles = allowedRoles,
                        selectedDistrict = selectedDistrict,
                        districtOptions = districtOptions,
                        sectorOptions = sectorOptions,
                        fullNameInput = fullNameInput,
                        emailInput = emailInput,
                        phoneInput = phoneInput,
                        fullNameValid = fullNameValid,
                        emailValid = emailValid,
                        phoneValid = phoneValid,
                        sectorInput = sectorInput,
                        isSaving = isSaving,
                        onRoleSelected = { selectedRole = it },
                        onDistrictSelected = { selectedDistrict = it },
                        onFullNameChanged = { fullNameInput = InputValidators.normalizeName(it) },
                        onEmailChanged = { emailInput = InputValidators.normalizeEmail(it) },
                        onPhoneChanged = { phoneInput = it },
                        onSectorChanged = { sectorInput = it },
                        onSave = {
                            if (isSaving) return@CreateUserCard

                            val trimmedName = InputValidators.normalizeName(fullNameInput).trim()
                            val trimmedEmail = InputValidators.normalizeEmail(emailInput)
                            val trimmedPhone = InputValidators.normalizePhone(phoneInput)
                            val phoneAsE164 = if (trimmedPhone.isBlank()) "" else InputValidators.toE164(trimmedPhone, "RW")
                            val trimmedSector = sectorInput.trim()
                            val createRole = if (roleType == UserRoleType.DISTRICT_MANAGER) {
                                "HSO"
                            } else {
                                selectedRole.ifBlank { "HSO" }
                            }
                            val createDistrict = if (roleType == UserRoleType.DISTRICT_MANAGER) {
                                profile.district
                            } else {
                                selectedDistrict
                            }

                            if (!InputValidators.isValidName(trimmedName)) {
                                errorMessage = context.getString(R.string.user_name_validation)
                                successMessage = null
                                return@CreateUserCard
                            }
                            if (!InputValidators.isValidEmail(trimmedEmail)) {
                                errorMessage = context.getString(R.string.user_email_validation)
                                successMessage = null
                                return@CreateUserCard
                            }
                            if (trimmedPhone.isNotBlank() && !InputValidators.isValidInternationalPhone(phoneAsE164)) {
                                errorMessage = context.getString(R.string.phone_invalid)
                                successMessage = null
                                return@CreateUserCard
                            }
                            if (trimmedSector.isBlank()) {
                                errorMessage = context.getString(R.string.user_sector_validation)
                                successMessage = null
                                return@CreateUserCard
                            }

                            isSaving = true
                            errorMessage = null
                            successMessage = null
                            scope.launch {
                                val result = managementRepository.createUser(
                                    CreateUserManagementRequest(
                                        fullName = trimmedName,
                                        email = trimmedEmail,
                                        role = createRole,
                                        district = createDistrict,
                                        sector = trimmedSector,
                                        phone = phoneAsE164.ifBlank { null },
                                        password = null,
                                    ),
                                )
                                isSaving = false
                                if (result.isSuccess) {
                                    val created = result.getOrNull()
                                    val baseMessage = context.getString(
                                        R.string.user_created_success,
                                        created?.fullName.orEmpty(),
                                    )
                                    val activationMessage = created?.activationCode
                                        ?.takeIf { it.isNotBlank() }
                                        ?.let { code ->
                                            context.getString(R.string.user_activation_code_hint, code)
                                        }
                                    val emailSentMessage = created
                                        ?.takeIf {
                                            it.activationStatus.equals("pending", ignoreCase = true) &&
                                                it.activationCode.isNullOrBlank()
                                        }
                                        ?.let {
                                            context.getString(
                                                R.string.user_activation_email_sent,
                                                it.email,
                                            )
                                        }
                                    successMessage = listOfNotNull(
                                        baseMessage,
                                        emailSentMessage,
                                        activationMessage,
                                    )
                                        .joinToString(" ")
                                    fullNameInput = ""
                                    emailInput = ""
                                    phoneInput = ""
                                    if (roleType == UserRoleType.DISTRICT_MANAGER) {
                                        sectorInput = sectorOptions.firstOrNull {
                                            namesEqual(it, profile.sector)
                                        } ?: sectorOptions.firstOrNull() ?: profile.sector
                                    }
                                    currentPage = 0
                                    showCreateForm = false
                                    reloadUsers(showLoader = false)
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message
                                        ?: context.getString(R.string.profile_save_failed)
                                }
                            }
                        },
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
                contentPadding = PaddingValues(top = Dimens.sectionGap, bottom = 96.dp),
            ) {
                item {
                    ScopeHeader(
                        roleType = roleType,
                        district = profile.district,
                        sector = profile.sector,
                        users = users,
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    item {
                        ErrorBanner(message = errorMessage.orEmpty())
                    }
                }
                if (!successMessage.isNullOrBlank()) {
                    item {
                        SuccessBanner(message = successMessage.orEmpty())
                    }
                }

                item {
                    PrimaryButton(
                        text = if (roleType == UserRoleType.DISTRICT_MANAGER) "Add HSO" else stringResource(R.string.show_add_user_form),
                        onClick = { showCreateForm = true },
                        tone = PrimaryButtonTone.Accent,
                        leadingIcon = Icons.Rounded.PersonAddAlt,
                    )
                }

                item {
                    SectionLabel(text = stringResource(R.string.managed_users_label))
                }

                if (users.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_managed_users),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                } else {
                    items(
                        count = pagedUsers.size,
                        key = { index -> pagedUsers[index].id },
                    ) { index ->
                        val managedUser = pagedUsers[index]
                        ManagedUserCardRedesigned(
                            managedUser = managedUser,
                            actionInProgress = actionUserId == managedUser.id,
                            canResendActivation = !managedUser.isActive &&
                                managedUser.role.equals("HSO", ignoreCase = true),
                            canDeactivate = managedUser.isActive,
                            onResendActivation = {
                                actionUserId = managedUser.id
                                errorMessage = null
                                successMessage = null
                                scope.launch {
                                    val result = managementRepository.resendActivation(managedUser.id)
                                    actionUserId = null
                                    if (result.isSuccess) {
                                        val updated = result.getOrNull()
                                        val code = updated?.activationCode
                                            ?.takeIf { it.isNotBlank() }
                                            ?.let { hint ->
                                                context.getString(
                                                    R.string.user_activation_code_hint,
                                                    hint,
                                                )
                                            }
                                            .orEmpty()
                                        successMessage = if (code.isBlank()) {
                                            context.getString(R.string.activation_resent_success)
                                        } else {
                                            "${context.getString(R.string.activation_resent_success)} $code"
                                        }
                                        reloadUsers(showLoader = false)
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message
                                            ?: context.getString(R.string.save_failed)
                                    }
                                }
                            },
                            onDeactivate = {
                                actionUserId = managedUser.id
                                errorMessage = null
                                successMessage = null
                                scope.launch {
                                    val result = managementRepository.deactivateUser(managedUser.id)
                                    actionUserId = null
                                    if (result.isSuccess) {
                                        successMessage = context.getString(
                                            R.string.user_deactivated_success,
                                            managedUser.fullName,
                                        )
                                        reloadUsers(showLoader = false)
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message
                                            ?: context.getString(R.string.save_failed)
                                    }
                                }
                            },
                            onDelete = {
                                actionUserId = managedUser.id
                                errorMessage = null
                                successMessage = null
                                scope.launch {
                                    val result = managementRepository.deleteUser(managedUser.id)
                                    actionUserId = null
                                    if (result.isSuccess) {
                                        successMessage = context.getString(
                                            R.string.user_deleted_success,
                                            managedUser.fullName,
                                        )
                                        currentPage = 0
                                        reloadUsers(showLoader = false)
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message
                                            ?: context.getString(R.string.save_failed)
                                    }
                                }
                            },
                        )
                    }
                    item {
                        ManagedUsersPaginationBar(
                            currentPage = currentPage,
                            totalPages = totalPages,
                            onPrevious = { currentPage = (currentPage - 1).coerceAtLeast(0) },
                            onNext = { currentPage = (currentPage + 1).coerceAtMost(totalPages - 1) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeHeader(
    roleType: UserRoleType,
    district: String,
    sector: String,
    users: List<ManagedUser>,
) {
    val activeUsers = users.count { it.isActive }
    val pendingUsers = users.count { !it.isActive }
    val scopeText = when (roleType) {
        UserRoleType.DISTRICT_MANAGER -> stringResource(R.string.scope_district_value, district)
        UserRoleType.CITY_MANAGER -> stringResource(R.string.scope_city_value)
        else -> stringResource(R.string.scope_sector_value, sector, district)
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
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = null,
                    tint = AppColors.TextOnDark,
                )
                Text(
                    text = stringResource(R.string.user_scope_title, roleType.displayLabel()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextOnDark,
                )
            }
            Text(
                text = scopeText,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextOnDarkMuted,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ScopeStatChip(
                    label = stringResource(R.string.total_users_short),
                    value = users.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                ScopeStatChip(
                    label = stringResource(R.string.active_users_short),
                    value = activeUsers.toString(),
                    modifier = Modifier.weight(1f),
                )
                ScopeStatChip(
                    label = stringResource(R.string.pending_users_short),
                    value = pendingUsers.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ScopeStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppColors.TextOnDark,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextOnDarkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CreateUserCard(
    roleType: UserRoleType,
    selectedRole: String,
    allowedRoles: List<String>,
    selectedDistrict: String,
    districtOptions: List<String>,
    sectorOptions: List<String>,
    fullNameInput: String,
    emailInput: String,
    phoneInput: String,
    fullNameValid: Boolean,
    emailValid: Boolean,
    phoneValid: Boolean,
    sectorInput: String,
    isSaving: Boolean,
    onRoleSelected: (String) -> Unit,
    onDistrictSelected: (String) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSectorChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StyledTextField(
                value = fullNameInput,
                onValueChange = onFullNameChanged,
                label = stringResource(R.string.full_name),
                leadingIcon = Icons.Rounded.PersonAddAlt,
                isError = fullNameInput.isNotBlank() && !fullNameValid,
                errorText = if (fullNameInput.isNotBlank() && !fullNameValid) {
                    stringResource(R.string.user_name_validation)
                } else {
                    null
                },
            )
            StyledTextField(
                value = emailInput,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.email),
                leadingIcon = Icons.Rounded.Email,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                ),
                isError = emailInput.isNotBlank() && !emailValid,
                errorText = if (emailInput.isNotBlank() && !emailValid) {
                    stringResource(R.string.user_email_validation)
                } else {
                    null
                },
            )
            CountryPhoneField(
                value = phoneInput,
                onValueChange = onPhoneChanged,
                label = stringResource(R.string.owner_phone),
                isError = phoneInput.isNotBlank() && !phoneValid,
                errorText = if (phoneInput.isNotBlank() && !phoneValid) {
                    stringResource(R.string.phone_invalid)
                } else {
                    null
                },
                defaultCountryIso = "RW",
            )

            DropdownTextField(
                value = roleTitle(if (roleType == UserRoleType.DISTRICT_MANAGER) "HSO" else selectedRole),
                label = stringResource(R.string.role_selection_title),
                options = allowedRoles,
                optionLabel = { roleTitle(it) },
                enabled = roleType == UserRoleType.CITY_MANAGER && allowedRoles.size > 1,
                leadingIcon = Icons.Rounded.Badge,
                onSelected = onRoleSelected,
            )

            if (roleType == UserRoleType.CITY_MANAGER) {
                DropdownTextField(
                    value = selectedDistrict,
                    label = stringResource(R.string.district),
                    options = districtOptions,
                    optionLabel = { it },
                    enabled = districtOptions.isNotEmpty(),
                    leadingIcon = Icons.Rounded.LocationOn,
                    onSelected = onDistrictSelected,
                )
            }

            DropdownTextField(
                value = sectorInput,
                label = stringResource(R.string.sector),
                options = sectorOptions.ifEmpty { listOfNotNull(sectorInput.takeIf { it.isNotBlank() }) },
                optionLabel = { it },
                enabled = sectorOptions.isNotEmpty(),
                leadingIcon = Icons.Rounded.LocationOn,
                onSelected = onSectorChanged,
            )

            PrimaryButton(
                text = stringResource(R.string.create_user_action),
                onClick = onSave,
                isLoading = isSaving,
                enabled = !isSaving,
                leadingIcon = Icons.Rounded.MailOutline,
                tone = PrimaryButtonTone.Accent,
            )
        }
    }
}

@Composable
private fun <T> DropdownTextField(
    value: String,
    label: String,
    options: List<T>,
    optionLabel: (T) -> String,
    enabled: Boolean,
    leadingIcon: ImageVector? = null,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val menuWidth = with(density) { textFieldSize.width.toDp() }
    val fieldHeight = with(density) {
        if (textFieldSize.height == 0) 56.dp else textFieldSize.height.toDp()
    }
    val canOpen = enabled && options.isNotEmpty()

    Box(modifier = Modifier.fillMaxWidth()) {
        StyledTextField(
            value = value,
            onValueChange = {},
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = if (canOpen) AppColors.TextSecondary else AppColors.TextHint,
                )
            },
            readOnly = true,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> textFieldSize = coordinates.size },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .clickable(enabled = canOpen) { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = if (textFieldSize.width == 0) Modifier else Modifier.width(menuWidth),
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(option),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
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

@Composable
private fun ManagedUserCard(
    managedUser: ManagedUser,
    actionInProgress: Boolean,
    canResendActivation: Boolean,
    canDeactivate: Boolean,
    onResendActivation: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    val roleDisplay = roleTitle(managedUser.role)
    val statusText = when {
        managedUser.isActive -> stringResource(R.string.user_status_active)
        managedUser.activationStatus.equals("pending", ignoreCase = true) ->
            stringResource(R.string.user_status_pending)
        else -> stringResource(R.string.user_status_inactive)
    }
    val statusColor = when {
        managedUser.isActive -> AppColors.AccentGreen
        managedUser.activationStatus.equals("pending", ignoreCase = true) -> AppColors.AccentOrange
        else -> AppColors.AccentRed
    }

    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    color = AppColors.SteelBlueTint,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials(managedUser.fullName),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = AppColors.SteelBlue,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = managedUser.fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${managedUser.district} • ${managedUser.sector}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                    )
                }
                Surface(
                    color = AppColors.SteelBlueTint,
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(
                        text = roleDisplay,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.SteelBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = managedUser.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (canResendActivation) {
                    TextButton(
                        onClick = onResendActivation,
                        enabled = !actionInProgress,
                    ) {
                        Text(text = stringResource(R.string.resend_activation))
                    }
                }
                if (canDeactivate) {
                    TextButton(
                        onClick = onDeactivate,
                        enabled = !actionInProgress,
                    ) {
                        Text(text = stringResource(R.string.deactivate))
                    }
                }
                TextButton(
                    onClick = onDelete,
                    enabled = !actionInProgress,
                ) {
                    Text(
                        text = stringResource(R.string.remove),
                        color = AppColors.AccentRed,
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagedUserCardRedesigned(
    managedUser: ManagedUser,
    actionInProgress: Boolean,
    canResendActivation: Boolean,
    canDeactivate: Boolean,
    onResendActivation: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit,
) {
    val roleDisplay = roleTitle(managedUser.role)
    val roleColor = when (roleDisplay) {
        "District Manager" -> Color(0xFF1F5BB4)
        "HSO" -> Color(0xFF7C3AED)
        "City Manager" -> Color(0xFF0D2B5E)
        else -> Color(0xFF0D2B5E)
    }
    val statusText = when {
        managedUser.isActive -> stringResource(R.string.user_status_active)
        managedUser.activationStatus.equals("pending", ignoreCase = true) ->
            stringResource(R.string.user_status_pending)
        else -> stringResource(R.string.user_status_inactive)
    }
    val statusColor = when {
        managedUser.isActive -> Color(0xFF16A34A)
        managedUser.activationStatus.equals("pending", ignoreCase = true) -> Color(0xFFB45309)
        else -> Color(0xFFC0392B)
    }
    val statusBg = when {
        managedUser.isActive -> Color(0xFFDCFCE7)
        managedUser.activationStatus.equals("pending", ignoreCase = true) -> Color(0xFFFEF3C7)
        else -> Color(0xFFFDECEB)
    }

    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = AppColors.NavyDark,
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials(managedUser.fullName),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = managedUser.fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "${managedUser.district} - ${managedUser.sector}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Surface(
                    color = roleColor.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(
                        text = roleDisplay,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = roleColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = AppColors.BorderLight,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape),
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = statusColor,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Email,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = managedUser.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (canResendActivation) {
                    UserActionButton(
                        text = stringResource(R.string.resend_activation),
                        icon = Icons.Rounded.Refresh,
                        onClick = onResendActivation,
                        enabled = !actionInProgress,
                        contentColor = AppColors.SteelBlue,
                        borderColor = AppColors.BorderLight,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (canDeactivate) {
                    UserActionButton(
                        text = stringResource(R.string.deactivate),
                        icon = Icons.Rounded.Block,
                        onClick = onDeactivate,
                        enabled = !actionInProgress,
                        contentColor = AppColors.TextSecondary,
                        borderColor = AppColors.BorderLight,
                        modifier = Modifier.weight(1f),
                    )
                }
                UserActionButton(
                    text = stringResource(R.string.remove),
                    icon = Icons.Rounded.DeleteOutline,
                    onClick = onDelete,
                    enabled = !actionInProgress,
                    contentColor = Color(0xFFC0392B),
                    borderColor = Color(0xFFFDECEB),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun UserActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    contentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = AppColors.TextHint,
        ),
        modifier = modifier.height(38.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ManagedUsersPaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    if (totalPages <= 1) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaginationIconButton(
            enabled = currentPage > 0,
            icon = Icons.Rounded.ChevronLeft,
            contentDescription = "Previous",
            onClick = onPrevious,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = AppColors.TextSecondary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        PaginationIconButton(
            enabled = currentPage < totalPages - 1,
            icon = Icons.Rounded.ChevronRight,
            contentDescription = "Next",
            onClick = onNext,
        )
    }
}

@Composable
private fun PaginationIconButton(
    enabled: Boolean,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(36.dp)
            .background(
                if (enabled) AppColors.SteelBlueTint else Color(0xFFF1F5F9),
                RoundedCornerShape(10.dp),
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) AppColors.SteelBlue else AppColors.TextHint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        color = AppColors.StatusImmediateBg,
        border = BorderStroke(1.dp, AppColors.StatusImmediate),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = AppColors.StatusImmediate,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun SuccessBanner(message: String) {
    Surface(
        color = AppColors.AccentGreenBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.AccentGreen,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

private fun roleSortOrder(role: String): Int {
    return when (role.uppercase(Locale.getDefault())) {
        "CITY_MANAGER" -> 0
        "DISTRICT_MANAGER" -> 1
        "HSO" -> 2
        else -> 3
    }
}

private fun roleTitle(role: String): String {
    return when (role.uppercase(Locale.getDefault())) {
        "CITY_MANAGER" -> "City Manager"
        "DISTRICT_MANAGER" -> "District Manager"
        "HSO" -> "HSO"
        else -> role
    }
}

private fun namesEqual(left: String?, right: String?): Boolean {
    return left.orEmpty().trim().equals(right.orEmpty().trim(), ignoreCase = true)
}

private fun displayLocationName(value: String): String {
    return value.trim()
        .lowercase(Locale.getDefault())
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
}

private fun initials(fullName: String): String {
    val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return "?"
    if (words.size == 1) return words.first().take(1).uppercase()
    return (words.first().take(1) + words.last().take(1)).uppercase()
}
