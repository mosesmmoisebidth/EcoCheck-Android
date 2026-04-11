package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.ClickableCard
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StatusChip
import com.moses.inspectionapp.ui.components.StatusChipStyle
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.mouseWheelScroll

@Composable
fun AssessmentTeamScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNext: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val user = repository.userProfile.collectAsState().value
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val inspections = repository.inspections.collectAsState().value
    val facility = facilities.firstOrNull { it.id == draft.facilityId }
    val sectorName = facility?.sector ?: user.sector
    val facilitiesById = remember(facilities) { facilities.associateBy { it.id } }
    val sectorMembers = inspections
        .filter { inspection ->
            facilitiesById[inspection.facilityId]?.sector?.equals(sectorName, ignoreCase = true) == true
        }
        .flatMap { it.teamMembers }
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val members = listOf(user.fullName)
        .plus(sectorMembers)
        .distinct()
    val selected = remember { mutableStateOf(setOf<String>()) }
    val scrollState = rememberScrollState()
    val canContinue = selected.value.isNotEmpty()
    val steps = assessmentStepLabels()

    LaunchedEffect(members, draft.teamMembers, user.fullName) {
        val initial = if (draft.teamMembers.isNotEmpty()) {
            draft.teamMembers.filter { members.contains(it) }
        } else {
            listOf(user.fullName)
        }
        selected.value = if (initial.isNotEmpty()) initial.toSet() else setOf(user.fullName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.inspection_team), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            StepProgressBar(
                steps = steps,
                currentStep = 3,
                onStepClick = onStepClick,
            )
            StepHeaderCard(
                title = stringResource(R.string.inspection_team),
                subtitle = stringResource(R.string.step_team_subtitle),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.select_team_members),
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = AppColors.TextSecondary,
                )
                StatusChip(
                    text = stringResource(R.string.selected_count, selected.value.size),
                    style = StatusChipStyle.Synced,
                )
            }
            members.forEach { member ->
                val isSelected = selected.value.contains(member)
                val initials = member.trim().firstOrNull()?.uppercase() ?: "?"
                ClickableCard(
                    onClick = {
                        selected.value = if (isSelected) {
                            selected.value - member
                        } else {
                            selected.value + member
                        }
                    },
                    isSelected = isSelected,
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AppColors.NavyMid, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = member, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            Text(
                                text = stringResource(R.string.hso_sector, sectorName),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextSecondary,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(2.dp, AppColors.SteelBlue, RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) AppColors.SteelBlue else Color.Transparent,
                                    RoundedCornerShape(6.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
            PrimaryButton(
                text = stringResource(R.string.next),
                onClick = {
                    DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                        teamMembers = selected.value.toList(),
                    )
                    onNext()
                },
                enabled = canContinue,
            )
            SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
        }
    }
}
