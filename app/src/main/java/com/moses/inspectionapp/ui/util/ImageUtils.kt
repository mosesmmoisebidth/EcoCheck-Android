package com.moses.inspectionapp.ui.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun copyUriToPhotoFile(context: Context, uri: Uri, prefix: String): String? {
    return withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri)
        val extension = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/jpg", "image/jpeg" -> "jpg"
            else -> "jpg"
        }
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        val file = File(photosDir, "${prefix}_${System.currentTimeMillis()}.$extension")
        runCatching {
            resolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            file.absolutePath
        }.getOrNull()
    }
}
