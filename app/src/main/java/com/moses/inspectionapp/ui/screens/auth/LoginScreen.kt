package com.moses.inspectionapp.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.remote.AuthRepository
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.sync.SyncManager
import com.moses.inspectionapp.ui.components.BrandLogo
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.NotificationHelper
import kotlinx.coroutines.launch

private enum class ActivationStep {
    Verify,
    SetPassword,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }
    val (passwordVisible, setPasswordVisible) = remember { mutableStateOf(false) }
    val isValid = email.isNotBlank() && password.length >= 6
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val loginFailedText = stringResource(R.string.login_failed)
    val context = LocalContext.current
    var showActivationSheet by remember { mutableStateOf(false) }
    var activationStep by remember { mutableStateOf(ActivationStep.Verify) }
    var activationEmail by remember { mutableStateOf("") }
    var activationCode by remember { mutableStateOf("") }
    var activationPassword by remember { mutableStateOf("") }
    var activationConfirm by remember { mutableStateOf("") }
    var activationError by remember { mutableStateOf<String?>(null) }
    var activationVerifyLoading by remember { mutableStateOf(false) }
    var activationSubmitLoading by remember { mutableStateOf(false) }
    var activationPasswordVisible by remember { mutableStateOf(false) }
    var activationConfirmVisible by remember { mutableStateOf(false) }
    val activationTitle = stringResource(R.string.activation_title)
    val activationSubtitle = stringResource(R.string.activation_subtitle)
    val activationPasswordSubtitle = stringResource(R.string.activation_password_subtitle)
    val activationCodeLabel = stringResource(R.string.activation_code)
    val activationCodeHint = stringResource(R.string.activation_code_hint)
    val activationPasswordLabel = stringResource(R.string.activation_password)
    val activationConfirmLabel = stringResource(R.string.activation_confirm_password)
    val activationContinueLabel = stringResource(R.string.activation_continue)
    val activationSubmitLabel = stringResource(R.string.activation_submit)
    val activationMismatchText = stringResource(R.string.activation_password_mismatch)
    val activationHint = stringResource(R.string.activation_help)
    val passwordMinLengthText = stringResource(R.string.password_min_length)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.NavyDark),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandLogo(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 12.dp),
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.TextOnDark,
                )
                Text(
                    text = stringResource(R.string.city_health_authority),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextOnDarkMuted,
                )
            }
            Surface(
                color = AppColors.CardSurface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimens.small),
                ) {
                    Text(
                        text = stringResource(R.string.welcome_back),
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(R.string.sign_in_to_continue),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                    )
                    StyledTextField(
                        value = email,
                        onValueChange = setEmail,
                        label = stringResource(R.string.email),
                        leadingIcon = Icons.Rounded.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                        ),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    StyledTextField(
                        value = password,
                        onValueChange = setPassword,
                        label = stringResource(R.string.password),
                        leadingIcon = Icons.Rounded.Lock,
                        trailingIcon = {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable { setPasswordVisible(!passwordVisible) },
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = password.isNotEmpty() && password.length < 6,
                        errorText = if (password.isNotEmpty() && password.length < 6) {
                            stringResource(R.string.password_min_length)
                        } else {
                            null
                        },
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    PrimaryButton(
                        text = stringResource(R.string.sign_in),
                        onClick = {
                            if (isLoading) return@PrimaryButton
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authRepository.login(email.trim(), password)
                                isLoading = false
                                if (result.isSuccess) {
                                    if (AppPreferences.autoSync) {
                                        SyncManager.enqueue(context)
                                    }
                                    val profile = result.getOrNull()
                                    NotificationHelper.notifyLogin(context, profile?.fullName)
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message
                                        ?: loginFailedText
                                }
                            }
                        },
                        enabled = isValid && !isLoading,
                        isLoading = isLoading,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    TextButton(
                        onClick = {
                            activationEmail = email.trim()
                            activationCode = ""
                            activationPassword = ""
                            activationConfirm = ""
                            activationError = null
                            activationVerifyLoading = false
                            activationSubmitLoading = false
                            showActivationSheet = true
                            activationStep = ActivationStep.Verify
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        Text(
                            text = activationHint,
                            color = AppColors.SteelBlue,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.AccentRed,
                        )
                    }
                }
            }
        }
    }

    if (showActivationSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val activationPasswordOk = activationPassword.length >= 6
        val activationMatch = activationPassword == activationConfirm
        val activationReady =
            activationEmail.isNotBlank() &&
                activationCode.isNotBlank() &&
                activationPasswordOk &&
                activationMatch

        ModalBottomSheet(
            onDismissRequest = { showActivationSheet = false },
            sheetState = sheetState,
            containerColor = AppColors.CardSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
            ) {
                AnimatedContent(
                    targetState = activationStep,
                    transitionSpec = {
                        val enter = slideInVertically(
                            animationSpec = tween(240),
                        ) { height -> height / 2 } + fadeIn(tween(240))
                        val exit = slideOutVertically(
                            animationSpec = tween(200),
                        ) { height -> -height / 2 } + fadeOut(tween(200))
                        enter togetherWith exit
                    },
                    label = "activationStep",
                ) { step ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        Text(
                            text = activationTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = AppColors.TextPrimary,
                        )
                        Text(
                            text = if (step == ActivationStep.Verify) {
                                activationSubtitle
                            } else {
                                activationPasswordSubtitle
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                        )
                        if (step == ActivationStep.Verify) {
                            StyledTextField(
                                value = activationEmail,
                                onValueChange = { activationEmail = it },
                                label = stringResource(R.string.email),
                                leadingIcon = Icons.Rounded.Email,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            )
                            StyledTextField(
                                value = activationCode,
                                onValueChange = { activationCode = it },
                                label = activationCodeLabel,
                                placeholder = activationCodeHint,
                                leadingIcon = Icons.Rounded.Lock,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            if (!activationError.isNullOrBlank()) {
                                Text(
                                    text = activationError.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.AccentRed,
                                )
                            }
                            val activationReady =
                                activationEmail.isNotBlank() && activationCode.isNotBlank()
                            PrimaryButton(
                                text = activationContinueLabel,
                                onClick = {
                                    if (activationVerifyLoading) return@PrimaryButton
                                    activationError = null
                                    activationVerifyLoading = true
                                    scope.launch {
                                        val result = authRepository.verifyActivation(
                                            identifier = activationEmail.trim(),
                                            activationCode = activationCode.trim(),
                                        )
                                        activationVerifyLoading = false
                                        if (result.isSuccess) {
                                            activationError = null
                                            activationPassword = ""
                                            activationConfirm = ""
                                            activationPasswordVisible = false
                                            activationConfirmVisible = false
                                            activationStep = ActivationStep.SetPassword
                                        } else {
                                            activationError = result.exceptionOrNull()?.message
                                                ?: loginFailedText
                                        }
                                    }
                                },
                                enabled = activationReady && !activationVerifyLoading,
                                isLoading = activationVerifyLoading,
                            )
                        } else {
                            val activationPasswordOk = activationPassword.length >= 6
                            val activationMatch = activationPassword == activationConfirm
                            StyledTextField(
                                value = activationPassword,
                                onValueChange = { activationPassword = it },
                                label = activationPasswordLabel,
                                leadingIcon = Icons.Rounded.Lock,
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (activationPasswordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = null,
                                        tint = AppColors.TextSecondary,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .clickable {
                                                activationPasswordVisible = !activationPasswordVisible
                                            },
                                    )
                                },
                                visualTransformation = if (activationPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                isError = activationPassword.isNotEmpty() && !activationPasswordOk,
                                errorText = if (activationPassword.isNotEmpty() && !activationPasswordOk) {
                                    passwordMinLengthText
                                } else {
                                    null
                                },
                            )
                            StyledTextField(
                                value = activationConfirm,
                                onValueChange = { activationConfirm = it },
                                label = activationConfirmLabel,
                                leadingIcon = Icons.Rounded.Lock,
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (activationConfirmVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = null,
                                        tint = AppColors.TextSecondary,
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .clickable {
                                                activationConfirmVisible = !activationConfirmVisible
                                            },
                                    )
                                },
                                visualTransformation = if (activationConfirmVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                isError = activationConfirm.isNotEmpty() && !activationMatch,
                                errorText = if (activationConfirm.isNotEmpty() && !activationMatch) {
                                    activationMismatchText
                                } else {
                                    null
                                },
                            )
                            if (!activationError.isNullOrBlank()) {
                                Text(
                                    text = activationError.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.AccentRed,
                                )
                            }
                            val activationReady =
                                activationPasswordOk && activationMatch
                            PrimaryButton(
                                text = activationSubmitLabel,
                                onClick = {
                                    if (activationSubmitLoading) return@PrimaryButton
                                    activationError = null
                                    if (!activationMatch) {
                                        activationError = activationMismatchText
                                        return@PrimaryButton
                                    }
                                    if (!activationPasswordOk) {
                                        activationError = passwordMinLengthText
                                        return@PrimaryButton
                                    }
                                    activationSubmitLoading = true
                                    scope.launch {
                                        val result = authRepository.activateAccount(
                                            identifier = activationEmail.trim(),
                                            activationCode = activationCode.trim(),
                                            password = activationPassword,
                                        )
                                        activationSubmitLoading = false
                                        if (result.isSuccess) {
                                            if (AppPreferences.autoSync) {
                                                SyncManager.enqueue(context)
                                            }
                                            val profile = result.getOrNull()
                                            NotificationHelper.notifyLogin(context, profile?.fullName)
                                            showActivationSheet = false
                                            onLoginSuccess()
                                        } else {
                                            activationError = result.exceptionOrNull()?.message
                                                ?: loginFailedText
                                        }
                                    }
                                },
                                enabled = activationReady && !activationSubmitLoading,
                                isLoading = activationSubmitLoading,
                            )
                            TextButton(
                                onClick = {
                                    activationError = null
                                    activationPassword = ""
                                    activationConfirm = ""
                                    activationPasswordVisible = false
                                    activationConfirmVisible = false
                                    activationStep = ActivationStep.Verify
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = stringResource(R.string.back), color = AppColors.TextSecondary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { showActivationSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.cancel), color = AppColors.TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

}
