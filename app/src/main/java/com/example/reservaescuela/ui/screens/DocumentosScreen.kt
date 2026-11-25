package com.example.reservaescuela.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.reservaescuela.model.Document
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.components.EmptyText
import com.example.reservaescuela.ui.components.PickedFile
import com.example.reservaescuela.ui.components.deleteCameraPhoto
import com.example.reservaescuela.ui.components.fileTypeLabel
import com.example.reservaescuela.ui.components.isPdfOrWord
import com.example.reservaescuela.ui.components.openFile
import com.example.reservaescuela.ui.components.pdfWordTypes
import com.example.reservaescuela.ui.components.rememberCameraCapture
import com.example.reservaescuela.ui.components.rememberFilePicker

@Composable
fun DocumentosScreen(vm: AppViewModel, show: (String) -> Unit) {
    val context = LocalContext.current
    val user = vm.currentUser ?: return
    val mine = vm.documents.filter { it.owner == user.email }.sortedByDescending { it.id }
    var toDelete by remember { mutableStateOf<Document?>(null) }

    fun guardar(file: PickedFile, message: String) {
        vm.addDocument(Document(System.currentTimeMillis(), "", file.name, file.type, file.uri))
        show(message)
    }

    val pickDocument = rememberFilePicker(pdfWordTypes) { file ->
        if (isPdfOrWord(file.type)) {
            guardar(file, "Documento cargado.")
        } else {
            show("Selecciona un PDF o Word.")
        }
    }

    val pickImage = rememberFilePicker(arrayOf("image/*")) { file ->
        if (file.type.startsWith("image/")) {
            guardar(file, "Foto agregada.")
        } else {
            show("Selecciona una imagen.")
        }
    }

    val takePhoto = rememberCameraCapture(
        onCaptured = { guardar(it, "Foto guardada.") },
        onError = show
    )

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = pickDocument, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar PDF o Word")
            }
            OutlinedButton(onClick = pickImage, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar foto de la galería")
            }
            OutlinedButton(onClick = takePhoto, modifier = Modifier.fillMaxWidth()) {
                Text("Sacar foto")
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (mine.isEmpty()) {
                item { EmptyText("Aún no has agregado archivos.") }
            }
            items(mine, key = { it.id }) { doc ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(doc.name, style = MaterialTheme.typography.titleMedium)
                        Text("Tipo: ${fileTypeLabel(doc.type)}")
                        Row {
                            TextButton(onClick = {
                                if (!openFile(context, doc.uri, doc.type)) {
                                    show("No se pudo abrir el archivo.")
                                }
                            }) {
                                Text("Ver")
                            }
                            TextButton(onClick = { toDelete = doc }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    val pending = toDelete
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar archivo") },
            text = { Text("¿Quieres eliminar \"${pending.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.removeDocument(pending.id)
                    deleteCameraPhoto(context, pending.uri, pending.name)
                    toDelete = null
                    show("Archivo eliminado.")
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
