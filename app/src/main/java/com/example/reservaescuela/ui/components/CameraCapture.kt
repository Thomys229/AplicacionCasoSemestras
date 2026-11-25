package com.example.reservaescuela.ui.components

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun rememberCameraCapture(
    onCaptured: (PickedFile) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }

    val contract = remember {
        object : ActivityResultContracts.TakePicture() {
            override fun createIntent(context: Context, input: Uri): Intent {
                return super.createIntent(context, input).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    clipData = ClipData.newUri(context.contentResolver, "Foto", input)
                }
            }
        }
    }

    val camera = rememberLauncherForActivityResult(contract) { saved ->
        val path = pendingPath
        pendingPath = null
        if (path != null) {
            val file = File(path)
            if (saved && file.exists() && file.length() > 0L) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                onCaptured(PickedFile(uri.toString(), file.name, "image/jpeg"))
            } else {
                file.delete()
                if (saved) onError("No se pudo guardar la foto.")
            }
        }
    }

    return {
        var file: File? = null
        try {
            val folder = File(context.filesDir, "fotos")
            if (!folder.exists() && !folder.mkdirs()) error("No se pudo crear la carpeta")
            file = File.createTempFile("foto_", ".jpg", folder)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            pendingPath = file.absolutePath
            camera.launch(uri)
        } catch (e: Exception) {
            file?.delete()
            pendingPath = null
            onError("No se pudo abrir la cámara.")
        }
    }
}

fun deleteCameraPhoto(context: Context, fileUri: String, fileName: String) {
    val uri = Uri.parse(fileUri)
    if (uri.scheme != "content" || uri.authority != "${context.packageName}.fileprovider") return
    if (!fileName.startsWith("foto_") || !fileName.endsWith(".jpg")) return

    val folder = File(context.filesDir, "fotos").canonicalFile
    val file = File(folder, fileName).canonicalFile
    if (file.parentFile == folder) file.delete()
}
