package com.moses.inspectionapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.data.store.SyncStateStore
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.theme.LocalAppTypography
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

@Composable
fun HomeScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNewAssessment: () -> Unit,
    onEnrollFacility: () -> Unit,
    onMyInspections: () -> Unit,
    onSyncNow: () -> Unit,
    onStats: () -> Unit,
) {
    val repository = AppContainer.repository
    val user = repository.userProfile.collectAsState().value
    val stats = repository.stats.collectAsState().value
    val pending = repository.pendingCounts.collectAsState().value
    val isSyncing by SyncStateStore.isSyncing.collectAsState()
    val pendingCount = pending.facilities + pending.inspections
    val ty = LocalAppTypography.current
    val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "syncRotationValue",
    )

    val dotToday = Color(0xFF16A34A)
    val dotPending = Color(0xFFE8650A)
    val dotWeek = Color(0xFF3B82F6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = Dimens.cardMaxWidth),
        ) {
            item {
                OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
            }
            item {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    color = AppColors.NavyDark,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
                        verticalArrangement = Arrangement.spacedBy(Dimens.smallGap),
                    ) {
                        Text(
                            text = stringResource(R.string.good_morning),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = ty.bodyLarge),
                            color = AppColors.TextOnDarkMuted,
                        )
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppColors.TextOnDark,
                        )
                        Text(
                            text = "${user.sector} \u2022 ${user.district}",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = ty.bodyMedium),
                            color = AppColors.TextOnDarkMuted,
                        )
                        Spacer(modifier = Modifier.height(Dimens.itemGap))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            StatChip(
                                label = stringResource(R.string.stat_today_label),
                                value = stats.todayInspections.toString(),
                                dotColor = dotToday,
                                modifier = Modifier.weight(1f),
                            )
                            StatChip(
                                label = stringResource(R.string.stat_pending_label),
                                value = pendingCount.toString(),
                                dotColor = dotPending,
                                modifier = Modifier.weight(1f),
                            )
                            StatChip(
                                label = stringResource(R.string.stat_week_label),
                                value = stats.weekInspections.toString(),
                                dotColor = dotWeek,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.itemGap))
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.08f)),
                        )
                        Spacer(modifier = Modifier.height(Dimens.smallGap))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = if (isOffline) Icons.Rounded.WifiOff else Icons.Rounded.Wifi,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = if (isOffline) {
                                    "Offline - data saved locally"
                                } else {
                                    val label = if (lastSyncLabel.isBlank()) "just now" else lastSyncLabel
                                    "Online - last synced $label"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextOnDarkMuted,
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.quick_actions).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(
                        start = Dimens.screenPadding,
                        top = Dimens.sectionGap,
                        bottom = Dimens.smallGap,
                    ),
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                ) {
                    PrimaryActionCard(
                        title = stringResource(R.string.new_assessment),
                        subtitle = stringResource(R.string.start_food_safety),
                        icon = Icons.Rounded.AddCircleOutline,
                        iconBg = Color(0xFFEBF4FF),
                        iconTint = AppColors.SteelBlue,
                        onClick = onNewAssessment,
                    )
                    PrimaryActionCard(
                        title = stringResource(R.string.enroll_facility),
                        subtitle = stringResource(R.string.add_new_restobar),
                        icon = Icons.Rounded.Storefront,
                        iconBg = Color(0xFFE6F7EE),
                        iconTint = dotToday,
                        onClick = onEnrollFacility,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screenPadding, vertical = Dimens.itemGap),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                ) {
                    GridActionCard(
                        title = stringResource(R.string.my_inspections),
                        subtitle = stringResource(R.string.view_recent_reports),
                        icon = Icons.Rounded.Assignment,
                        iconBg = Color(0xFFF5F0FF),
                        iconTint = Color(0xFF7C3AED),
                        onClick = onMyInspections,
                        modifier = Modifier.weight(1f),
                    )
                    GridActionCard(
                        title = stringResource(R.string.sync_now),
                        subtitle = stringResource(R.string.upload_pending),
                        icon = Icons.Rounded.Sync,
                        iconBg = Color(0xFFFFF4E6),
                        iconTint = AppColors.AccentOrange,
                        onClick = onSyncNow,
                        modifier = Modifier.weight(1f),
                        rotation = if (isSyncing) syncRotation else 0f,
                        pendingCount = pendingCount,
                    )
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screenPadding),
                ) {
                    PrimaryActionCard(
                        title = stringResource(R.string.basic_stats),
                        subtitle = stringResource(R.string.daily_weekly_totals),
                        icon = Icons.Rounded.BarChart,
                        iconBg = Color(0xFFFFF0F0),
                        iconTint = AppColors.AccentRed,
                        onClick = onStats,
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(Dimens.xxLarge * 2f))
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    val ty = LocalAppTypography.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AppColors.NavyMid,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, RoundedCornerShape(50)),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = ty.titleLarge,
                    ),
                    color = AppColors.TextOnDark,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = ty.labelSmall,
                ),
                color = AppColors.TextOnDarkMuted,
            )
        }
    }
}

@Composable
private fun PrimaryActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    val ty = LocalAppTypography.current
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                color = iconBg,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = ty.bodyMedium),
                    color = AppColors.TextSecondary,
                )
            }
            Surface(
                color = AppColors.SteelBlueTint,
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppColors.SteelBlue,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GridActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    pendingCount: Int = 0,
) {
    Surface(
        color = AppColors.CardSurface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = iconBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation },
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                )
            }
            if (title == stringResource(R.string.sync_now) && pendingCount > 0) {
                Surface(
                    color = AppColors.AccentOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50.dp),
                ) {
                    Text(
                        text = "$pendingCount pending",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.AccentOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
