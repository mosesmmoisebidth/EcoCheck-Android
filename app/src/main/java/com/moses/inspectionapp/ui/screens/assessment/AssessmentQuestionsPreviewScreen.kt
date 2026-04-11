package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
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
fun AssessmentQuestionsPreviewScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onStartQuestions: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val faults = repository.faults.collectAsState().value
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val scrollState = rememberScrollState()
    val steps = assessmentStepLabels()
    val questions = faults.filter { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val totalFine = questions.sumOf { it.standardFine }
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
        AppTopBar(title = stringResource(R.string.question_preview_title), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Column(
            modifier = Modifier
                .weight(1f)
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
                title = stringResource(R.string.question_preview_title),
                subtitle = stringResource(R.string.question_preview_subtitle),
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
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                        Text(
                            text = stringResource(R.string.questions_count, questions.size),
                            style = MaterialTheme.typography.titleSmall,
                            color = AppColors.TextPrimary,
                        )
                        Text(
                            text = stringResource(R.string.question_preview_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.total_fine),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextSecondary,
                            )
                            Text(
                                text = stringResource(R.string.rwf_amount, totalFine),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
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
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        pageItems.forEachIndexed { index, fault ->
                            val displayIndex = pageStart + index + 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        color = AppColors.SteelBlueTint,
                                        shape = CardShape,
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
                                Text(
                                    text = stringResource(
                                        R.string.fine_label,
                                        stringResource(R.string.rwf_amount, fault.standardFine),
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextSecondary,
                                )
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
                text = stringResource(R.string.start_questions),
                onClick = onStartQuestions,
                enabled = questions.isNotEmpty(),
            )
            SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}
