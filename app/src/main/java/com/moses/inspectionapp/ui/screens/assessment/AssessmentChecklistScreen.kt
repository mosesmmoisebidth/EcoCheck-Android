package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.mouseWheelScroll

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AssessmentChecklistScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNext: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
) {
    val repository = AppContainer.repository
    val faults = repository.faults.collectAsState().value
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val selected = remember { mutableStateOf(draft.selectedFaultIds) }
    val currentIndex = remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val steps = assessmentStepLabels()
    val typeFaults = faults.filter { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val subtotal = faults.filter { selected.value.contains(it.id) }.sumOf { it.standardFine }

    LaunchedEffect(draft.inspectionTypeId) {
        currentIndex.value = 0
        selected.value = draft.selectedFaultIds
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.fault_checklist), onBack = onBack)
            OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .mouseWheelScroll(scrollState)
                    .verticalScroll(scrollState)
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.sectionGap)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
            ) {
                StepProgressBar(
                    steps = steps,
                    currentStep = 4,
                    onStepClick = onStepClick,
                )
                StepHeaderCard(
                    title = stringResource(R.string.fault_checklist),
                    subtitle = stringResource(R.string.step_faults_subtitle),
                )

                if (typeFaults.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.no_faults),
                        message = stringResource(R.string.sync_faults_hint),
                        icon = Icons.Rounded.CheckCircle,
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.question_progress,
                            (currentIndex.value + 1).coerceAtMost(typeFaults.size),
                            typeFaults.size,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextSecondary,
                    )
                    AnimatedContent(
                        targetState = currentIndex.value,
                        transitionSpec = {
                            val forward = targetState > initialState
                            val enter = slideInHorizontally(
                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                initialOffsetX = { if (forward) it else -it },
                            ) + fadeIn(animationSpec = tween(220))
                            val exit = slideOutHorizontally(
                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                targetOffsetX = { if (forward) -it else it },
                            ) + fadeOut(animationSpec = tween(220))
                            enter togetherWith exit
                        },
                        label = "questionSlide",
                    ) { targetIndex ->
                        val currentFault = typeFaults.getOrNull(targetIndex)
                        if (currentFault != null) {
                            val yesInteraction = remember(currentFault.id) { MutableInteractionSource() }
                            val noInteraction = remember(currentFault.id) { MutableInteractionSource() }
                            val yesPressed by yesInteraction.collectIsPressedAsState()
                            val noPressed by noInteraction.collectIsPressedAsState()
                            val yesScale by animateFloatAsState(
                                targetValue = if (yesPressed) 0.97f else 1f,
                                animationSpec = tween(120),
                                label = "yesScale",
                            )
                            val noScale by animateFloatAsState(
                                targetValue = if (noPressed) 0.97f else 1f,
                                animationSpec = tween(120),
                                label = "noScale",
                            )

                            Surface(
                                color = AppColors.CardSurface,
                                shadowElevation = 1.dp,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(16.dp)),
                            ) {
                                Column(
                                    modifier = Modifier.padding(Dimens.cardPadding),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = currentFault.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppColors.TextPrimary,
                                    )
                                    Text(
                                        text = stringResource(R.string.answer_yes_no),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextSecondary,
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Button(
                                            onClick = {
                                                selected.value = selected.value - currentFault.id
                                                DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                                    selectedFaultIds = selected.value,
                                                )
                                                if (currentIndex.value < typeFaults.lastIndex) {
                                                    currentIndex.value += 1
                                                } else {
                                                    onNext()
                                                }
                                            },
                                            interactionSource = yesInteraction,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AppColors.SteelBlue,
                                                contentColor = Color.White,
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .graphicsLayer(scaleX = yesScale, scaleY = yesScale),
                                        ) {
                                            Text(
                                                text = stringResource(R.string.answer_yes),
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                selected.value = selected.value + currentFault.id
                                                DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                                    selectedFaultIds = selected.value,
                                                )
                                                if (currentIndex.value < typeFaults.lastIndex) {
                                                    currentIndex.value += 1
                                                } else {
                                                    onNext()
                                                }
                                            },
                                            interactionSource = noInteraction,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AppColors.AccentOrange,
                                                contentColor = Color.White,
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .graphicsLayer(scaleX = noScale, scaleY = noScale),
                                        ) {
                                            Text(
                                                text = stringResource(R.string.answer_no),
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                    if (currentIndex.value > 0) {
                                        SecondaryButton(
                                            text = stringResource(R.string.previous_question),
                                            onClick = { currentIndex.value -= 1 },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            }
        }

        Surface(
            color = AppColors.NavyDark,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.medium),
            ) {
                Text(
                    text = stringResource(R.string.subtotal_heading),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextOnDarkMuted,
                )
                Text(
                    text = stringResource(R.string.rwf_amount, subtotal),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.faults_selected, selected.value.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextOnDarkMuted,
                )
            }
        }
    }
}
