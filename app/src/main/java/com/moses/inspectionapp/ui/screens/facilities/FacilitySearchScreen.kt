package com.moses.inspectionapp.ui.screens.facilities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.FacilitySummaryCard
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.mouseWheelScroll

@Composable
fun FacilitySearchScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onSelectFacility: () -> Unit,
    onEnrollNew: () -> Unit,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val facilities = repository.facilities.collectAsState().value
    val (query, setQuery) = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val normalizedQuery = query.trim()
    val filtered = facilities
        .sortedByDescending { it.createdAt }
        .filter { facility ->
            if (normalizedQuery.isBlank()) {
                true
            } else {
                facility.name.contains(normalizedQuery, ignoreCase = true) ||
                    facility.tin.contains(normalizedQuery, ignoreCase = true)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.facility_search), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium),
        ) {
            StyledTextField(
                value = query,
                onValueChange = setQuery,
                label = stringResource(R.string.search_name_tin),
                leadingIcon = Icons.Rounded.Search,
            )
            if (filtered.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.no_facilities),
                    message = stringResource(R.string.try_another_or_enroll),
                    icon = Icons.Rounded.Search,
                )
            } else {
                filtered.forEach { facility ->
                    FacilitySummaryCard(
                        name = facility.name,
                        tin = facility.tin,
                        location = "${facility.sector}, ${facility.district}",
                        onClick = {
                            DraftStore.selectedFacilityId.value = facility.id
                            DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                facilityId = facility.id,
                                facilityName = facility.name,
                            )
                            onSelectFacility()
                        },
                    )
                }
            }
            PrimaryButton(text = stringResource(R.string.enroll_new_facility), onClick = onEnrollNew)
        }
    }
}


