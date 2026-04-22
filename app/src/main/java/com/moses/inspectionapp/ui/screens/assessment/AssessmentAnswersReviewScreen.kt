package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PaginationRow
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.components.StaggeredAnimatedItem
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.CardShape
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.MARKS_PER_QUESTION
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.calculateAssessmentScore
import kotlin.math.roundToInt

@Composable
fun AssessmentAnswersReviewScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNext: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val faults = repository.faults.collectAsState().value
    val steps = assessmentStepLabels()
    val questions = faults.filter { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val teamMembers = draft.teamMembers.filter { it.isNotBlank() }
    val faultIds = draft.selectedFaultIds
    val totalCount = questions.size
    val faultCount = faultIds.size
    val compliantCount = (totalCount - faultCount).coerceAtLeast(0)
    val scoreSummary = calculateAssessmentScore(
        totalQuestions = totalCount,
        failedAnswers = faultCount,
    )
    val complianceRatio = if (totalCount == 0) 0f else compliantCount / totalCount.toFloat()
    val progress by animateFloatAsState(targetValue = complianceRatio, label = "complianceProgress")
    val compliancePercent = (complianceRatio * 100).roundToInt()
    val orderedQuestions = questions.filter { faultIds.contains(it.id) } + questions.filterNot { faultIds.contains(it.id) }
    val pageSize = 8
    val totalPages = if (orderedQuestions.isEmpty()) 0 else (orderedQuestions.size + pageSize - 1) / pageSize
    val (currentPage, setCurrentPage) = remember { mutableStateOf(0) }
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages - 1) {
            setCurrentPage(0)
        }
    }
    val pageStart = currentPage * pageSize
    val pageItems = orderedQuestions.drop(pageStart).take(pageSize)
    val pageFaults = pageItems.filter { faultIds.contains(it.id) }
    val pageCompliant = pageItems.filterNot { faultIds.contains(it.id) }
    val numberLookup = questions.withIndex().associate { it.value.id to (it.index + 1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.review_answers_title), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = Dimens.cardMaxWidth)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
        ) {
            item {
                StepProgressBar(
                    steps = steps,
                    currentStep = 4,
                    onStepClick = onStepClick,
                )
            }

            if (teamMembers.isNotEmpty()) {
                item {
                    StaggeredAnimatedItem(index = 0) {
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
                            ) {
                                Text(
                                    text = stringResource(R.string.inspection_team),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = AppColors.TextPrimary,
                                )
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    teamMembers.forEachIndexed { index, member ->
                                        TeamMemberChip(
                                            label = member,
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
                StaggeredAnimatedItem(index = if (teamMembers.isEmpty()) 0 else 1) {
                    Surface(
                        color = AppColors.CardSurface,
                        shape = CardShape,
                        border = BorderStroke(0.5.dp, AppColors.BorderLight),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$totalCount",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AppColors.SteelBlue,
                                    )
                                    Text(
                                        text = "Questions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppColors.TextSecondary,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp)
                                        .background(AppColors.BorderLight)
                                        .padding(horizontal = 16.dp),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = compliantCount.toString(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AppColors.AccentGreen,
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = AppColors.AccentGreen,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                        Text(
                                            text = "Compliant",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.AccentGreen,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .width(1.dp)
                                            .background(AppColors.BorderLight),
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = faultCount.toString(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AppColors.AccentRed,
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.Cancel,
                                                contentDescription = null,
                                                tint = AppColors.AccentRed,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                        Text(
                                            text = "Faults",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.AccentRed,
                                        )
                                    }
                                }
                            }
                            Surface(
                                color = AppColors.BorderLight,
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .height(6.dp)
                                        .background(AppColors.AccentGreen),
                                )
                            }
                            Text(
                                text = "$compliancePercent% compliant",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextSecondary,
                            )
                            Text(
                                text = "Score: ${scoreSummary.scoreOutOf100}/100 (${scoreSummary.rawScore}/${scoreSummary.rawMax})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = AppColors.SteelBlue,
                            )
                        }
                    }
                }
            }

            if (questions.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(R.string.no_faults),
                        message = stringResource(R.string.sync_faults_hint),
                        icon = Icons.Rounded.CheckCircle,
                    )
                }
            } else {
                if (pageFaults.isNotEmpty()) {
                    item {
                        SectionDivider(
                            label = "FAULTS FOUND (${pageFaults.size})",
                            color = AppColors.AccentRed,
                        )
                    }
                    itemsIndexed(pageFaults) { index, fault ->
                        val displayIndex = numberLookup[fault.id] ?: (pageStart + index + 1)
                        StaggeredAnimatedItem(index = index + 1) {
                            AnswerRowCard(
                                number = displayIndex,
                                question = fault.name,
                                isFault = true,
                            )
                        }
                    }
                }
                if (pageCompliant.isNotEmpty()) {
                    item {
                        SectionDivider(
                            label = "COMPLIANT (${pageCompliant.size})",
                            color = AppColors.AccentGreen,
                        )
                    }
                    itemsIndexed(pageCompliant) { index, fault ->
                        val displayIndex = numberLookup[fault.id] ?: (pageStart + index + 1)
                        StaggeredAnimatedItem(index = index + 1 + pageFaults.size) {
                            AnswerRowCard(
                                number = displayIndex,
                                question = fault.name,
                                isFault = false,
                            )
                        }
                    }
                }

                item {
                    PaginationRow(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageSelected = setCurrentPage,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = Dimens.cardMaxWidth)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.smallGap),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = AppColors.CardSurface,
                shape = CardShape,
                border = BorderStroke(0.5.dp, AppColors.BorderLight),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.TextPrimary,
                    )
                    Text(
                        text = "${scoreSummary.scoreOutOf100}/100",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppColors.SteelBlue,
                    )
                }
            }
            PrimaryButton(
                text = stringResource(R.string.next),
                onClick = onNext,
                enabled = questions.isNotEmpty(),
            )
            SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun TeamMemberChip(
    label: String,
    isLead: Boolean,
) {
    val background = if (isLead) AppColors.SteelBlueTint else AppColors.AccentGreenBg
    val textColor = if (isLead) AppColors.SteelBlue else AppColors.AccentGreen
    Surface(
        color = background,
        shape = RoundedCornerShape(50.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SectionDivider(
    label: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(color.copy(alpha = 0.3f)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = color,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(color.copy(alpha = 0.3f)),
        )
    }
}

@Composable
private fun AnswerRowCard(
    number: Int,
    question: String,
    isFault: Boolean,
) {
    val cardColor = if (isFault) AppColors.AccentRedBg else AppColors.CardSurface
    val borderColor = if (isFault) AppColors.AccentRed else AppColors.AccentGreen
    val numberBg = if (isFault) AppColors.AccentRedBg else AppColors.SteelBlueTint
    val numberColor = if (isFault) AppColors.AccentRed else AppColors.SteelBlue
    val chipColor = if (isFault) AppColors.AccentRed else AppColors.AccentGreen
    val chipBg = if (isFault) AppColors.AccentRed.copy(alpha = 0.15f) else AppColors.AccentGreen.copy(alpha = 0.15f)
    val markLabel = if (isFault) "0/$MARKS_PER_QUESTION" else "$MARKS_PER_QUESTION/$MARKS_PER_QUESTION"

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, AppColors.BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(borderColor),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = numberBg,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.size(26.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = number.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = numberColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.TextPrimary,
                    )
                    if (isFault) {
                        Text(
                            text = "0 marks recorded",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.AccentRed,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = chipBg,
                        shape = RoundedCornerShape(50.dp),
                    ) {
                        Text(
                            text = if (isFault) "No" else "Yes",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = chipColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Text(
                        text = "$markLabel marks",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isFault) FontWeight.SemiBold else FontWeight.Normal),
                        color = if (isFault) chipColor else AppColors.TextSecondary,
                    )
                }
            }
        }
    }
}
