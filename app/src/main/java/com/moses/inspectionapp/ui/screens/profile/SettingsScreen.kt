package com.moses.inspectionapp.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.moses.inspectionapp.BuildConfig
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.LocaleManager
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNotificationHistory: () -> Unit = {},
) {
    val (wifiOnly, setWifiOnly) = remember { mutableStateOf(AppPreferences.wifiOnly) }
    val (autoSync, setAutoSync) = remember { mutableStateOf(AppPreferences.autoSync) }
    val (languageCode, setLanguageCode) = remember { mutableStateOf(AppPreferences.languageCode ?: "en") }
    val (notificationsEnabled, setNotificationsEnabled) = remember {
        mutableStateOf(AppPreferences.notificationsEnabled)
    }
    val (notifyLogin, setNotifyLogin) = remember { mutableStateOf(AppPreferences.notifyLogin) }
    val (notifyFacility, setNotifyFacility) = remember { mutableStateOf(AppPreferences.notifyFacility) }
    val (notifyInspection, setNotifyInspection) = remember { mutableStateOf(AppPreferences.notifyInspection) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearDialog by remember { mutableStateOf(false) }
    val notificationDeniedText = stringResource(R.string.notifications_permission_denied)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            setNotificationsEnabled(true)
        } else {
            setNotificationsEnabled(false)
            Toast.makeText(context, notificationDeniedText, Toast.LENGTH_LONG).show()
        }
    }

    val masterRowColor by animateColorAsState(
        targetValue = if (notificationsEnabled) AppColors.SteelBlueTint else Color.Transparent,
        label = "masterRowBg",
    )
    val masterIconBg by animateColorAsState(
        targetValue = if (notificationsEnabled) AppColors.SteelBlue.copy(alpha = 0.15f) else AppColors.BorderLight,
        label = "masterIconBg",
    )
    val masterIconTint by animateColorAsState(
        targetValue = if (notificationsEnabled) AppColors.SteelBlue else AppColors.TextSecondary,
        label = "masterIconTint",
    )
    val childAlpha by animateFloatAsState(
        targetValue = if (notificationsEnabled) 1f else 0.4f,
        label = "childAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.settings), onBack = onBack)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
                item {
                    SettingsSectionLabel(title = stringResource(R.string.language))
                    Surface(
                        color = AppColors.CardSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Select your preferred language",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = AppColors.TextSecondary,
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                SegmentedLanguageChip(
                                    label = stringResource(R.string.language_english),
                                    selected = languageCode == "en",
                                    shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp),
                                    onClick = { setLanguageCode("en") },
                                    modifier = Modifier.weight(1f),
                                )
                                SegmentedLanguageChip(
                                    label = stringResource(R.string.language_kinyarwanda),
                                    selected = languageCode == "rw",
                                    shape = RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp),
                                    onClick = { setLanguageCode("rw") },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                item {
                    SettingsSectionLabel(title = stringResource(R.string.sync_preferences))
                    Surface(
                        color = AppColors.CardSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp,
                    ) {
                        Column {
                            SettingToggleRow(
                                icon = Icons.Rounded.Sync,
                                iconBg = AppColors.SteelBlueTint,
                                iconTint = AppColors.SteelBlue,
                                title = stringResource(R.string.auto_sync),
                                subtitle = stringResource(R.string.auto_sync_desc),
                                checked = autoSync,
                                onCheckedChange = setAutoSync,
                            )
                            IndentedDivider()
                            SettingToggleRow(
                                icon = Icons.Rounded.Wifi,
                                iconBg = Color(0xFFEBF4FF),
                                iconTint = AppColors.SteelBlue,
                                title = stringResource(R.string.wifi_only),
                                subtitle = stringResource(R.string.wifi_only_desc),
                                checked = wifiOnly,
                                onCheckedChange = setWifiOnly,
                            )
                        }
                    }
                }
                item {
                    SettingsSectionLabel(title = stringResource(R.string.notifications))
                    Surface(
                        color = AppColors.CardSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNotificationHistory() },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    color = AppColors.SteelBlueTint,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Notifications,
                                            contentDescription = null,
                                            tint = AppColors.SteelBlue,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Notification history",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppColors.TextPrimary,
                                    )
                                    Text(
                                        text = "See recent notifications",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.TextSecondary,
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = AppColors.CardSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp,
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(masterRowColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        color = masterIconBg,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(36.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Notifications,
                                                contentDescription = null,
                                                tint = masterIconTint,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(R.string.notifications_enabled),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = AppColors.TextPrimary,
                                        )
                                        Text(
                                            text = stringResource(R.string.notifications_enabled_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary,
                                        )
                                    }
                                }
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { enabled ->
                                        val needsPermission = enabled &&
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS,
                                            ) != PackageManager.PERMISSION_GRANTED
                                        if (needsPermission) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            setNotificationsEnabled(enabled)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AppColors.SteelBlue,
                                        checkedTrackColor = AppColors.SteelBlueLight,
                                        uncheckedThumbColor = AppColors.BorderMedium,
                                        uncheckedTrackColor = AppColors.BorderLight,
                                    ),
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppColors.BorderMedium),
                            )
                            NotificationChildRow(
                                title = stringResource(R.string.notify_login),
                                subtitle = stringResource(R.string.notify_login_desc),
                                icon = Icons.Rounded.Login,
                                iconBg = Color(0xFFFFF4E6),
                                iconTint = AppColors.AccentOrange,
                                checked = notifyLogin,
                                onCheckedChange = setNotifyLogin,
                                enabled = notificationsEnabled,
                                alpha = childAlpha,
                            )
                            IndentedDivider()
                            NotificationChildRow(
                                title = stringResource(R.string.notify_facility),
                                subtitle = stringResource(R.string.notify_facility_desc),
                                icon = Icons.Rounded.Storefront,
                                iconBg = Color(0xFFE6F7EE),
                                iconTint = Color(0xFF16A34A),
                                checked = notifyFacility,
                                onCheckedChange = setNotifyFacility,
                                enabled = notificationsEnabled,
                                alpha = childAlpha,
                            )
                            IndentedDivider()
                            NotificationChildRow(
                                title = stringResource(R.string.notify_inspection),
                                subtitle = stringResource(R.string.notify_inspection_desc),
                                icon = Icons.Rounded.Assignment,
                                iconBg = Color(0xFFF5F0FF),
                                iconTint = Color(0xFF7C3AED),
                                checked = notifyInspection,
                                onCheckedChange = setNotifyInspection,
                                enabled = notificationsEnabled,
                                alpha = childAlpha,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Settings are saved locally on your device.",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PrimaryButton(
                        text = stringResource(R.string.save_settings),
                        leadingIcon = Icons.Rounded.Save,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            AppPreferences.autoSync = autoSync
                            AppPreferences.wifiOnly = wifiOnly
                            AppPreferences.languageCode = languageCode
                            AppPreferences.notificationsEnabled = notificationsEnabled
                            AppPreferences.notifyLogin = notifyLogin
                            AppPreferences.notifyFacility = notifyFacility
                            AppPreferences.notifyInspection = notifyInspection
                            LocaleManager.applyLanguage(languageCode)
                            (context as? android.app.Activity)?.recreate()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Settings saved successfully",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                    )
                }

                item {
                    SettingsSectionLabel(title = "About")
                    Surface(
                        color = AppColors.CardSurface,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp,
                    ) {
                        Column {
                            InfoRow(
                                icon = Icons.Rounded.Info,
                                iconBg = AppColors.BorderLight,
                                iconTint = AppColors.TextSecondary,
                                title = "App Version",
                                subtitle = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                trailing = "v${BuildConfig.VERSION_NAME}",
                            )
                            IndentedDivider()
                            InfoRow(
                                icon = Icons.Rounded.DeleteSweep,
                                iconBg = Color(0xFFFFF4E6),
                                iconTint = AppColors.AccentOrange,
                                title = "Clear Cache",
                                subtitle = "Free up local storage",
                                trailingIcon = Icons.Rounded.KeyboardArrowRight,
                                onClick = { showClearDialog = true },
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) { data ->
            Snackbar(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = Dimens.screenPadding),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = "Clear all cached data?") },
            text = { Text(text = "This will remove cached data stored on this device.") },
            confirmButton = {
                Text(
                    text = "Clear",
                    color = AppColors.AccentOrange,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable {
                            AppPreferences.facilitiesBackup = null
                            showClearDialog = false
                        },
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    color = AppColors.TextSecondary,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable { showClearDialog = false },
                )
            },
        )
    }
}

@Composable
private fun SettingsSectionLabel(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(AppColors.SteelBlue, CircleShape),
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.3.sp,
            ),
            color = AppColors.TextSecondary,
        )
    }
}

@Composable
private fun SegmentedLanguageChip(
    label: String,
    selected: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateColorAsState(
        targetValue = if (selected) AppColors.SteelBlue else AppColors.CardSurface,
        label = "segmentBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else AppColors.TextSecondary,
        label = "segmentText",
    )
    Surface(
        color = background,
        shape = shape,
        modifier = modifier
            .height(46.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor,
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = iconBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.SteelBlue,
                checkedTrackColor = AppColors.SteelBlueLight,
                uncheckedThumbColor = AppColors.BorderMedium,
                uncheckedTrackColor = AppColors.BorderLight,
            ),
        )
    }
}

@Composable
private fun NotificationChildRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    alpha: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = iconBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.size(44.dp, 26.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.SteelBlue,
                checkedTrackColor = AppColors.SteelBlueLight,
                uncheckedThumbColor = AppColors.BorderMedium,
                uncheckedTrackColor = AppColors.BorderLight,
            ),
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = iconBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                )
            }
        }
        when {
            trailingIcon != null -> {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                )
            }
            trailing != null -> {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun IndentedDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp)
            .height(1.dp)
            .background(AppColors.BorderLight),
    )
}
