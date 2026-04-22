package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Comment
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.Decision
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.data.sync.SyncManager
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.ErrorState
import com.moses.inspectionapp.ui.components.LabelValueRow
import com.moses.inspectionapp.ui.components.NoImagePlaceholder
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.SectionLabel
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.components.StaggeredAnimatedItem
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.CardShape
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.MARKS_PER_QUESTION
import com.moses.inspectionapp.ui.util.NotificationHelper
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.calculateAssessmentScore
import com.moses.inspectionapp.ui.util.decisionLabel
import com.moses.inspectionapp.ui.util.formatDateTime
import com.moses.inspectionapp.ui.util.visitTypeLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentReviewScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val inspectionTypes = repository.inspectionTypes.collectAsState().value
    val faults = repository.faults.collectAsState().value
    val facility = facilities.firstOrNull { it.id == draft.facilityId }
    val inspectionType = inspectionTypes.firstOrNull { it.id == draft.inspectionTypeId }
    val selectedFaults = faults.filter { draft.selectedFaultIds.contains(it.id) }
    val questionCount = faults.count { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val scoreSummary = calculateAssessmentScore(
        totalQuestions = questionCount,
        failedAnswers = selectedFaults.size,
    )
    val decision = draft.decision ?: Decision.WARNING
    val totalCharge by animateIntAsState(targetValue = draft.adjustmentAmount, label = "reviewTotalCharge")
    val steps = assessmentStepLabels()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val facilityName = draft.facilityName.ifBlank { facility?.name.orEmpty() }
    val facilityTin = facility?.tin.orEmpty()
    val locationLabel = listOfNotNull(facility?.sector, facility?.district)
        .filter { it.isNotBlank() }
        .joinToString(", ")
    val inspectionTypeLabel = inspectionType?.name ?: stringResource(R.string.inspection)
    val inspectionDateLabel = formatDateTime(draft.createdAt)
    val teamMembers = draft.teamMembers.filter { it.isNotBlank() }
    val teamLabel = if (teamMembers.size == 1) "1 Team member" else "${teamMembers.size} Team members"
    val commentItems = parseBulletItems(draft.comments)
    val recommendationItems = parseNumberedItems(draft.recommendations)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.review_submit), onBack = onBack)
            OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = Dimens.cardMaxWidth)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                contentPadding = PaddingValues(bottom = 184.dp),
            ) {
                item {
                    StepProgressBar(
                        steps = steps,
                        currentStep = 8,
                        onStepClick = onStepClick,
                    )
                }

                item {
                    StaggeredAnimatedItem(index = 0) {
                        HeroSummaryCard(
                            facilityName = facilityName,
                            facilityTin = facilityTin,
                            locationLabel = locationLabel,
                            inspectionTypeLabel = inspectionTypeLabel,
                            inspectionDateLabel = inspectionDateLabel,
                            teamLabel = teamLabel,
                            decision = decision,
                        )
                    }
                }

                item {
                    StaggeredAnimatedItem(index = 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Visit Details")
                            ReviewCard {
                                CardHeader(
                                    title = "Visit Details",
                                    icon = Icons.Rounded.Schedule,
                                )
                                DividerLine()
                                LabelValueRow(
                                    label = stringResource(R.string.visit_type),
                                    value = visitTypeLabel(
                                        draft.visitType
                                            ?: com.moses.inspectionapp.data.model.VisitType.FIRST_VISIT,
                                    ),
                                )
                                LabelValueRow(
                                    label = stringResource(R.string.inspection_date_label),
                                    value = inspectionDateLabel,
                                )
                                LabelValueRow(
                                    label = stringResource(R.string.inspection_type_label),
                                    value = inspectionTypeLabel,
                                )
                            }
                        }
                    }
                }

                item {
                    StaggeredAnimatedItem(index = 2) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Inspection Team")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.inspection_team),
                                    icon = Icons.Rounded.Group,
                                )
                                DividerLine()
                                if (teamMembers.isEmpty()) {
                                    Text(
                                        text = "No team members selected",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextSecondary,
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        teamMembers.forEachIndexed { index, member ->
                                            TeamMemberRow(
                                                name = member,
                                                isLead = index == 0,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    StaggeredAnimatedItem(index = 3) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Faults Found")
                            ReviewCard {
                                val hasFaults = selectedFaults.isNotEmpty()
                                val headerColor = if (hasFaults) AppColors.AccentRed else AppColors.AccentGreen
                                CardHeader(
                                    title = "Faults Found",
                                    icon = if (hasFaults) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                                    iconTint = headerColor,
                                    textColor = headerColor,
                                )
                                DividerLine()
                                if (!hasFaults) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(AppColors.AccentGreenBg, RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = AppColors.AccentGreen,
                                            modifier = Modifier.size(48.dp),
                                        )
                                        Text(
                                            text = stringResource(R.string.no_faults_found),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = AppColors.AccentGreen,
                                        )
                                        Text(
                                            text = "Facility is compliant",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary,
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        selectedFaults.forEachIndexed { index, fault ->
                                            FaultRow(
                                                name = fault.name,
                                                markValue = 0,
                                            )
                                            if (index != selectedFaults.lastIndex) {
                                                DividerLine()
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = AppColors.SteelBlueTint,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "Score",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = AppColors.SteelBlue,
                                            )
                                            Text(
                                                text = "${scoreSummary.scoreOutOf100}/100",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = AppColors.SteelBlue,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    StaggeredAnimatedItem(index = 4) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Decision")
                            val config = decisionCardConfig(decision)
                            Surface(
                                color = config.background,
                                shape = CardShape,
                                border = BorderStroke(1.5.dp, config.color),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(Dimens.cardPadding),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = config.icon,
                                        contentDescription = null,
                                        tint = config.color,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "DECISION",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.2.sp,
                                            ),
                                            color = AppColors.TextSecondary,
                                        )
                                        Text(
                                            text = decisionLabel(decision),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = config.color,
                                        )
                                        Text(
                                            text = config.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextSecondary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    StaggeredAnimatedItem(index = 5) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Scoring & Charge")
                            ReviewCard {
                                CardHeader(
                                    title = "Scoring & Charge",
                                    icon = Icons.Rounded.Payments,
                                )
                                DividerLine()
                                LabelValueRow(
                                    label = "Score",
                                    value = "${scoreSummary.scoreOutOf100}/100 (${scoreSummary.rawScore}/${scoreSummary.rawMax})",
                                )
                                DividerLine()
                                val adjustmentColor = when {
                                    draft.adjustmentAmount > 0 -> AppColors.AccentGreen
                                    draft.adjustmentAmount < 0 -> AppColors.AccentRed
                                    else -> AppColors.TextPrimary
                                }
                                LabelValueRow(
                                    label = "Charge Amount",
                                    value = stringResource(R.string.rwf_amount, draft.adjustmentAmount),
                                    valueColor = adjustmentColor,
                                )
                                DividerLine(stronger = true)
                                Surface(
                                    color = AppColors.SteelBlueTint,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Total Charge",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = AppColors.TextPrimary,
                                        )
                                        Text(
                                            text = stringResource(R.string.rwf_amount, totalCharge),
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = AppColors.SteelBlue,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (draft.comments.isNotBlank()) {
                    item {
                        StaggeredAnimatedItem(index = 6) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SectionLabel(text = "Comments")
                                ReviewCard {
                                    CardHeader(
                                        title = stringResource(R.string.comments),
                                        icon = Icons.Rounded.Comment,
                                    )
                                    DividerLine()
                                    Surface(
                                        color = AppColors.PageBackground,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            if (commentItems.isEmpty()) {
                                                Text(
                                                    text = draft.comments.trim(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AppColors.TextPrimary,
                                                )
                                            } else {
                                                commentItems.forEach { item ->
                                                    BulletRow(text = item)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (draft.recommendations.isNotBlank()) {
                    item {
                        StaggeredAnimatedItem(index = 7) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SectionLabel(text = "Recommendations")
                                ReviewCard {
                                    CardHeader(
                                        title = stringResource(R.string.recommendations),
                                        icon = Icons.Rounded.Lightbulb,
                                        iconTint = AppColors.AccentGold,
                                    )
                                    DividerLine()
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (recommendationItems.isEmpty()) {
                                            Text(
                                                text = draft.recommendations.trim(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = AppColors.TextPrimary,
                                            )
                                        } else {
                                            recommendationItems.forEachIndexed { index, item ->
                                                RecommendationRow(number = index + 1, text = item)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    StaggeredAnimatedItem(index = 8) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel(text = "Inspection Photos")
                            ReviewCard {
                                CardHeader(
                                    title = stringResource(R.string.inspection_photos),
                                    icon = Icons.Rounded.PhotoCamera,
                                )
                                DividerLine()
                                if (draft.photoPaths.isEmpty()) {
                                    NoImagePlaceholder(
                                        label = stringResource(R.string.no_photo_captured),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } else {
                                    val photoScroll = rememberScrollState()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(photoScroll),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        draft.photoPaths.forEach { path ->
                                            val model = if (path.startsWith("http", ignoreCase = true)) {
                                                path
                                            } else {
                                                File(path)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = AppColors.SteelBlueTint,
                                            ) {
                                                AsyncImage(
                                                    model = model,
                                                    contentDescription = stringResource(R.string.inspection_photos),
                                                    modifier = Modifier.size(140.dp),
                                                    contentScale = ContentScale.Crop,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    item {
                        ErrorState(
                            title = stringResource(R.string.save_failed),
                            message = errorMessage.orEmpty(),
                        )
                    }
                }
            }
        }

        Surface(
            color = AppColors.CardSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = Dimens.cardMaxWidth)
                        .padding(horizontal = Dimens.screenPadding, vertical = Dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.submit_offline_hint),
                            style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    PrimaryButton(
                        text = stringResource(R.string.submit_inspection),
                        leadingIcon = Icons.Rounded.Send,
                        onClick = {
                            if (isSubmitting) return@PrimaryButton
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSubmitting = true
                            errorMessage = null
                            scope.launch {
                                val inspectionId = runCatching {
                                    withContext(Dispatchers.IO) {
                                        repository.saveInspection(draft)
                                    }
                                }.onFailure { error ->
                                    errorMessage = error.message ?: context.getString(R.string.save_failed)
                                }.getOrNull()
                                if (inspectionId != null) {
                                    DraftStore.selectedInspectionId.value = inspectionId
                                    NotificationHelper.notifyInspectionSubmitted(
                                        context = context,
                                        facilityName = facilityName,
                                        isOffline = isOffline,
                                    )
                                    if (!isOffline) {
                                        SyncManager.enqueue(context)
                                    }
                                    showSheet = true
                                }
                                isSubmitting = false
                            }
                        },
                        isLoading = isSubmitting,
                    )
                }
            }
        }

        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.NavyDark.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    color = AppColors.CardSurface,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.SteelBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            text = stringResource(R.string.submit_inspection),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = AppColors.TextPrimary,
                        )
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = AppColors.CardSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(AppColors.BorderMedium, RoundedCornerShape(50.dp)),
                )
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.AccentGreen,
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = "Inspection Saved!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = "Ready to sync when online",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Back to Home",
                    onClick = {
                        DraftStore.resetInspectionDraft()
                        showSheet = false
                        onSubmit()
                    },
                )
                SecondaryButton(
                    text = "View Inspection",
                    onClick = {
                        DraftStore.resetInspectionDraft()
                        showSheet = false
                        onSubmit()
                    },
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun HeroSummaryCard(
    facilityName: String,
    facilityTin: String,
    locationLabel: String,
    inspectionTypeLabel: String,
    inspectionDateLabel: String,
    teamLabel: String,
    decision: Decision,
) {
    Surface(
        color = AppColors.NavyDark,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "INSPECTION SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = Color.White,
                )
                DecisionBadge(decision = decision)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Storefront,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = facilityName.ifBlank { "Facility" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                    if (facilityTin.isNotBlank()) {
                        Text(
                            text = facilityTin,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }
            if (locationLabel.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeroChip(icon = Icons.Rounded.Event, label = inspectionDateLabel)
                HeroChip(icon = Icons.Rounded.Assignment, label = inspectionTypeLabel)
                HeroChip(icon = Icons.Rounded.Group, label = teamLabel)
            }
        }
    }
}

@Composable
private fun HeroChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ReviewCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = AppColors.CardSurface,
        shape = CardShape,
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun CardHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = AppColors.SteelBlue,
    textColor: Color = AppColors.TextPrimary,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
        Icon(imageVector = icon, contentDescription = null, tint = iconTint)
    }
}

@Composable
private fun DividerLine(stronger: Boolean = false) {
    val color = if (stronger) AppColors.BorderMedium else AppColors.BorderLight
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}

@Composable
private fun FaultRow(name: String, markValue: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Cancel,
            contentDescription = null,
            tint = AppColors.AccentRed,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$markValue/$MARKS_PER_QUESTION marks",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = AppColors.AccentRed,
        )
    }
}

@Composable
private fun TeamMemberRow(name: String, isLead: Boolean) {
    val initials = initialsForName(name)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = AppColors.NavyDark,
            shape = CircleShape,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextPrimary,
                )
                if (isLead) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = AppColors.AccentGold,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Text(
                text = if (isLead) "Lead Officer" else "Team Member",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun BulletRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(AppColors.TextSecondary, CircleShape),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary,
        )
    }
}

@Composable
private fun RecommendationRow(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = AppColors.SteelBlueTint,
            shape = CircleShape,
            modifier = Modifier.size(22.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.SteelBlue,
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary,
        )
    }
}

private fun parseBulletItems(text: String): List<String> {
    return text
        .lines()
        .mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                null
            } else {
                trimmed.removePrefix("-").trim()
            }
        }
}

private fun parseNumberedItems(text: String): List<String> {
    val regex = Regex("^\\d+\\.")
    return text
        .lines()
        .mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                null
            } else {
                regex.replace(trimmed, "").trim()
            }
        }
}

private fun initialsForName(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isBlank()) return "?"
    val parts = trimmed.split(" ")
    val first = parts.firstOrNull()?.firstOrNull()?.uppercase() ?: "?"
    val second = if (parts.size > 1) parts[1].firstOrNull()?.uppercase() else null
    return if (second != null) "${first}${second}" else first.toString()
}

private data class DecisionBadgeConfig(
    val color: Color,
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun decisionBadgeConfig(decision: Decision): DecisionBadgeConfig {
    return when (decision) {
        Decision.WARNING -> DecisionBadgeConfig(AppColors.StatusWarning, AppColors.StatusWarningBg, Icons.Rounded.Warning)
        Decision.CLOSURE_IMMEDIATE -> DecisionBadgeConfig(AppColors.StatusImmediate, AppColors.StatusImmediateBg, Icons.Rounded.Block)
        Decision.CLOSURE_DEADLINE -> DecisionBadgeConfig(AppColors.StatusClosure, AppColors.StatusClosureBg, Icons.Rounded.Schedule)
        Decision.PROSECUTION_RECOMMENDED -> DecisionBadgeConfig(AppColors.AccentGold, AppColors.StatusWarningBg, Icons.Rounded.Payments)
        Decision.NO_ACTION -> DecisionBadgeConfig(AppColors.StatusCompliant, AppColors.StatusCompliantBg, Icons.Rounded.CheckCircle)
    }
}

@Composable
private fun DecisionBadge(decision: Decision) {
    val config = decisionBadgeConfig(decision)
    Surface(
        color = config.background,
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                tint = config.color,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = decisionLabel(decision),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = config.color,
            )
        }
    }
}

private data class DecisionCardConfig(
    val color: Color,
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
)

@Composable
private fun decisionCardConfig(decision: Decision): DecisionCardConfig {
    return when (decision) {
        Decision.WARNING -> DecisionCardConfig(
            color = AppColors.StatusWarning,
            background = AppColors.StatusWarningBg,
            icon = Icons.Rounded.Warning,
            description = stringResource(R.string.decision_warning_desc),
        )
        Decision.CLOSURE_IMMEDIATE -> DecisionCardConfig(
            color = AppColors.StatusImmediate,
            background = AppColors.StatusImmediateBg,
            icon = Icons.Rounded.Block,
            description = stringResource(R.string.decision_closure_immediate_desc),
        )
        Decision.CLOSURE_DEADLINE -> DecisionCardConfig(
            color = AppColors.StatusClosure,
            background = AppColors.StatusClosureBg,
            icon = Icons.Rounded.Schedule,
            description = stringResource(R.string.decision_closure_deadline_desc),
        )
        Decision.PROSECUTION_RECOMMENDED -> DecisionCardConfig(
            color = AppColors.AccentGold,
            background = AppColors.StatusWarningBg,
            icon = Icons.Rounded.Payments,
            description = stringResource(R.string.decision_prosecution_desc),
        )
        Decision.NO_ACTION -> DecisionCardConfig(
            color = AppColors.StatusCompliant,
            background = AppColors.StatusCompliantBg,
            icon = Icons.Rounded.CheckCircle,
            description = stringResource(R.string.decision_no_action_desc),
        )
    }
}
