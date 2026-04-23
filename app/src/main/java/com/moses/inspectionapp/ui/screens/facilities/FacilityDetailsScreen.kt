package com.moses.inspectionapp.ui.screens.facilities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.components.StatusChip
import com.moses.inspectionapp.ui.components.StatusChipStyle
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.NoImagePlaceholder
import com.moses.inspectionapp.data.model.SyncStatus
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.util.syncStatusLabel
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import coil.compose.AsyncImage
import java.io.File
import com.moses.inspectionapp.ui.util.formatDateTime

@Composable
fun FacilityDetailsScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onStartAssessment: () -> Unit,
    onEdit: () -> Unit,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val facilities = repository.facilities.collectAsState().value
    val inspections = repository.inspections.collectAsState().value
    val user = repository.userProfile.collectAsState().value
    val selectedId = DraftStore.selectedFacilityId.collectAsState().value
    val facility = facilities.firstOrNull { facilityItem ->
        facilityItem.id == selectedId || facilityItem.serverId == selectedId
    }
    val facilityKeys = listOfNotNull(
        facility?.id?.takeIf { it.isNotBlank() },
        facility?.serverId?.takeIf { it.isNotBlank() },
    ).toSet()
    val lastInspection = inspections
        .filter { inspection -> inspection.facilityId in facilityKeys }
        .maxByOrNull { it.createdAt }
    val now = System.currentTimeMillis()
    val canEdit = facility != null &&
        facility.createdBy == user.id &&
        now - facility.createdAt <= 24 * 60 * 60 * 1000
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.facility_details_title), onBack = onBack)
        if (isOffline) {
            OfflineBanner(lastSync = lastSyncLabel)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap)
                .padding(bottom = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
        ) {
            if (facility == null) {
                EmptyState(
                    title = stringResource(R.string.no_facility_selected),
                    message = stringResource(R.string.search_or_enroll),
                )
            } else {
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(16.dp)),
                ) {
                    Box(modifier = Modifier.padding(Dimens.cardPadding)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = facility.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = AppColors.TextPrimary,
                            )
                            Text(
                                text = "${stringResource(R.string.tin)} • ${facility.tin}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextSecondary,
                            )
                            Text(
                                text = "${facility.sector}, ${facility.district}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextPrimary,
                            )
                            Text(
                                text = stringResource(R.string.created_on, formatDateTime(facility.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextSecondary,
                            )
                        }
                        StatusChip(
                            text = syncStatusLabel(facility.syncStatus),
                            style = when (facility.syncStatus) {
                                SyncStatus.PENDING -> StatusChipStyle.Pending
                                SyncStatus.SYNCED -> StatusChipStyle.Synced
                                SyncStatus.CONFLICT -> StatusChipStyle.Conflict
                            },
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                    }
                }

                SectionHeader(title = stringResource(R.string.owner_manager))
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(14.dp)),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DetailRow(icon = Icons.Rounded.Person, label = stringResource(R.string.owner_name), value = facility.ownerName)
                        DetailRow(icon = Icons.Rounded.Phone, label = stringResource(R.string.owner_phone), value = facility.ownerPhone)
                        if (facility.ownerEmail.isNotBlank()) {
                            DetailRow(icon = Icons.Rounded.Email, label = stringResource(R.string.email), value = facility.ownerEmail)
                        }
                    }
                }

                SectionHeader(title = stringResource(R.string.location))
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(14.dp)),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DetailRow(icon = Icons.Rounded.LocationOn, label = stringResource(R.string.district), value = facility.district)
                        DetailRow(icon = Icons.Rounded.Map, label = stringResource(R.string.sector), value = facility.sector)
                        DetailRow(icon = Icons.Rounded.Home, label = stringResource(R.string.cell), value = facility.cell)
                        DetailRow(icon = Icons.Rounded.Home, label = stringResource(R.string.village), value = facility.village)
                        if (facility.latitude != null && facility.longitude != null) {
                            DetailRow(
                                icon = Icons.Rounded.Badge,
                                label = stringResource(R.string.coordinates),
                                value = stringResource(
                                    R.string.coordinates_label,
                                    facility.latitude,
                                    facility.longitude,
                                ),
                            )
                        }
                    }
                }

                SectionHeader(title = stringResource(R.string.photo))
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(14.dp)),
                ) {
                    if (!facility.photoPath.isNullOrBlank()) {
                        AsyncImage(
                            model = File(facility.photoPath),
                            contentDescription = stringResource(R.string.facility_photo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )
                    } else {
                        NoImagePlaceholder(label = stringResource(R.string.no_photo_captured))
                    }
                }

                SectionHeader(title = stringResource(R.string.last_inspection))
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(14.dp)),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (lastInspection == null) {
                            Text(text = stringResource(R.string.no_inspections), color = AppColors.TextSecondary)
                        } else {
                            Text(
                                text = stringResource(R.string.decision_label, decisionLabel(lastInspection.decision)),
                                color = AppColors.TextSecondary,
                            )
                            Text(
                                text = stringResource(R.string.fine_label, "${lastInspection.totalFine} RWF"),
                                color = AppColors.TextSecondary,
                            )
                        }
                    }
                }

                PrimaryButton(
                    text = stringResource(R.string.start_assessment),
                    onClick = {
                        DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                            facilityId = facility.id,
                            facilityName = facility.name,
                        )
                        onStartAssessment()
                    },
                )
                SecondaryButton(
                    text = stringResource(R.string.edit_facility_24h),
                    onClick = onEdit,
                    enabled = canEdit,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(AppColors.SteelBlueTint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AppColors.SteelBlue)
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
            )
        }
    }
}


