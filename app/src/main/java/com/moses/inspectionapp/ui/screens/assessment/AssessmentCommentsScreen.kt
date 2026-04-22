package com.moses.inspectionapp.ui.screens.assessment

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Comment
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.OfflineBanner
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.components.SectionHeader
import com.moses.inspectionapp.ui.components.SecondaryButton
import com.moses.inspectionapp.ui.components.StepHeaderCard
import com.moses.inspectionapp.ui.components.StepProgressBar
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.calculateAssessmentScore
import com.moses.inspectionapp.ui.util.assessmentStepLabels
import com.moses.inspectionapp.ui.util.copyUriToPhotoFile
import com.moses.inspectionapp.ui.util.mouseWheelScroll
import com.moses.inspectionapp.ui.util.visitTypeLabel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AssessmentCommentsScreen(
    isOffline: Boolean = false,
    lastSyncLabel: String = "",
    onNext: () -> Unit,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit = {},
    onCapturePhoto: () -> Unit = {},
) {
    val repository = AppContainer.repository
    val context = LocalContext.current
    val draft = DraftStore.inspectionDraft.collectAsState().value
    val faults = repository.faults.collectAsState().value
    var commentsState by remember { mutableStateOf(TextFieldValue(draft.comments)) }
    var recommendationsState by remember { mutableStateOf(TextFieldValue(draft.recommendations)) }
    val scrollState = rememberScrollState()
    val steps = assessmentStepLabels()
    val photoPaths = DraftStore.inspectionPhotoPaths.collectAsState().value
    val scope = rememberCoroutineScope()
    val photoItemSize = 112.dp
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = copyUriToPhotoFile(context, uri, "inspection")
                if (path != null) {
                    val updated = photoPaths + path
                    DraftStore.inspectionPhotoPaths.value = updated
                    DraftStore.inspectionDraft.value =
                        DraftStore.inspectionDraft.value.copy(photoPaths = updated)
                }
            }
        }
    }

    val questionCount = faults.count { it.active && it.inspectionTypeId == draft.inspectionTypeId }
    val scoreSummary = calculateAssessmentScore(
        totalQuestions = questionCount,
        failedAnswers = draft.selectedFaultIds.size,
    )
    val chargeAmount = draft.adjustmentAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(AppColors.PageBackground),
    ) {
        AppTopBar(title = stringResource(R.string.comments), onBack = onBack)
        OfflineBanner(lastSync = lastSyncLabel, isVisible = isOffline)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.screenPadding),
            contentAlignment = Alignment.TopCenter,
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
                    currentStep = 7,
                    onStepClick = onStepClick,
                )
                StepHeaderCard(
                    title = stringResource(R.string.comments_recommendations),
                    subtitle = stringResource(R.string.step_notes_subtitle),
                )

                Surface(
                    color = AppColors.NavyDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                    ) {
                        Icon(imageVector = Icons.Rounded.Storefront, contentDescription = null, tint = AppColors.TextOnDark)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = draft.facilityName, style = MaterialTheme.typography.titleMedium, color = AppColors.TextOnDark)
                            Text(
                                text = visitTypeLabel(draft.visitType ?: com.moses.inspectionapp.data.model.VisitType.FIRST_VISIT),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextOnDarkMuted,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${scoreSummary.scoreOutOf100}/100",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = AppColors.TextOnDark,
                            )
                            Text(
                                text = "Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextOnDarkMuted,
                            )
                            if (chargeAmount != 0) {
                                Text(
                                    text = "Charge: ${stringResource(R.string.rwf_amount, chargeAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextOnDarkMuted,
                                )
                            }
                        }
                    }
                }

                SectionHeader(title = stringResource(R.string.comments_header))
                ExpandableListTextField(
                    value = commentsState,
                    onValueChange = { commentsState = it },
                    label = stringResource(R.string.comments),
                    leadingIcon = Icons.Rounded.Comment,
                    listStyle = ListStyle.BULLET,
                )
                Text(
                    text = stringResource(R.string.char_count, commentsState.text.length, 500),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.align(Alignment.End),
                )

                SectionHeader(title = stringResource(R.string.recommendations_header))
                ExpandableListTextField(
                    value = recommendationsState,
                    onValueChange = { recommendationsState = it },
                    label = stringResource(R.string.recommendations),
                    leadingIcon = Icons.Rounded.Lightbulb,
                    listStyle = ListStyle.NUMBERED,
                )
                Text(
                    text = stringResource(R.string.char_count, recommendationsState.text.length, 500),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.align(Alignment.End),
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        stringResource(R.string.quick_rec_ventilation),
                        stringResource(R.string.quick_rec_storage),
                    ).forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = AppColors.CardSurface,
                            modifier = Modifier.border(1.dp, AppColors.SteelBlue, RoundedCornerShape(50.dp)),
                            onClick = {
                                recommendationsState = appendListItem(
                                    recommendationsState,
                                    suggestion,
                                    ListStyle.NUMBERED,
                                )
                            },
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.SteelBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                SectionHeader(title = stringResource(R.string.inspection_photos))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    photoPaths.forEach { path ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.CardSurface,
                            modifier = Modifier
                                .size(photoItemSize)
                                .border(1.dp, AppColors.BorderLight, RoundedCornerShape(12.dp)),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = stringResource(R.string.inspection_photos),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                IconButton(
                                    onClick = {
                                        val updated = photoPaths.filterNot { it == path }
                                        DraftStore.inspectionPhotoPaths.value = updated
                                        DraftStore.inspectionDraft.value =
                                            DraftStore.inspectionDraft.value.copy(photoPaths = updated)
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.remove),
                                        tint = AppColors.TextOnDark,
                                    )
                                }
                            }
                        }
                    }
                    PhotoActionCard(
                        title = stringResource(R.string.capture),
                        subtitle = stringResource(R.string.camera),
                        icon = Icons.Rounded.PhotoCamera,
                        onClick = onCapturePhoto,
                        modifier = Modifier.size(photoItemSize),
                    )
                    PhotoActionCard(
                        title = stringResource(R.string.upload),
                        subtitle = stringResource(R.string.gallery),
                        icon = Icons.Rounded.PhotoLibrary,
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.size(photoItemSize),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = stringResource(R.string.next),
                    onClick = {
                        DraftStore.inspectionDraft.value = DraftStore.inspectionDraft.value.copy(
                            comments = commentsState.text.trim(),
                            recommendations = recommendationsState.text.trim(),
                        )
                        onNext()
                    },
                )
                SecondaryButton(text = stringResource(R.string.back), onClick = onBack)
            }
        }
    }
}

private enum class ListStyle {
    BULLET,
    NUMBERED,
}

@Composable
private fun ExpandableListTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    listStyle: ListStyle,
    maxChars: Int = 500,
) {
    var isFocused by remember { mutableStateOf(false) }
    val readOnlyContainer = AppColors.InputSurface
    val focusedContainer = AppColors.FocusedInput
    val unfocusedContainer = readOnlyContainer

    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            val formatted = applyListFormatting(value, next, listStyle)
            if (formatted.text.length <= maxChars || formatted.text.length < value.text.length) {
                onValueChange(formatted)
            }
        },
        label = { Text(label) },
        leadingIcon = {
            Icon(imageVector = leadingIcon, contentDescription = null, tint = AppColors.TextSecondary)
        },
        singleLine = false,
        minLines = if (isFocused) 6 else 4,
        maxLines = if (isFocused) 12 else 6,
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences,
        ),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.SteelBlue,
            unfocusedBorderColor = AppColors.BorderLight,
            focusedLabelColor = AppColors.SteelBlue,
            unfocusedLabelColor = AppColors.TextSecondary,
            cursorColor = AppColors.SteelBlue,
            focusedContainerColor = focusedContainer,
            unfocusedContainerColor = unfocusedContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .heightIn(min = if (isFocused) 180.dp else 140.dp)
            .onFocusChanged { isFocused = it.isFocused },
    )
}

@Composable
private fun PhotoActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AppColors.InputSurface,
        modifier = modifier.border(1.dp, AppColors.BorderLight, RoundedCornerShape(14.dp)),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppColors.SteelBlueTintMid, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = AppColors.SteelBlue)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = AppColors.TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
        }
    }
}

private fun applyListFormatting(
    oldValue: TextFieldValue,
    newValue: TextFieldValue,
    style: ListStyle,
): TextFieldValue {
    if (newValue.text.isEmpty()) return newValue

    var updated = newValue
    if (oldValue.text.isEmpty() && !startsWithListPrefix(newValue.text, style)) {
        val prefix = initialPrefix(style)
        val newText = prefix + newValue.text
        val delta = prefix.length
        updated = newValue.copy(
            text = newText,
            selection = TextRange(
                start = newValue.selection.start + delta,
                end = newValue.selection.end + delta,
            ),
        )
    }

    val isAppendingNewLine =
        updated.text.length > oldValue.text.length &&
            updated.text.endsWith("\n") &&
            updated.selection.start == updated.text.length &&
            updated.selection.end == updated.text.length
    if (isAppendingNewLine) {
        val nextPrefix = when (style) {
            ListStyle.BULLET -> "- "
            ListStyle.NUMBERED -> "${nextNumber(updated.text)}. "
        }
        val newText = updated.text + nextPrefix
        updated = updated.copy(
            text = newText,
            selection = TextRange(newText.length),
        )
    }

    return updated
}

private fun startsWithListPrefix(text: String, style: ListStyle): Boolean {
    val trimmed = text.trimStart()
    return when (style) {
        ListStyle.BULLET -> trimmed.startsWith("- ")
        ListStyle.NUMBERED -> Regex("^\\d+\\.\\s").containsMatchIn(trimmed)
    }
}

private fun initialPrefix(style: ListStyle): String {
    return when (style) {
        ListStyle.BULLET -> "- "
        ListStyle.NUMBERED -> "1. "
    }
}

private fun nextNumber(text: String): Int {
    val regex = Regex("^\\s*(\\d+)\\.")
    val lastNumber = text.lines()
        .mapNotNull { line -> regex.find(line)?.groupValues?.getOrNull(1)?.toIntOrNull() }
        .lastOrNull()
    return (lastNumber ?: 0) + 1
}

private fun appendListItem(
    current: TextFieldValue,
    item: String,
    style: ListStyle,
): TextFieldValue {
    val trimmed = current.text.trimEnd()
    val prefix = if (trimmed.isBlank()) {
        initialPrefix(style)
    } else {
        val spacer = if (trimmed.endsWith("\n")) "" else "\n"
        spacer + when (style) {
            ListStyle.BULLET -> "- "
            ListStyle.NUMBERED -> "${nextNumber(trimmed)}. "
        }
    }
    val newText = trimmed + prefix + item
    return current.copy(text = newText, selection = TextRange(newText.length))
}
