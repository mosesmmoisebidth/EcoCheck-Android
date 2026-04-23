package com.moses.inspectionapp.ui.screens.inspections

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.model.Fault
import com.moses.inspectionapp.data.model.Inspection
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.MARKS_PER_QUESTION
import com.moses.inspectionapp.ui.util.PdfGenerator
import java.io.File

@Composable
fun InspectionPdfScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val inspections = repository.inspections.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val allFaults = repository.faults.collectAsState().value
    val inspectionTypes = repository.inspectionTypes.collectAsState().value
    val selectedId = DraftStore.selectedInspectionId.collectAsState().value
    val inspection = inspections.firstOrNull { it.id == selectedId }
    val facility = facilities.firstOrNull { facilityItem ->
        facilityItem.id == inspection?.facilityId || facilityItem.serverId == inspection?.facilityId
    }
    val inspectionTypeName = inspectionTypes
        .firstOrNull { it.id == inspection?.inspectionTypeId }
        ?.name ?: stringResource(R.string.inspection)
    val context = LocalContext.current
    val pdfFileState = remember { mutableStateOf<File?>(null) }
    val statusMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(inspection?.id, allFaults, inspectionTypeName) {
        if (inspection != null) {
            val selectedFaultIds = repository
                .getInspectionFaults(inspection.id)
                .map { it.id }
                .toSet()
            val questions = resolveInspectionQuestions(
                allFaults = allFaults,
                inspection = inspection,
            )
            val questionAnswers = questions.mapIndexed { index, fault ->
                val isFault = selectedFaultIds.contains(fault.id)
                PdfGenerator.QuestionAnswer(
                    number = index + 1,
                    question = fault.name,
                    answer = if (isFault) "No" else "Yes",
                    marksAwarded = if (isFault) 0 else MARKS_PER_QUESTION,
                    maxMarks = MARKS_PER_QUESTION,
                )
            }
            pdfFileState.value = PdfGenerator.generateInspectionPdf(
                context = context,
                inspection = inspection,
                facility = facility,
                inspectionTypeName = inspectionTypeName,
                questionAnswers = questionAnswers,
            )
            statusMessage.value = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = stringResource(R.string.inspection_pdf), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.medium, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (inspection == null) {
                EmptyState(
                    title = stringResource(R.string.no_inspection_selected),
                    message = stringResource(R.string.select_inspection_from_list),
                )
            } else {
                Text(text = stringResource(R.string.pdf_ready))
                Text(text = inspection.facilityName)
                PrimaryButton(
                    text = stringResource(R.string.share_pdf),
                    onClick = {
                        val file = pdfFileState.value ?: return@PrimaryButton
                        sharePdf(context = context, file = file)
                    },
                    enabled = pdfFileState.value != null,
                )
                PrimaryButton(
                    text = stringResource(R.string.download_pdf),
                    onClick = {
                        val file = pdfFileState.value ?: return@PrimaryButton
                        val saved = downloadPdfToDevice(context = context, sourceFile = file)
                        statusMessage.value = if (saved) {
                            context.getString(R.string.pdf_saved_downloads)
                        } else {
                            context.getString(R.string.pdf_save_failed)
                        }
                    },
                    enabled = pdfFileState.value != null,
                )
                statusMessage.value?.let { message ->
                    Text(text = message)
                }
            }
            PrimaryButton(text = stringResource(R.string.done), onClick = onDone)
        }
    }
}

private fun resolveInspectionQuestions(
    allFaults: List<Fault>,
    inspection: Inspection,
): List<Fault> {
    val typeId = inspection.inspectionTypeId
    if (typeId.isNullOrBlank()) return emptyList()
    return allFaults.filter { it.active && it.inspectionTypeId == typeId }
}

private fun sharePdf(context: android.content.Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_pdf)))
}

private fun downloadPdfToDevice(
    context: android.content.Context,
    sourceFile: File,
): Boolean {
    return runCatching {
        val fileName = sourceFile.name
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/EcoCheck")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val downloadUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return false

        resolver.openOutputStream(downloadUri)?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        } ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val doneValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            resolver.update(downloadUri, doneValues, null, null)
        }
        true
    }.getOrDefault(false)
}
