package com.moses.inspectionapp.ui.screens.camera

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.moses.inspectionapp.R
import com.moses.inspectionapp.data.store.DraftStore
import com.moses.inspectionapp.data.store.PhotoCaptureTarget
import com.moses.inspectionapp.ui.components.AppTopBar
import com.moses.inspectionapp.ui.components.PrimaryButton
import com.moses.inspectionapp.ui.theme.Dimens
import java.io.File

@Composable
fun CameraCaptureScreen(
    onCaptured: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val hasPermission = remember { mutableStateOf(false) }
    val captureTarget = DraftStore.photoCaptureTarget.collectAsState().value

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission.value = granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(hasPermission.value) {
        if (hasPermission.value) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            val listener = Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
            }
            cameraProviderFuture.addListener(listener, executor)
        }
        onDispose { }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val title = if (captureTarget == PhotoCaptureTarget.INSPECTION) {
            stringResource(R.string.inspection_photos)
        } else {
            stringResource(R.string.facility_photo)
        }
        AppTopBar(title = title, onBack = onBack)
        if (!hasPermission.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.medium),
                verticalArrangement = Arrangement.spacedBy(Dimens.medium),
            ) {
                Text(text = stringResource(R.string.camera_permission_required))
                PrimaryButton(
                    text = stringResource(R.string.capture),
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                )
                OutlinedButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
            return
        }

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .padding(Dimens.medium),
            verticalArrangement = Arrangement.spacedBy(Dimens.small),
        ) {
            PrimaryButton(
                text = stringResource(R.string.capture),
                onClick = {
                    val prefix = if (captureTarget == PhotoCaptureTarget.INSPECTION) {
                        "inspection"
                    } else {
                        "facility"
                    }
                    val file = createPhotoFile(context, prefix)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                if (captureTarget == PhotoCaptureTarget.INSPECTION) {
                                    val updated = DraftStore.inspectionPhotoPaths.value + file.absolutePath
                                    DraftStore.inspectionPhotoPaths.value = updated
                                    DraftStore.inspectionDraft.value =
                                        DraftStore.inspectionDraft.value.copy(photoPaths = updated)
                                } else {
                                    DraftStore.facilityPhotoPath.value = file.absolutePath
                                }
                                onCaptured()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                if (captureTarget == PhotoCaptureTarget.FACILITY) {
                                    DraftStore.facilityPhotoPath.value = null
                                }
                            }
                        },
                    )
                },
            )
            OutlinedButton(onClick = onCancel) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    }
}

private fun createPhotoFile(context: Context, prefix: String): File {
    val photosDir = File(context.filesDir, "photos")
    if (!photosDir.exists()) {
        photosDir.mkdirs()
    }
    return File(photosDir, "${prefix}_${System.currentTimeMillis()}.jpg")
}
