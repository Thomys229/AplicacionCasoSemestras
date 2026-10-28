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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.reservaescuela.ui.components.fileTypeLabel
import com.example.reservaescuela.ui.components.isPdfOrWord
import com.example.reservaescuela.ui.components.openFile
import com.example.reservaescuela.ui.components.pdfWordTypes
import com.example.reservaescuela.ui.components.rememberFilePicker

// cargar, ver y eliminar documentos pdf o word
@Composable
fun DocumentosScreen(vm: AppViewModel, show: (String) -> Unit) {
    val context = LocalContext.current
    val user = vm.currentUser ?: return
    val mine = vm.documents.filter { it.owner == user.email }.sortedByDescending { it.id }
    var toDelete by remember { mutableStateOf<Document?>(null) }

    val pickFile = rememberFilePicker(pdfWordTypes) { file ->
        if (!isPdfOrWord(file.type)) {
            show("Solo se permiten archivos PDF o Word.")
        } else {
            vm.addDocument(Document(System.currentTimeMillis(), "", file.name, file.type, file.uri))
            show("Documento cargado.")
        }
    }

    Column(Modifier.fillMaxSize()) {
        Button(
            onClick = pickFile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Cargar documento (PDF o Word)")
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (mine.isEmpty()) {
                item { EmptyText("Aún no has cargado documentos.") }
            }
            items(mine, key = { it.id }) { doc ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(doc.name, style = MaterialTheme.typography.titleMedium)
                        Text("Tipo: ${fileTypeLabel(doc.type)}")
                        Row {
                            TextButton(onClick = {
                                if (!openFile(context, doc.uri, doc.type)) {
                                    show("No hay una aplicación para abrir este archivo.")
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
            title = { Text("Eliminar documento") },
            text = { Text("¿Quieres eliminar \"${pending.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.removeDocument(pending.id)
                    toDelete = null
                    show("Documento eliminado.")
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
