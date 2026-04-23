package com.moses.inspectionapp.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.isManager
import com.moses.inspectionapp.data.model.parseUserRole
import com.moses.inspectionapp.data.remote.AuthRepository
import com.moses.inspectionapp.data.validator.InputValidators
import com.moses.inspectionapp.ui.components.ClickableCard
import com.moses.inspectionapp.ui.components.InfoRow
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.PrimaryButtonTone
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.theme.LocalAppSpacing
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onSettings: () -> Unit = {},
    onManageUsers: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val repository = AppContainer.repository
    val user = repository.userProfile.collectAsState().value
    val roleType = parseUserRole(user.role)
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var isLoggingOut by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var emailInput by remember { mutableStateOf(user.email) }
    var emailPassword by remember { mutableStateOf("") }
    var emailMessage by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isChangingEmail by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMessage by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var emailPasswordVisible by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val profileSavedText = stringResource(R.string.profile_saved)
    val profileSaveFailedText = stringResource(R.string.profile_save_failed)
    val passwordMismatchText = stringResource(R.string.passwords_do_not_match)
    val passwordUpdatedText = stringResource(R.string.password_updated)
    val emailUpdatedText = stringResource(R.string.email_updated)
    val passwordMinLengthText = stringResource(R.string.password_min_length)
    val invalidNameText = stringResource(R.string.user_name_validation)
    val invalidEmailText = stringResource(R.string.user_email_validation)
    val scrollState = rememberScrollState()
    val sp = LocalAppSpacing.current

    LaunchedEffect(user.fullName) {
        val trimmed = user.fullName.trim()
        if (trimmed.isBlank()) {
            firstName = ""
            lastName = ""
            return@LaunchedEffect
        }
        val parts = trimmed.split(Regex("\\s+"))
        firstName = parts.firstOrNull().orEmpty()
        lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
    }

    LaunchedEffect(user.email) {
        emailInput = user.email
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = Dimens.cardMaxWidth)
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.NavyDark)
                    .padding(top = Dimens.sectionGap * 2f, bottom = Dimens.large),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(sp.avatarSize),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.fullName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (sp.avatarSize.value * 0.4f).sp,
                            ),
                            color = AppColors.SteelBlue,
                        )
                    }
                }
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppColors.TextOnDark,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(50.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Text(
                        text = user.role,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextOnDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = "${user.sector} | ${user.district}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextOnDarkMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.screenPadding,
                        end = Dimens.screenPadding,
                        top = Dimens.itemGap,
                        bottom = Dimens.sectionGap,
                    )
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            ) {
                Surface(color = AppColors.CardSurface, shape = RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                    Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                        InfoRow(icon = Icons.Rounded.Person, label = stringResource(R.string.full_name), value = user.fullName)
                        InfoRow(icon = Icons.Rounded.Email, label = stringResource(R.string.email), value = user.email)
                        InfoRow(icon = Icons.Rounded.LocationCity, label = stringResource(R.string.district), value = user.district)
                        InfoRow(icon = Icons.Rounded.Map, label = stringResource(R.string.sector), value = user.sector)
                        InfoRow(icon = Icons.Rounded.Badge, label = stringResource(R.string.employee_id), value = user.id)
                    }
                }

                SectionHeader(title = stringResource(R.string.profile_details))
                Surface(color = AppColors.CardSurface, shape = RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                    Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfilePhotoRow(
                            initials = user.fullName.firstOrNull()?.uppercase()?.toString() ?: "?",
                            avatarSize = sp.avatarSize,
                        )
                        StyledTextField(
                            value = firstName,
                            onValueChange = { firstName = InputValidators.normalizeName(it) },
                            label = stringResource(R.string.first_name),
                            isError = firstName.isNotBlank() && !InputValidators.isValidName(firstName),
                            errorText = if (firstName.isNotBlank() && !InputValidators.isValidName(firstName)) {
                                invalidNameText
                            } else {
                                null
                            },
                        )
                        StyledTextField(
                            value = lastName,
                            onValueChange = { lastName = InputValidators.normalizeName(it) },
                            label = stringResource(R.string.last_name),
                            isError = lastName.isNotBlank() && !InputValidators.isValidName(lastName),
                            errorText = if (lastName.isNotBlank() && !InputValidators.isValidName(lastName)) {
                                invalidNameText
                            } else {
                                null
                            },
                        )
                        PrimaryButton(
                            text = stringResource(R.string.save_profile),
                            onClick = {
                                if (isSaving) return@PrimaryButton
                                isSaving = true
                                saveMessage = null
                                saveError = null
                                scope.launch {
                                    if (!InputValidators.isValidName(firstName) || !InputValidators.isValidName(lastName)) {
                                        isSaving = false
                                        saveError = invalidNameText
                                        return@launch
                                    }
                                    val result = authRepository.updateProfile(
                                        firstName.trim(),
                                        lastName.trim(),
                                    )
                                    isSaving = false
                                    if (result.isSuccess) {
                                        saveMessage = profileSavedText
                                    } else {
                                        saveError = result.exceptionOrNull()?.message
                                            ?: profileSaveFailedText
                                    }
                                }
                            },
                            isLoading = isSaving,
                            enabled = firstName.isNotBlank() && lastName.isNotBlank() && !isSaving,
                        )
                        if (!saveMessage.isNullOrBlank()) {
                            Text(
                                text = saveMessage.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.AccentGreen,
                            )
                        }
                        if (!saveError.isNullOrBlank()) {
                            Text(
                                text = saveError.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.AccentRed,
                            )
                        }
                    }
                }

                SectionHeader(title = stringResource(R.string.change_email))
                Surface(color = AppColors.CardSurface, shape = RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                    Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StyledTextField(
                        value = emailInput,
                        onValueChange = { emailInput = InputValidators.normalizeEmail(it) },
                        label = stringResource(R.string.new_email),
                        leadingIcon = Icons.Rounded.Email,
                        isError = emailInput.isNotBlank() && !InputValidators.isValidEmail(emailInput),
                        errorText = if (emailInput.isNotBlank() && !InputValidators.isValidEmail(emailInput)) {
                            invalidEmailText
                        } else {
                            null
                        },
                    )
                    StyledTextField(
                        value = emailPassword,
                        onValueChange = { emailPassword = it },
                        label = stringResource(R.string.current_password),
                        leadingIcon = Icons.Rounded.Lock,
                        trailingIcon = {
                            Icon(
                                imageVector = if (emailPasswordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable { emailPasswordVisible = !emailPasswordVisible },
                            )
                        },
                        visualTransformation = if (emailPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    )
                    PrimaryButton(
                        text = stringResource(R.string.update_email),
                        onClick = {
                            if (isChangingEmail) return@PrimaryButton
                            isChangingEmail = true
                            emailMessage = null
                            emailError = null
                            scope.launch {
                                if (!InputValidators.isValidEmail(emailInput)) {
                                    isChangingEmail = false
                                    emailError = invalidEmailText
                                    return@launch
                                }
                                val result = authRepository.changeEmail(
                                    email = emailInput.trim(),
                                    password = emailPassword,
                                )
                                isChangingEmail = false
                                if (result.isSuccess) {
                                    emailMessage = emailUpdatedText
                                    emailPassword = ""
                                    emailPasswordVisible = false
                                } else {
                                    emailError = result.exceptionOrNull()?.message
                                }
                            }
                        },
                        isLoading = isChangingEmail,
                        enabled = InputValidators.isValidEmail(emailInput) && emailPassword.isNotBlank() && !isChangingEmail,
                    )
                    if (!emailMessage.isNullOrBlank()) {
                        Text(
                            text = emailMessage.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.AccentGreen,
                        )
                    }
                    if (!emailError.isNullOrBlank()) {
                        Text(
                            text = emailError.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.AccentRed,
                        )
                    }
                    }
                }

                SectionHeader(title = stringResource(R.string.change_password))
                Surface(color = AppColors.CardSurface, shape = RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                    Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StyledTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = stringResource(R.string.current_password),
                        leadingIcon = Icons.Rounded.Lock,
                        trailingIcon = {
                            Icon(
                                imageVector = if (currentPasswordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable { currentPasswordVisible = !currentPasswordVisible },
                            )
                        },
                        visualTransformation = if (currentPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    )
                    StyledTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = stringResource(R.string.new_password),
                        leadingIcon = Icons.Rounded.Lock,
                        trailingIcon = {
                            Icon(
                                imageVector = if (newPasswordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable { newPasswordVisible = !newPasswordVisible },
                            )
                        },
                        visualTransformation = if (newPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = newPassword.isNotEmpty() && newPassword.length < 6,
                        errorText = if (newPassword.isNotEmpty() && newPassword.length < 6) {
                            passwordMinLengthText
                        } else {
                            null
                        },
                    )
                    StyledTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = stringResource(R.string.confirm_password),
                        leadingIcon = Icons.Rounded.Lock,
                        trailingIcon = {
                            Icon(
                                imageVector = if (confirmPasswordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable { confirmPasswordVisible = !confirmPasswordVisible },
                            )
                        },
                        visualTransformation = if (confirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                        errorText = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                            passwordMismatchText
                        } else {
                            null
                        },
                    )
                    PrimaryButton(
                        text = stringResource(R.string.update_password),
                        onClick = {
                            if (isChangingPassword) return@PrimaryButton
                            passwordMessage = null
                            passwordError = null
                            if (newPassword.length < 6) {
                                passwordError = passwordMinLengthText
                                return@PrimaryButton
                            }
                            if (newPassword != confirmPassword) {
                                passwordError = passwordMismatchText
                                return@PrimaryButton
                            }
                            isChangingPassword = true
                            scope.launch {
                                val result = authRepository.changePassword(
                                    currentPassword = currentPassword,
                                    newPassword = newPassword,
                                )
                                isChangingPassword = false
                                if (result.isSuccess) {
                                    passwordMessage = passwordUpdatedText
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    currentPasswordVisible = false
                                    newPasswordVisible = false
                                    confirmPasswordVisible = false
                                } else {
                                    passwordError = result.exceptionOrNull()?.message
                                }
                            }
                        },
                        isLoading = isChangingPassword,
                        enabled = currentPassword.isNotBlank() &&
                            newPassword.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            !isChangingPassword,
                    )
                    if (!passwordMessage.isNullOrBlank()) {
                        Text(
                            text = passwordMessage.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.AccentGreen,
                        )
                    }
                    if (!passwordError.isNullOrBlank()) {
                        Text(
                            text = passwordError.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.AccentRed,
                        )
                    }
                    }
                }

                SectionHeader(title = stringResource(R.string.account))
                if (roleType.isManager()) {
                    ClickableCard(onClick = onManageUsers) {
                        Row(
                            modifier = Modifier.padding(Dimens.cardPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Groups,
                                contentDescription = null,
                                tint = AppColors.SteelBlue,
                            )
                            Text(
                                text = stringResource(R.string.manage_users),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                                color = AppColors.TextPrimary,
                            )
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                            )
                        }
                    }
                }
                ClickableCard(onClick = onSettings) {
                    Row(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        Icon(imageVector = Icons.Rounded.Settings, contentDescription = null, tint = AppColors.SteelBlue)
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            color = AppColors.TextPrimary,
                        )
                        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = AppColors.TextSecondary)
                    }
                }
                PrimaryButton(
                    text = stringResource(R.string.logout),
                    leadingIcon = Icons.Rounded.Logout,
                    tone = PrimaryButtonTone.Danger,
                    onClick = {
                        if (isLoggingOut) return@PrimaryButton
                        isLoggingOut = true
                        scope.launch {
                            authRepository.logout()
                            isLoggingOut = false
                            onLogout()
                        }
                    },
                    isLoading = isLoggingOut,
                    enabled = !isLoggingOut,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProfilePhotoRow(initials: String, avatarSize: Dp) {
    Surface(
        color = AppColors.SteelBlueTint,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(avatarSize),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (avatarSize.value * 0.4f).sp,
                        ),
                        color = AppColors.SteelBlue,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_photo),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.tap_add_photo),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                    )
                }
            }
            Surface(
                color = AppColors.CardSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AppColors.BorderLight),
            ) {
                Text(
                    text = stringResource(R.string.change_photo),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.SteelBlue,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
