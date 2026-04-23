package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.data.validator.InputValidators
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.ClickableCard
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StatusChip
import com.moses.inspectionapp.ui.components.StatusChipStyle
import com.moses.inspectionapp.ui.components.StyledTextField
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import kotlinx.coroutines.launch

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
    val customMembers = repository.customTeamMembers.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val inspections = repository.inspections.collectAsState().value
    val facility = facilities.firstOrNull { facilityItem ->
        facilityItem.id == draft.facilityId || facilityItem.serverId == draft.facilityId
    }
    val sectorName = facility?.sector ?: user.sector
    val scope = rememberCoroutineScope()
    val facilitiesById = remember(facilities) {
        facilities.flatMap { facilityItem ->
            listOfNotNull(
                facilityItem.id.takeIf { it.isNotBlank() }?.let { it to facilityItem },
                facilityItem.serverId?.takeIf { it.isNotBlank() }?.let { it to facilityItem },
            )
        }.toMap()
    }
    val customMemberLookup = remember(customMembers) {
        customMembers
            .map(::normalizeMemberName)
            .map { it.lowercase() }
            .toSet()
    }
    val sectorMembers = inspections
        .filter { inspection ->
            inspection.createdBy == user.id &&
                facilitiesById[inspection.facilityId]?.sector?.equals(sectorName, ignoreCase = true) == true
        }
        .flatMap { it.teamMembers }
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val selected = remember { mutableStateOf(setOf<String>()) }
    val hasInitializedSelection = rememberSaveable { mutableStateOf(false) }
    val newMemberName = rememberSaveable { mutableStateOf("") }
    val newMemberError = remember { mutableStateOf<String?>(null) }
    val members = mergedMembers(
        listOf(user.fullName) +
            customMembers +
            sectorMembers +
            draft.teamMembers +
            selected.value.toList(),
    )
    val scrollState = rememberScrollState()
    val canContinue = selected.value.isNotEmpty()
    val steps = assessmentStepLabels()
    val memberNameRequiredError = stringResource(R.string.team_member_name_required)
    val memberNameInvalidError = stringResource(R.string.user_name_validation)
    val memberInputHint = stringResource(R.string.team_member_input_help)

    LaunchedEffect(draft.teamMembers, user.fullName, hasInitializedSelection.value) {
        if (!hasInitializedSelection.value) {
            val initial = if (draft.teamMembers.isNotEmpty()) {
                draft.teamMembers
            } else {
                listOf(user.fullName)
            }
            selected.value = initial
                .map(::normalizeMemberName)
                .filter { it.isNotBlank() }
                .toSet()
                .ifEmpty { setOf(user.fullName.trim()) }
            hasInitializedSelection.value = true
        }
    }

    val addMember = {
        val normalizedName = normalizeMemberName(newMemberName.value)
        if (normalizedName.isBlank()) {
            newMemberError.value = memberNameRequiredError
        } else if (!InputValidators.isValidName(normalizedName)) {
            newMemberError.value = memberNameInvalidError
        } else {
            val existingMatch = members.firstOrNull { it.equals(normalizedName, ignoreCase = true) }
            val memberToSelect = existingMatch ?: normalizedName
            if (existingMatch == null) {
                scope.launch {
                    repository.addCustomTeamMember(normalizedName)
                }
            }
            selected.value = selected.value + memberToSelect
            newMemberName.value = ""
            newMemberError.value = null
        }
    }
    val removeMember: (String) -> Unit = removeMember@{ memberName ->
        val normalizedName = normalizeMemberName(memberName)
        if (normalizedName.isBlank() || normalizedName.equals(user.fullName, ignoreCase = true)) {
            return@removeMember
        }
        scope.launch {
            repository.removeCustomTeamMember(normalizedName)
        }
        selected.value = selected.value.filterNot { it.equals(normalizedName, ignoreCase = true) }.toSet()
        DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
            teamMembers = DraftStore.inspectionDraft.value.teamMembers.filterNot {
                it.equals(normalizedName, ignoreCase = true)
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.inspection_team), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.screenPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = Dimens.cardMaxWidth)
                        .mouseWheelScroll(scrollState)
                        .verticalScroll(scrollState)
                        .padding(vertical = Dimens.sectionGap),
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
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val isCompactHeader = maxWidth <= 360.dp
                        if (isCompactHeader) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.select_team_members),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = AppColors.TextSecondary,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    StatusChip(
                                        text = stringResource(R.string.selected_count, selected.value.size),
                                        style = StatusChipStyle.Synced,
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.select_team_members),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = AppColors.TextSecondary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                StatusChip(
                                    text = stringResource(R.string.selected_count, selected.value.size),
                                    style = StatusChipStyle.Synced,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StyledTextField(
                            value = newMemberName.value,
                            onValueChange = {
                                newMemberName.value = InputValidators.normalizeName(it)
                                newMemberError.value = null
                            },
                            label = stringResource(R.string.team_member_name),
                            placeholder = stringResource(R.string.team_member_name_hint),
                            isError = newMemberError.value != null,
                            errorText = newMemberError.value,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            color = AppColors.SteelBlue,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(Dimens.inputHeight),
                        ) {
                            IconButton(onClick = addMember) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = stringResource(R.string.add_member),
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                    Text(
                        text = memberInputHint,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                    )
                    members.forEach { member ->
                        val isSelected = selected.value.any { it.equals(member, ignoreCase = true) }
                        val initials = member.trim().firstOrNull()?.uppercase() ?: "?"
                        val isLeadMember = member.equals(user.fullName, ignoreCase = true)
                        val isCustomMember = customMemberLookup.contains(normalizeMemberName(member).lowercase())
                        val roleLabel = if (isLeadMember) {
                            stringResource(R.string.lead_inspector_role)
                        } else {
                            stringResource(R.string.assistant_member_role)
                        }
                        val avatarBackground = if (isLeadMember) AppColors.NavyMid else AppColors.SteelBlueTint
                        val avatarTextColor = if (isLeadMember) Color.White else AppColors.SteelBlue
                        val removeEnabled = isCustomMember && !isLeadMember
                        ClickableCard(
                            onClick = {
                                selected.value = if (isSelected) {
                                    selected.value.filterNot { it.equals(member, ignoreCase = true) }.toSet()
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
                                        .background(avatarBackground, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = avatarTextColor,
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = member, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            color = if (isLeadMember) {
                                                AppColors.SteelBlueTint
                                            } else {
                                                AppColors.AccentGreenBg
                                            },
                                            shape = RoundedCornerShape(50.dp),
                                        ) {
                                            Text(
                                                text = roleLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isLeadMember) AppColors.SteelBlue else AppColors.AccentGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            )
                                        }
                                        Text(
                                            text = sectorName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    if (removeEnabled) {
                                        IconButton(
                                            onClick = { removeMember(member) },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = stringResource(R.string.remove),
                                                tint = AppColors.AccentRed,
                                            )
                                        }
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
                    }
                    PrimaryButton(
                        text = stringResource(R.string.next),
                        onClick = {
                            val orderedMembers = members.filter { selectedName ->
                                selected.value.any { it.equals(selectedName, ignoreCase = true) }
                            }
                            DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                teamMembers = orderedMembers,
                            )
                            onNext()
                        },
                        enabled = canContinue,
                    )
                    SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
                }
            }
        }
    }
}

private fun normalizeMemberName(raw: String): String {
    return InputValidators.normalizeName(raw)
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

private fun mergedMembers(rawMembers: List<String>): List<String> {
    val seen = mutableSetOf<String>()
    return rawMembers
        .map(::normalizeMemberName)
        .filter { it.isNotBlank() }
        .filter { candidate -> seen.add(candidate.lowercase()) }
}
