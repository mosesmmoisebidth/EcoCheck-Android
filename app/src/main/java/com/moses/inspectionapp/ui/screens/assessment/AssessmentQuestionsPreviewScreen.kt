package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
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
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import kotlin.math.max
import kotlin.math.roundToInt

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
    val inspectionTypes = repository.inspectionTypes.collectAsState().value
    val inspectionType = inspectionTypes.firstOrNull { it.id == draft.inspectionTypeId }
    val steps = assessmentStepLabels()
    val questions = faults.filter { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val questionCount = questions.size
    val finePerFault = questions.firstOrNull()?.standardFine ?: 0
    val estimateMinutes = max(1, (questionCount * 0.5f).roundToInt())
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
        LazyColumn(
            modifier = Modifier
                .weight(1f)
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

            item {
                StaggeredAnimatedItem(index = 0) {
                    Surface(
                        color = AppColors.NavyDark,
                        shape = CardShape,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp),
                            ) {
                                Text(
                                    text = "PREVIEW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.2f.sp,
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                            Text(
                                text = inspectionType?.name?.let { "$it Checklist" }
                                    ?: stringResource(R.string.question_preview_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                PreviewChip(
                                    icon = Icons.Rounded.FormatListBulleted,
                                    label = "$questionCount Questions",
                                )
                                PreviewChip(
                                    icon = Icons.Rounded.Schedule,
                                    label = "~$estimateMinutes min",
                                )
                                PreviewChip(
                                    icon = Icons.Rounded.Payments,
                                    label = "Fine per fault: ${stringResource(R.string.rwf_amount, finePerFault)}",
                                )
                            }
                        }
                    }
                }
            }

            if (questions.isEmpty()) {
                item {
                    StaggeredAnimatedItem(index = 1) {
                        EmptyState(
                            title = stringResource(R.string.no_faults),
                            message = stringResource(R.string.sync_faults_hint),
                            icon = Icons.Rounded.CheckCircle,
                        )
                    }
                }
            } else {
                itemsIndexed(pageItems) { index, fault ->
                    val displayIndex = pageStart + index + 1
                    StaggeredAnimatedItem(index = index + 1) {
                        Surface(
                            color = AppColors.CardSurface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            shadowElevation = 1.dp,
                            border = BorderStroke(0.5.dp, AppColors.BorderLight),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    color = AppColors.SteelBlueTint,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp),
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
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = AppColors.TextPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                Surface(
                                    color = AppColors.StatusWarningBg,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.rwf_amount, fault.standardFine),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppColors.StatusWarning,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
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
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.smallGap),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.start_questions),
                leadingIcon = Icons.Rounded.PlayArrow,
                onClick = onStartQuestions,
                enabled = questions.isNotEmpty(),
            )
            SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
private fun PreviewChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp),
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
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}
