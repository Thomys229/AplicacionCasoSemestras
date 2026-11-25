package com.example.reservaescuela.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.reservaescuela.model.Ticket
import com.example.reservaescuela.model.spaceName
import com.example.reservaescuela.ui.AppViewModel
import com.example.reservaescuela.ui.components.*

@Composable
fun IncidenciasScreen(vm: AppViewModel, show: (String) -> Unit) {
    val context = LocalContext.current
    val user = vm.currentUser ?: return
    var space by remember { mutableIntStateOf(1) }
    var location by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Leve") }
    var description by remember { mutableStateOf("") }
    var file by remember { mutableStateOf<PickedFile?>(null) }
    var error by remember { mutableStateOf("") }
    val pickFile = rememberFilePicker(ticketFileTypes) { selected ->
        file?.let { deleteCameraPhoto(context, it.uri, it.name) }
        file = selected
    }
    val takePhoto = rememberCameraCapture(
        onCaptured = { selected ->
            file?.let { deleteCameraPhoto(context, it.uri, it.name) }
            file = selected
        },
        onError = show
    )
    val visibleTickets = vm.tickets.filter { vm.isAdmin || it.owner == user.email }.sortedByDescending { it.id }

    FormColumn {
        SpaceDropdown(space) { space = it }
        AppTextField(location, { location = it }, "Número o lugar")
        DropdownField("Gravedad", listOf("Leve", "Atención inmediata"), severity) { severity = it }
        AppTextField(description, { description = it }, "Descripción del problema")
        OutlinedButton(onClick = pickFile) { Text("Adjuntar PDF, Word o foto") }
        OutlinedButton(onClick = takePhoto) { Text("Sacar foto") }
        val attached = file
        if (attached != null) {
            Text("Adjunto: ${attached.name}")
            Row {
                TextButton(onClick = { if (!openFile(context, attached.uri, attached.type)) show("No se pudo abrir el archivo.") }) { Text("Visualizar") }
                TextButton(onClick = {
                    file?.let { deleteCameraPhoto(context, it.uri, it.name) }
                    file = null
                }) { Text("Quitar adjunto") }
            }
        }
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        Button(
            onClick = {
                if (location.isBlank() || description.isBlank()) {
                    error = "Completa el lugar y la descripción."
                } else {
                    vm.addTicket(Ticket(
                        id = System.currentTimeMillis(), owner = "", space = space,
                        location = location.trim(), severity = severity, description = description.trim(),
                        fileUri = attached?.uri ?: "", fileName = attached?.name ?: "", fileType = attached?.type ?: ""
                    ))
                    location = ""
                    description = ""
                    file = null
                    error = ""
                    show("Incidencia registrada.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Enviar incidencia") }
        HorizontalDivider()
        Text("Incidencias registradas", style = MaterialTheme.typography.titleMedium)
        if (visibleTickets.isEmpty()) EmptyText("No hay incidencias.")
        visibleTickets.forEach { ticket ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${ticket.location} · ${ticket.severity}", style = MaterialTheme.typography.titleMedium)
                    Text("${spaceName(ticket.space)}: ${ticket.description}")
                    if (ticket.fileUri.isNotBlank()) {
                        Text("Archivo: ${ticket.fileName} (${fileTypeLabel(ticket.fileType)})")
                        Row {
                            TextButton(onClick = {
                                if (!openFile(context, ticket.fileUri, ticket.fileType)) show("No se pudo abrir el archivo.")
                            }) { Text("Ver archivo") }
                            TextButton(onClick = {
                                vm.removeTicketFile(ticket.id)
                                deleteCameraPhoto(context, ticket.fileUri, ticket.fileName)
                                show("Archivo eliminado.")
                            }) { Text("Eliminar archivo") }
                        }
                    }
                }
            }
        }
    }
}
