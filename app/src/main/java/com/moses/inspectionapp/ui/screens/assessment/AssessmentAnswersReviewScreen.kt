package com.moses.inspectionapp.ui.screens.assessment

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.components.PaginationRow
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.CardShape
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.mouseWheelScroll

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
    val scrollState = rememberScrollState()
    val steps = assessmentStepLabels()
    val questions = faults.filter { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val noFaultIds = draft.selectedFaultIds
    val totalFine = questions.filter { noFaultIds.contains(it.id) }.sumOf { it.standardFine }
    val faultCount = noFaultIds.size
    val pageSize = 8
    val totalPages = if (questions.isEmpty()) 0 else (questions.size + pageSize - 1) / pageSize
    val (currentPage, setCurrentPage) = remember { mutableStateOf(0) }
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages - 1) {
            setCurrentPage(0)
        }
    }
    val pageStart = currentPage * pageSize
    val pageItems = questions.drop(pageStart).take(pageSize)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.review_answers_title), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseWheelScroll(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
        ) {
            StepProgressBar(
                steps = steps,
                currentStep = 4,
                onStepClick = onStepClick,
            )
            StepHeaderCard(
                title = stringResource(R.string.review_answers_title),
                subtitle = stringResource(R.string.review_answers_subtitle),
            )

            if (questions.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.no_faults),
                    message = stringResource(R.string.sync_faults_hint),
                    icon = Icons.Rounded.CheckCircle,
                )
            } else {
                Surface(
                    color = AppColors.SteelBlueTint,
                    shape = CardShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.BorderLight, CardShape),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.questions_count, questions.size),
                            style = MaterialTheme.typography.titleSmall,
                            color = AppColors.TextPrimary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.faults_selected, faultCount),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextSecondary,
                            )
                            Text(
                                text = stringResource(R.string.rwf_amount, totalFine),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = AppColors.TextPrimary,
                            )
                        }
                    }
                }

                Surface(
                    color = AppColors.CardSurface,
                    shape = CardShape,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        pageItems.forEachIndexed { index, fault ->
                            val displayIndex = pageStart + index + 1
                            val isFault = noFaultIds.contains(fault.id)
                            val answerText = if (isFault) stringResource(R.string.answer_no) else stringResource(R.string.answer_yes)
                            val answerColor = if (isFault) AppColors.StatusWarning else AppColors.StatusCompliant
                            val answerBg = if (isFault) AppColors.StatusWarningBg else AppColors.StatusCompliantBg
                            val fineLabel = if (isFault) {
                                stringResource(R.string.fine_label, stringResource(R.string.rwf_amount, fault.standardFine))
                            } else {
                                stringResource(R.string.fine_label, stringResource(R.string.rwf_amount, 0))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Surface(
                                        color = AppColors.SteelBlueTint,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "$displayIndex",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = AppColors.SteelBlue,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                    Text(
                                        text = fault.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextPrimary,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        color = answerBg,
                                        shape = RoundedCornerShape(50.dp),
                                    ) {
                                        Text(
                                            text = answerText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = answerColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        )
                                    }
                                    Text(
                                        text = fineLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppColors.TextSecondary,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                PaginationRow(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPageSelected = setCurrentPage,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            PrimaryButton(
                text = stringResource(R.string.next),
                onClick = onNext,
                enabled = questions.isNotEmpty(),
            )
            SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
