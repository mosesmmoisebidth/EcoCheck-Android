package com.moses.inspectionapp.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.moses.inspectionapp.BuildConfig
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.remote.ApiClient
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.LocaleManager
import com.moses.inspectionapp.ui.components.AppFilterChip
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.InfoRow
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.mouseWheelScroll

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val (wifiOnly, setWifiOnly) = remember { mutableStateOf(AppPreferences.wifiOnly) }
    val (autoSync, setAutoSync) = remember { mutableStateOf(AppPreferences.autoSync) }
    val (languageCode, setLanguageCode) = remember { mutableStateOf(AppPreferences.languageCode ?: "en") }
    val (apiBaseUrl, setApiBaseUrl) = remember {
        mutableStateOf(AppPreferences.apiBaseUrl ?: BuildConfig.API_BASE_URL)
    }
    val (notificationsEnabled, setNotificationsEnabled) = remember {
        mutableStateOf(AppPreferences.notificationsEnabled)
    }
    val (notifyLogin, setNotifyLogin) = remember { mutableStateOf(AppPreferences.notifyLogin) }
    val (notifyFacility, setNotifyFacility) = remember { mutableStateOf(AppPreferences.notifyFacility) }
    val (notifyInspection, setNotifyInspection) = remember { mutableStateOf(AppPreferences.notifyInspection) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.settings), onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
        ) {
            SectionHeader(title = stringResource(R.string.language))
            Surface(color = AppColors.CardSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.cardPadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppFilterChip(
                        label = stringResource(R.string.language_english),
                        isSelected = languageCode == "en",
                        onClick = { setLanguageCode("en") },
                    )
                    AppFilterChip(
                        label = stringResource(R.string.language_kinyarwanda),
                        isSelected = languageCode == "rw",
                        onClick = { setLanguageCode("rw") },
                    )
                }
            }

            SectionHeader(title = stringResource(R.string.sync_preferences))
            Surface(color = AppColors.CardSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.auto_sync), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(text = stringResource(R.string.auto_sync_desc), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                        }
                        Switch(
                            checked = autoSync,
                            onCheckedChange = setAutoSync,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.SteelBlue,
                                checkedTrackColor = AppColors.SteelBlueLight,
                                uncheckedThumbColor = AppColors.BorderMedium,
                                uncheckedTrackColor = AppColors.BorderLight,
                            ),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.wifi_only), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(text = stringResource(R.string.wifi_only_desc), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                        }
                        Switch(
                            checked = wifiOnly,
                            onCheckedChange = setWifiOnly,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.SteelBlue,
                                checkedTrackColor = AppColors.SteelBlueLight,
                                uncheckedThumbColor = AppColors.BorderMedium,
                                uncheckedTrackColor = AppColors.BorderLight,
                            ),
                        )
                    }
                }
            }

            SectionHeader(title = stringResource(R.string.notifications))
            Surface(color = AppColors.CardSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.notifications_enabled), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(text = stringResource(R.string.notifications_enabled_desc), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.notify_login), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(text = stringResource(R.string.notify_login_desc), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                        }
                        Switch(
                            checked = notifyLogin,
                            onCheckedChange = setNotifyLogin,
                            enabled = notificationsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.SteelBlue,
                                checkedTrackColor = AppColors.SteelBlueLight,
                                uncheckedThumbColor = AppColors.BorderMedium,
                                uncheckedTrackColor = AppColors.BorderLight,
                            ),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.notify_facility), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(text = stringResource(R.string.notify_facility_desc), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                        }
                        Switch(
                            checked = notifyFacility,
                            onCheckedChange = setNotifyFacility,
                            enabled = notificationsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.SteelBlue,
                                checkedTrackColor = AppColors.SteelBlueLight,
                                uncheckedThumbColor = AppColors.BorderMedium,
                                uncheckedTrackColor = AppColors.BorderLight,
                            ),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.notify_inspection), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(text = stringResource(R.string.notify_inspection_desc), style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
                        }
                        Switch(
                            checked = notifyInspection,
                            onCheckedChange = setNotifyInspection,
                            enabled = notificationsEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.SteelBlue,
                                checkedTrackColor = AppColors.SteelBlueLight,
                                uncheckedThumbColor = AppColors.BorderMedium,
                                uncheckedTrackColor = AppColors.BorderLight,
                            ),
                        )
                    }
                }
            }

            SectionHeader(title = stringResource(R.string.api_settings))
            Surface(color = AppColors.CardSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                    StyledTextField(
                        value = apiBaseUrl,
                        onValueChange = setApiBaseUrl,
                        label = stringResource(R.string.api_base_url),
                        leadingIcon = Icons.Rounded.Link,
                    )
                    Text(
                        text = stringResource(R.string.api_base_url_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            SectionHeader(title = stringResource(R.string.about))
            Surface(color = AppColors.CardSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                    InfoRow(icon = Icons.Rounded.Info, label = stringResource(R.string.app_version_label), value = BuildConfig.VERSION_NAME)
                    InfoRow(icon = Icons.Rounded.Info, label = stringResource(R.string.build_number_label), value = BuildConfig.VERSION_CODE.toString())
                }
            }

            PrimaryButton(
                text = stringResource(R.string.save_settings),
                leadingIcon = Icons.Rounded.Save,
                onClick = {
                    AppPreferences.autoSync = autoSync
                    AppPreferences.wifiOnly = wifiOnly
                    AppPreferences.languageCode = languageCode
                    AppPreferences.notificationsEnabled = notificationsEnabled
                    AppPreferences.notifyLogin = notifyLogin
                    AppPreferences.notifyFacility = notifyFacility
                    AppPreferences.notifyInspection = notifyInspection
                    ApiClient.updateBaseUrl(apiBaseUrl)
                    LocaleManager.applyLanguage(languageCode)
                    (context as? android.app.Activity)?.recreate()
                    onBack()
                },
            )
        }
    }
}
