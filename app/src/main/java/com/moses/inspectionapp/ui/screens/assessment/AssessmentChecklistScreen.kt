package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.components.StaggeredAnimatedItem
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val answered = remember { mutableStateOf(draft.answeredFaultIds + draft.selectedFaultIds) }
    val currentIndex = remember { mutableStateOf(0) }
    val steps = assessmentStepLabels()
    val typeFaults = faults.filter { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val subtotalTarget = faults.filter { selected.value.contains(it.id) }.sumOf { it.standardFine }
    val subtotal by animateIntAsState(targetValue = subtotalTarget, label = "subtotal")
    val faultCount by animateIntAsState(targetValue = selected.value.size, label = "faultCount")
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isAdvancing = remember { mutableStateOf(false) }

    LaunchedEffect(draft.inspectionTypeId) {
        currentIndex.value = 0
        selected.value = draft.selectedFaultIds
        answered.value = draft.answeredFaultIds + draft.selectedFaultIds
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.fault_checklist), onBack = onBack)
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

                if (typeFaults.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.no_faults),
                            message = stringResource(R.string.sync_faults_hint),
                            icon = Icons.Rounded.CheckCircle,
                        )
                    }
                } else {
                    item {
                        StaggeredAnimatedItem(index = 0) {
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
                                    val questionNumber = (currentIndex.value + 1).coerceAtMost(typeFaults.size)
                                    val progressRatio = questionNumber / typeFaults.size.toFloat()
                                    val progress by animateFloatAsState(
                                        targetValue = progressRatio,
                                        animationSpec = tween(200),
                                        label = "questionProgress",
                                    )
                                    val hasAnswered = answered.value.contains(currentFault.id)
                                    val isNoSelected = selected.value.contains(currentFault.id)
                                    val isYesSelected = hasAnswered && !isNoSelected

                                    Surface(
                                        color = AppColors.CardSurface,
                                        shape = RoundedCornerShape(20.dp),
                                        shadowElevation = 3.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(20.dp)),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = "Question $questionNumber of ${typeFaults.size}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AppColors.TextSecondary,
                                                )
                                                Surface(
                                                    color = AppColors.SteelBlueTint,
                                                    shape = RoundedCornerShape(50.dp),
                                                    modifier = Modifier
                                                        .width(120.dp)
                                                        .height(6.dp),
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(progress)
                                                            .height(6.dp)
                                                            .background(AppColors.SteelBlue),
                                                    )
                                                }
                                            }

                                            Text(
                                                text = currentFault.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = AppColors.TextPrimary,
                                            )

                                            Surface(
                                                color = AppColors.SteelBlueTint,
                                                shape = RoundedCornerShape(8.dp),
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Info,
                                                        contentDescription = null,
                                                        tint = AppColors.SteelBlue,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                    Text(
                                                        text = "Selecting No will add a fault and apply the standard fine",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AppColors.TextSecondary,
                                                    )
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                AnswerChoiceCard(
                                                    title = stringResource(R.string.answer_yes),
                                                    subtitle = "No fault - compliant",
                                                    selected = isYesSelected,
                                                    accent = AppColors.AccentGreen,
                                                    accentBg = AppColors.AccentGreenBg,
                                                    icon = Icons.Rounded.CheckCircle,
                                                    onClick = {
                                                        if (isAdvancing.value) return@AnswerChoiceCard
                                                        isAdvancing.value = true
                                                        scope.launch {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            selected.value = selected.value - currentFault.id
                                                            answered.value = answered.value + currentFault.id
                                                            DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                                                selectedFaultIds = selected.value,
                                                                answeredFaultIds = answered.value,
                                                            )
                                                            delay(320)
                                                            if (currentIndex.value < typeFaults.lastIndex) {
                                                                currentIndex.value += 1
                                                            } else {
                                                                onNext()
                                                            }
                                                            isAdvancing.value = false
                                                        }
                                                    },
                                                )
                                                AnswerChoiceCard(
                                                    title = stringResource(R.string.answer_no),
                                                    subtitle = "Fine: ${stringResource(R.string.rwf_amount, currentFault.standardFine)} will be applied",
                                                    selected = isNoSelected,
                                                    accent = AppColors.AccentRed,
                                                    accentBg = AppColors.AccentRedBg,
                                                    icon = Icons.Rounded.Cancel,
                                                    onClick = {
                                                        if (isAdvancing.value) return@AnswerChoiceCard
                                                        isAdvancing.value = true
                                                        scope.launch {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            selected.value = selected.value + currentFault.id
                                                            answered.value = answered.value + currentFault.id
                                                            DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                                                                selectedFaultIds = selected.value,
                                                                answeredFaultIds = answered.value,
                                                            )
                                                            delay(320)
                                                            if (currentIndex.value < typeFaults.lastIndex) {
                                                                currentIndex.value += 1
                                                            } else {
                                                                onNext()
                                                            }
                                                            isAdvancing.value = false
                                                        }
                                                    },
                                                )

                                                AnimatedVisibility(visible = isNoSelected) {
                                                    Surface(
                                                        color = AppColors.AccentRed.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(50.dp),
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Warning,
                                                                contentDescription = null,
                                                                tint = AppColors.AccentRed,
                                                                modifier = Modifier.size(14.dp),
                                                            )
                                                            Text(
                                                                text = "${stringResource(R.string.rwf_amount, currentFault.standardFine)} fault added",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = AppColors.AccentRed,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        val canGoPrev = currentIndex.value > 0
                        val canGoNext = currentIndex.value < typeFaults.lastIndex
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                color = AppColors.SteelBlueTint.copy(alpha = if (canGoPrev) 1f else 0.4f),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp),
                                onClick = { if (canGoPrev) currentIndex.value -= 1 },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowLeft,
                                        contentDescription = null,
                                        tint = AppColors.SteelBlue.copy(alpha = if (canGoPrev) 1f else 0.4f),
                                    )
                                }
                            }
                            Text(
                                text = "${(currentIndex.value + 1).coerceAtMost(typeFaults.size)} / ${typeFaults.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextSecondary,
                            )
                            Surface(
                                color = AppColors.SteelBlueTint.copy(alpha = if (canGoNext) 1f else 0.4f),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp),
                                onClick = { if (canGoNext) currentIndex.value += 1 },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = AppColors.SteelBlue.copy(alpha = if (canGoNext) 1f else 0.4f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.smallGap),
            ) {
                SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            }
        }

        Surface(
            color = AppColors.NavyDark,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "SUBTOTAL",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = AppColors.TextOnDarkMuted,
                    )
                    Text(
                        text = stringResource(R.string.rwf_amount, subtotal),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.2f)),
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FAULTS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = AppColors.TextOnDarkMuted,
                    )
                    Text(
                        text = faultCount.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextOnDarkMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    accentBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (selected) accentBg else AppColors.CardSurface,
        label = "answerBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accent else AppColors.BorderLight,
        label = "answerBorder",
    )
    val iconBg by animateColorAsState(
        targetValue = if (selected) accent else accent.copy(alpha = 0.1f),
        label = "answerIconBg",
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) Color.White else accent,
        label = "answerIconTint",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        color = background,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.5.dp,
            color = borderColor,
        ),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = iconBg,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selected) accent else AppColors.TextPrimary,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = if (selected) accent else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (selected) accent else AppColors.BorderMedium),
                modifier = Modifier.size(18.dp),
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color.White, CircleShape),
                    )
                }
            }
        }
    }
}
