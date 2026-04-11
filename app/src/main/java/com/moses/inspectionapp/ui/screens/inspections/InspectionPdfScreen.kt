package com.moses.inspectionapp.ui.screens.inspections

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
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.AppContainer
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.EmptyState
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.theme.Dimens
import com.moses.inspectionapp.ui.util.PdfGenerator
import java.io.File
import androidx.core.content.FileProvider
import android.content.Intent

@Composable
fun InspectionPdfScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val repository = AppContainer.repository
    val inspections = repository.inspections.collectAsState().value
    val facilities = repository.facilities.collectAsState().value
    val selectedId = DraftStore.selectedInspectionId.collectAsState().value
    val inspection = inspections.firstOrNull { it.id == selectedId }
    val facility = facilities.firstOrNull { it.id == inspection?.facilityId }
    val context = LocalContext.current
    val pdfFileState = remember { mutableStateOf<File?>(null) }

    LaunchedEffect(inspection?.id) {
        if (inspection != null) {
            pdfFileState.value = PdfGenerator.generateInspectionPdf(context, inspection, facility)
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
                    },
                    enabled = pdfFileState.value != null,
                )
            }
            PrimaryButton(text = stringResource(R.string.done), onClick = onDone)
        }
    }
}
